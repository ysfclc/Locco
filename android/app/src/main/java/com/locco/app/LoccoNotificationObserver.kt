package com.locco.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.HandlerThread
import java.security.MessageDigest
import java.util.UUID

/**
 * Opt-in Android system adapter. It stores package/category/timestamps only.
 * Notification text, title, actions, extras and raw payloads are intentionally ignored.
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
        // Copy only the small metadata fields needed by the worker. Never read notification.extras.
        val packageName = sbn.packageName ?: return
        val postedAt = sbn.postTime
        val category = sbn.notification.category?.take(80)
        val notificationKey = sbn.key
        worker.post {
            val prefs = getSharedPreferences(OBSERVER_PREFS, MODE_PRIVATE)
            val allowlist = prefs.getStringSet(OBSERVED_PACKAGES_KEY, emptySet()) ?: emptySet()
            if (packageName !in allowlist) return@post
            val dedupe = sha256("$packageName|$notificationKey|$postedAt")
            val database = runCatching { LoccoDatabase.create(applicationContext) }.getOrNull() ?: return@post
            try {
                database.recordNotificationObservation(
                    id = UUID.randomUUID().toString(),
                    dedupeKey = dedupe,
                    sourcePackage = packageName,
                    occurredAt = postedAt,
                    receivedAt = System.currentTimeMillis(),
                    category = category
                )
            } finally {
                database.close()
            }
        }
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
    }
}
