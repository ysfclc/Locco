package com.locco.app

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.security.MessageDigest
import java.util.UUID

/**
 * Imports Android-provided package lifecycle history for explicitly selected apps.
 * This is queried on demand; it is not a continuous background monitor.
 */
internal object UsageStatsObserver {
    private const val HISTORY_WINDOW_MS = 24L * 60L * 60L * 1000L

    fun hasAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    fun collect(context: Context, allowlist: Set<String>, database: LoccoDatabase): Int {
        if (allowlist.isEmpty() || !hasAccess(context)) return 0
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return 0
        val end = System.currentTimeMillis()
        val begin = end - HISTORY_WINDOW_MS
        val usageEvents = manager.queryEvents(begin, end) ?: return 0
        val event = UsageEvents.Event()
        val records = mutableListOf<UsageEventRecord>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName !in allowlist) continue
            val timestamp = event.timeStamp
            if (timestamp <= 0L) continue
            records += UsageEventRecord(
                packageName = packageName,
                eventType = event.eventType,
                timestamp = timestamp,
                activityClassName = event.className
            )
        }

        val transitions = UsageLifecycleReducer.reduce(records)
        transitions.forEach { transition ->
            val dedupeKey = sha256(
                "${transition.packageName}|${transition.eventType}|${transition.timestamp}"
            )
            database.recordUsageObservation(
                id = UUID.randomUUID().toString(),
                dedupeKey = dedupeKey,
                sourcePackage = transition.packageName,
                eventType = transition.eventType,
                occurredAt = transition.timestamp,
                receivedAt = end
            )
        }
        return transitions.size
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
