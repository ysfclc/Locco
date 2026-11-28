package com.locco.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.security.MessageDigest
import java.util.UUID

/**
 * Passive, user-scoped observer for the accessible UI hierarchy exposed by Android.
 *
 * It does not click, gesture, type, take screenshots, or inspect app-private processes.
 * AccessibilityNodeInfo objects are read and reduced to bounded primitive values in the
 * callback; only those values cross to the worker thread.
 */
class LoccoAccessibilityObserver : AccessibilityService(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var workerThread: HandlerThread
    private lateinit var worker: Handler
    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences(LoccoNotificationObserver.OBSERVER_PREFS, Context.MODE_PRIVATE)
        workerThread = HandlerThread("locco-accessibility-observer").apply { start() }
        worker = Handler(workerThread.looper)
        prefs.registerOnSharedPreferenceChangeListener(this)
        applyObservationScope()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null ||
            key == LoccoNotificationObserver.OBSERVED_PACKAGES_KEY ||
            key == LoccoNotificationObserver.CAPTURE_UI_OBSERVATIONS_KEY ||
            key == LoccoNotificationObserver.UI_DISCLOSURE_VERSION_KEY
        ) {
            applyObservationScope()
        }
    }

    private fun applyObservationScope() {
        if (!::prefs.isInitialized) return
        val allowed = selectedPackages(prefs)
            .filter { it != packageName && it.isNotBlank() }
            .sorted()
        val enabled = isCaptureEnabled(prefs) && allowed.isNotEmpty()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = if (enabled) OBSERVED_EVENT_TYPES else 0
        info.packageNames = if (enabled) allowed.toTypedArray() else emptyArray()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = NOTIFICATION_TIMEOUT_MS
        // Intentionally do not set FLAG_INCLUDE_NOT_IMPORTANT_VIEWS,
        // FLAG_RETRIEVE_INTERACTIVE_WINDOWS or any gesture capability.
        setServiceInfo(info)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::worker.isInitialized) return

        val sourcePackage = event.packageName?.toString() ?: return
        if (sourcePackage == packageName || !isCaptureEnabled(prefs)) return
        if (sourcePackage !in selectedPackages(prefs)) return

        val eventType = when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "screen_state_changed"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "control_clicked"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "container_scrolled"
            else -> return
        }

        // AccessibilityEvent.eventTime uses uptime. Convert it to an approximate wall clock.
        val receivedAt = System.currentTimeMillis()
        val elapsedSinceEvent = (SystemClock.uptimeMillis() - event.eventTime).coerceAtLeast(0L)
        val occurredAt = (receivedAt - elapsedSinceEvent).coerceAtLeast(0L)

        // Copy all data before returning from this callback. Node handles are not retained.
        val root = runCatching { rootInActiveWindow }.getOrNull()
        val rootMatchesSource = root != null && root.packageName?.toString() == sourcePackage
        val (nodes, nodeLimitReached) = if (rootMatchesSource) {
            collectVisibleNodes(root!!)
        } else {
            null to false
        }
        val snapshot = UiEvidenceCapture.snapshot(nodes, nodeLimitReached)

        val targetCandidate = if (
            eventType == "control_clicked" || eventType == "container_scrolled"
        ) {
            runCatching { event.source }.getOrNull()
                ?.takeIf { it.packageName?.toString() == sourcePackage }
                ?.let(::copyNode)
        } else {
            null
        }
        val target = UiEvidenceCapture.target(targetCandidate)
        val screenClass = if (eventType == "screen_state_changed") {
            event.className?.toString()?.take(255)
        } else {
            null
        }

        val redacted = (snapshot.redactedFields + target.redactedFields).distinct().sorted()
        val dedupeKey = sha256(
            listOf(
                sourcePackage,
                eventType,
                occurredAt.toString(),
                screenClass.orEmpty(),
                target.label.orEmpty(),
                target.className.orEmpty(),
                snapshot.contentHash.orEmpty()
            ).joinToString("|")
        )
        val observation = UiObservationRecord(
            id = UUID.randomUUID().toString(),
            dedupeKey = dedupeKey,
            sourcePackage = sourcePackage,
            eventType = eventType,
            occurredAt = occurredAt,
            receivedAt = receivedAt,
            screenClass = screenClass,
            targetLabel = target.label,
            targetClass = target.className,
            contentText = snapshot.text,
            contentHash = snapshot.contentHash,
            redactedFields = redacted,
            snapshotStatus = snapshot.status
        )

        worker.post {
            // Revalidate both app scope and explicit consent at persistence time.
            val latest = getSharedPreferences(LoccoNotificationObserver.OBSERVER_PREFS, Context.MODE_PRIVATE)
            if (!isCaptureEnabled(latest)) return@post
            if (observation.sourcePackage !in selectedPackages(latest)) return@post
            runCatching {
                val database = LoccoDatabase.create(applicationContext)
                try {
                    database.recordUiObservation(observation)
                } finally {
                    database.close()
                }
            }
        }
    }

    private fun collectVisibleNodes(root: AccessibilityNodeInfo): Pair<List<UiNodeCandidate>?, Boolean> {
        val result = mutableListOf<UiNodeCandidate>()
        var limitReached = false

        fun visit(node: AccessibilityNodeInfo, depth: Int) {
            if (depth > MAX_TREE_DEPTH || result.size >= UiEvidenceCapture.MAX_NODES) {
                limitReached = true
                return
            }
            if (node.packageName?.toString() != packageNameForTree) {
                // A subtree from another package is never copied into this source record.
                return
            }
            copyNode(node)?.let(result::add)
            for (index in 0 until node.childCount) {
                if (result.size >= UiEvidenceCapture.MAX_NODES) {
                    limitReached = true
                    break
                }
                val child = runCatching { node.getChild(index) }.getOrNull() ?: continue
                visit(child, depth + 1)
            }
        }

        // The root package was checked by the callback. Keep a local source identity for
        // child validation without retaining Android node handles.
        packageNameForTree = root.packageName?.toString() ?: return null to false
        visit(root, 0)
        return result to limitReached
    }

    private var packageNameForTree: String = ""

    private fun copyNode(node: AccessibilityNodeInfo): UiNodeCandidate =
        UiNodeCandidate(
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            visibleToUser = node.isVisibleToUser,
            password = node.isPassword,
            editable = node.isEditable
        )

    override fun onInterrupt() {
        // Observation is passive; interruption does not trigger an action or retry.
    }

    override fun onDestroy() {
        if (::prefs.isInitialized) prefs.unregisterOnSharedPreferenceChangeListener(this)
        if (::workerThread.isInitialized) workerThread.quitSafely()
        super.onDestroy()
    }

    private fun selectedPackages(preferences: SharedPreferences): Set<String> =
        preferences.getStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, emptySet())
            ?.toSet()
            .orEmpty()

    private fun isCaptureEnabled(preferences: SharedPreferences): Boolean =
        preferences.getBoolean(LoccoNotificationObserver.CAPTURE_UI_OBSERVATIONS_KEY, false) &&
            preferences.getInt(LoccoNotificationObserver.UI_DISCLOSURE_VERSION_KEY, 0) >=
            LoccoNotificationObserver.UI_DISCLOSURE_VERSION

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        private const val NOTIFICATION_TIMEOUT_MS = 150L
        private const val MAX_TREE_DEPTH = 16
        private const val OBSERVED_EVENT_TYPES =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED
    }
}
