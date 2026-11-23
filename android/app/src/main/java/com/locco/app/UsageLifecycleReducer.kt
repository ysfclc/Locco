package com.locco.app

import android.app.usage.UsageEvents

internal data class UsageEventRecord(
    val packageName: String,
    val eventType: Int,
    val timestamp: Long,
    val activityClassName: String?
)

internal data class UsageLifecycleTransition(
    val packageName: String,
    val eventType: String,
    val timestamp: Long
)

/**
 * Converts activity-level UsageEvents into package-level foreground/background transitions.
 *
 * Android Q+ reports activity resume/pause events. Mapping each pause directly to an app
 * background event is incorrect when an app moves between its own activities. We therefore
 * track the active activity set and emit only package visibility boundary changes. Older
 * package-level events are used only when no activity-level events exist for that package.
 */
internal object UsageLifecycleReducer {
    fun reduce(records: List<UsageEventRecord>): List<UsageLifecycleTransition> {
        val result = mutableListOf<UsageLifecycleTransition>()
        records.groupBy { it.packageName }.forEach { (packageName, packageRecords) ->
            val modern = packageRecords.filter {
                it.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    it.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            }
            if (modern.isNotEmpty()) {
                reduceActivityEvents(packageName, modern, result)
            } else {
                packageRecords
                    .filter {
                        it.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                            it.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
                    }
                    .sortedBy { it.timestamp }
                    .forEach { record ->
                        result += UsageLifecycleTransition(
                            packageName = packageName,
                            eventType = if (record.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                "app_foregrounded"
                            } else {
                                "app_backgrounded"
                            },
                            timestamp = record.timestamp
                        )
                    }
            }
        }
        return result.sortedWith(compareBy<UsageLifecycleTransition> { it.timestamp }.thenBy { it.packageName })
    }

    private fun reduceActivityEvents(
        packageName: String,
        records: List<UsageEventRecord>,
        output: MutableList<UsageLifecycleTransition>
    ) {
        val activeActivities = mutableSetOf<String>()
        records.groupBy { it.timestamp }.toSortedMap().forEach { (timestamp, atTime) ->
            val wasForegrounded = activeActivities.isNotEmpty()
            atTime.forEach { record ->
                val activity = record.activityClassName?.takeIf { it.isNotBlank() } ?: return@forEach
                when (record.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> activeActivities.add(activity)
                    UsageEvents.Event.ACTIVITY_PAUSED -> activeActivities.remove(activity)
                }
            }
            val isForegrounded = activeActivities.isNotEmpty()
            if (wasForegrounded != isForegrounded) {
                output += UsageLifecycleTransition(
                    packageName = packageName,
                    eventType = if (isForegrounded) "app_foregrounded" else "app_backgrounded",
                    timestamp = timestamp
                )
            }
        }
    }
}
