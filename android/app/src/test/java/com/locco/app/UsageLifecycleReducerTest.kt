package com.locco.app

import android.app.usage.UsageEvents
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageLifecycleReducerTest {
    private fun event(packageName: String, type: Int, time: Long, activity: String? = null) =
        UsageEventRecord(packageName, type, time, activity)

    @Test
    fun activitySwitchWithinSamePackageDoesNotCreateFalseBackgroundTransition() {
        val result = UsageLifecycleReducer.reduce(
            listOf(
                event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 10L, "Main"),
                event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 20L, "Details"),
                event("com.example.app", UsageEvents.Event.ACTIVITY_PAUSED, 20L, "Main"),
                event("com.example.app", UsageEvents.Event.ACTIVITY_PAUSED, 30L, "Details")
            )
        )

        assertEquals(
            listOf(
                UsageLifecycleTransition("com.example.app", "app_foregrounded", 10L),
                UsageLifecycleTransition("com.example.app", "app_backgrounded", 30L)
            ),
            result
        )
    }

    @Test
    fun sameTimestampActivityHandoffDoesNotEmitTransientBackground() {
        val result = UsageLifecycleReducer.reduce(
            listOf(
                event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 10L, "Main"),
                event("com.example.app", UsageEvents.Event.ACTIVITY_PAUSED, 20L, "Main"),
                event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 20L, "Details")
            )
        )

        assertEquals(
            listOf(UsageLifecycleTransition("com.example.app", "app_foregrounded", 10L)),
            result
        )
    }

    @Test
    fun packageLevelEventsRemainSupportedWhenActivityClassNamesAreAbsent() {
        val result = UsageLifecycleReducer.reduce(
            listOf(
                event("com.example.app", UsageEvents.Event.MOVE_TO_BACKGROUND, 30L),
                event("com.example.app", UsageEvents.Event.MOVE_TO_FOREGROUND, 10L)
            )
        )

        assertEquals(
            listOf(
                UsageLifecycleTransition("com.example.app", "app_foregrounded", 10L),
                UsageLifecycleTransition("com.example.app", "app_backgrounded", 30L)
            ),
            result
        )
    }

    @Test
    fun validActivityEventsTakePrecedenceOverLegacyPackageLevelEvents() {
        val result = UsageLifecycleReducer.reduce(
            listOf(
                event("com.example.app", UsageEvents.Event.MOVE_TO_FOREGROUND, 10L),
                event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 11L, "Main"),
                event("com.example.app", UsageEvents.Event.MOVE_TO_BACKGROUND, 30L),
                event("com.example.app", UsageEvents.Event.ACTIVITY_PAUSED, 31L, "Main")
            )
        )

        assertEquals(
            listOf(
                UsageLifecycleTransition("com.example.app", "app_foregrounded", 11L),
                UsageLifecycleTransition("com.example.app", "app_backgrounded", 31L)
            ),
            result
        )
    }

    @Test
    fun classlessActivityTypeUsesLegacyCompatibleInterpretation() {
        // Android's deprecated MOVE_TO_* constants alias the modern ACTIVITY_* values.
        // Without an activity class name, these records cannot be distinguished reliably.
        val result = UsageLifecycleReducer.reduce(
            listOf(event("com.example.app", UsageEvents.Event.ACTIVITY_RESUMED, 10L))
        )

        assertEquals(
            listOf(UsageLifecycleTransition("com.example.app", "app_foregrounded", 10L)),
            result
        )
    }
}
