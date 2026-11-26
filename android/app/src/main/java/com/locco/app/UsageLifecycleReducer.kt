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
 * Converts UsageEvents into package-level foreground/background transitions.
 *
 * On Android Q+, activity resume/pause records include an activity class name. The deprecated
 * MOVE_TO_FOREGROUND/BACKGROUND constants share event-type values with the modern activity
 * constants, so event type alone cannot distinguish the formats. A nonblank activity class name
 * is the discriminator: when present, reduce activity boundaries; otherwise use the legacy
 * package-level interpretation. Class names remain transient and are never persisted.
 */
internal object UsageLifecycleReducer {
    fun reduce(records: List<UsageEventRecord>): List<UsageLifecycleTransition> {
        val result = mutableListOf<UsageLifecycleTransition>()
        records.groupBy { it.packageName }.forEach { (packageName, packageRecords) ->
            val activityEvents = packageRecords.filter { record ->
                (record.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    record.eventType == UsageEvents.Event.ACTIVITY_PAUSED) &&
                    !record.activityClassName.isNullOrBlank()
            }

            if (activityEvents.isNotEmpty()) {
                reduceActivityEvents(packageName, activityEvents, result)
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
            atTime.forEach recordLoop@{ record ->
                val activity = record.activityClassName?.takeIf { it.isNotBlank() } ?: return@recordLoop
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
