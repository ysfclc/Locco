package com.locco.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.HandlerThread
import java.security.MessageDigest
import java.util.UUID

/**
 * Opt-in Android system adapter.
 * Notification content is read only when the user explicitly enables content
 * capture, and only for packages in the selected-app allowlist.
 */
class LoccoNotificationObserver : NotificationListenerService() {
    private lateinit var workerThread: HandlerThread
    private lateinit var worker: Handler

    override fun onCreate() {
        super.onCreate()
        workerThread = HandlerThread("locco-notification-observer").apply { start() }
        worker = Handler(workerThread.looper)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        val prefs = getSharedPreferences(OBSERVER_PREFS, MODE_PRIVATE)
        val initialAllowlist = prefs.getStringSet(OBSERVED_PACKAGES_KEY, emptySet()) ?: emptySet()
        if (packageName !in initialAllowlist) return

        val postedAt = sbn.postTime
        val category = sbn.notification.category?.take(80)
        val notificationKey = sbn.key
        val captureContent = prefs.getBoolean(CAPTURE_NOTIFICATION_CONTENT_KEY, false)
        val content = if (captureContent) extractVisibleContent(sbn.notification) else null

        worker.post {
            val latestPrefs = getSharedPreferences(OBSERVER_PREFS, MODE_PRIVATE)
            val latestAllowlist = latestPrefs.getStringSet(OBSERVED_PACKAGES_KEY, emptySet()) ?: emptySet()
            if (packageName !in latestAllowlist) return@post
            // Re-check the content toggle before persisting a callback already queued.
            val permittedContent = if (
                latestPrefs.getBoolean(CAPTURE_NOTIFICATION_CONTENT_KEY, false)
            ) content else null
            val dedupe = sha256("$packageName|$notificationKey|$postedAt")
            runCatching {
                val database = LoccoDatabase.create(applicationContext)
                try {
                    database.recordNotificationObservation(
                        id = UUID.randomUUID().toString(),
                        dedupeKey = dedupe,
                        sourcePackage = packageName,
                        occurredAt = postedAt,
                        receivedAt = System.currentTimeMillis(),
                        category = category,
                        content = permittedContent
                    )
                } finally {
                    database.close()
                }
            }
        }
    }

    private fun extractVisibleContent(notification: Notification): CapturedNotificationContent? {
        val extras = notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val bodyParts = buildList {
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.takeIf(String::isNotBlank)?.let(::add)
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.map { it.toString() }
                ?.filter(String::isNotBlank)
                ?.forEach { add(it) }
        }.distinct()
        return NotificationContentCapture.capture(title, bodyParts.joinToString("\n").takeIf { it.isNotBlank() })
    }

    override fun onDestroy() {
        if (::workerThread.isInitialized) workerThread.quitSafely()
        super.onDestroy()
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val OBSERVER_PREFS = "locco_observer_v1"
        const val OBSERVED_PACKAGES_KEY = "observed_packages"
        const val CAPTURE_NOTIFICATION_CONTENT_KEY = "capture_notification_content"
        const val CAPTURE_UI_OBSERVATIONS_KEY = "capture_ui_observations"
        const val UI_DISCLOSURE_VERSION_KEY = "ui_disclosure_version"
        const val UI_DISCLOSURE_VERSION = 1
    }
}
