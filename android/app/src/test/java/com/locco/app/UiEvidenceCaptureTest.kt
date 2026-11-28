package com.locco.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiEvidenceCaptureTest {
    @Test
    fun snapshotExcludesPasswordEditableAndInvisibleNodes() {
        val result = UiEvidenceCapture.snapshot(
            listOf(
                UiNodeCandidate(text = "Visible headline", className = "android.widget.TextView"),
                UiNodeCandidate(text = "secret123", password = true),
                UiNodeCandidate(text = "typed value", editable = true),
                UiNodeCandidate(text = "off screen", visibleToUser = false)
            )
        )

        assertEquals("partial", result.status)
        assertTrue(result.text!!.contains("Visible headline"))
        assertFalse(result.text.contains("secret123"))
        assertFalse(result.text.contains("typed value"))
        assertFalse(result.text.contains("off screen"))
        assertTrue(result.redactedFields.contains("password_nodes_excluded"))
        assertTrue(result.redactedFields.contains("editable_nodes_excluded"))
        assertTrue(result.redactedFields.contains("invisible_nodes_skipped"))
    }

    @Test
    fun snapshotRedactsCredentialCodesAndPaymentCardLikeValues() {
        val result = UiEvidenceCapture.snapshot(
            listOf(
                UiNodeCandidate(
                    text = "Verification code is 482913. Password: hunter2. Card 4111 1111 1111 1111."
                )
            )
        )

        assertNotNull(result.text)
        assertFalse(result.text!!.contains("482913"))
        assertFalse(result.text.contains("hunter2"))
        assertFalse(result.text.contains("4111 1111 1111 1111"))
        assertTrue(result.redactedFields.contains("one_time_codes"))
        assertTrue(result.redactedFields.contains("credential_like_values"))
        assertTrue(result.redactedFields.contains("payment_card_like_values"))
        assertEquals("partial", result.status)
    }

    @Test
    fun snapshotIsDeduplicatedAndStableForSameAccessibleEvidence() {
        val nodes = listOf(
            UiNodeCandidate(text = "Continue", className = "android.widget.Button"),
            UiNodeCandidate(text = "Continue", className = "android.widget.Button")
        )
        val first = UiEvidenceCapture.snapshot(nodes)
        val second = UiEvidenceCapture.snapshot(nodes)
        assertEquals(first.text, second.text)
        assertEquals(first.contentHash, second.contentHash)
        assertEquals(1, first.text!!.lines().size)
        assertEquals("available", first.status)
    }

    @Test
    fun snapshotBoundsNodeCountAndAggregateUtf8Bytes() {
        val nodes = (0 until UiEvidenceCapture.MAX_NODES + 20).map {
            UiNodeCandidate(text = "🙂".repeat(200), className = "android.widget.TextView")
        }
        val result = UiEvidenceCapture.snapshot(nodes, nodeLimitReached = true)
        assertNotNull(result.text)
        assertTrue(result.text!!.toByteArray(Charsets.UTF_8).size <= UiEvidenceCapture.MAX_SNAPSHOT_BYTES)
        assertTrue(result.redactedFields.contains("node_limit_reached"))
        assertTrue(result.redactedFields.contains("node_text_truncated"))
        assertEquals("partial", result.status)
        assertFalse(result.text.last().isHighSurrogate())
    }

    @Test
    fun unavailableHierarchyIsNotInvented() {
        val result = UiEvidenceCapture.snapshot(null)
        assertNull(result.text)
        assertNull(result.contentHash)
        assertEquals("unavailable", result.status)
        assertTrue(result.redactedFields.contains("accessible_hierarchy_unavailable"))
    }

    @Test
    fun editableOrPasswordControlsNeverExposeTargetLabels() {
        val password = UiEvidenceCapture.target(
            UiNodeCandidate(text = "password", className = "android.widget.EditText", password = true)
        )
        val editable = UiEvidenceCapture.target(
            UiNodeCandidate(text = "typed value", className = "android.widget.EditText", editable = true)
        )
        val button = UiEvidenceCapture.target(
            UiNodeCandidate(text = "Continue", className = "android.widget.Button")
        )

        assertNull(password.label)
        assertNull(editable.label)
        assertEquals("Continue", button.label)
        assertEquals("android.widget.Button", button.className)
        assertTrue(password.redactedFields.contains("password_target_excluded"))
        assertTrue(editable.redactedFields.contains("editable_target_excluded"))
    }

    @Test
    fun targetLabelUsesSameSensitiveValueFilters() {
        val target = UiEvidenceCapture.target(
            UiNodeCandidate(text = "Verification code is 123456", className = "android.widget.TextView")
        )
        assertFalse(target.label!!.contains("123456"))
        assertTrue(target.redactedFields.contains("one_time_codes"))
    }

 
    @Test
    fun rateLimiterThrottlesScrollStormsAndKeepsSourcesIndependent() {
        val limiter = UiEvidenceRateLimiter()
        assertTrue(limiter.shouldAccept("com.example.one", "container_scrolled", 1_000L))
        assertFalse(limiter.shouldAccept("com.example.one", "container_scrolled", 1_399L))
        assertTrue(limiter.shouldAccept("com.example.one", "container_scrolled", 1_400L))
        assertTrue(limiter.shouldAccept("com.example.two", "container_scrolled", 1_400L))
        assertTrue(limiter.shouldAccept("com.example.one", "control_clicked", 1_400L))
        assertFalse(limiter.shouldAccept("com.example.one", "unknown", 2_000L))
    }

    @Test
    fun rateLimiterThrottlesRapidClicksAndWindowEventsSeparately() {
        val limiter = UiEvidenceRateLimiter()
        assertTrue(limiter.shouldAccept("com.example.app", "control_clicked", 5_000L))
        assertFalse(limiter.shouldAccept("com.example.app", "control_clicked", 5_119L))
        assertTrue(limiter.shouldAccept("com.example.app", "control_clicked", 5_120L))
        assertTrue(limiter.shouldAccept("com.example.app", "screen_state_changed", 5_120L))
        assertFalse(limiter.shouldAccept("com.example.app", "screen_state_changed", 5_419L))
        assertTrue(limiter.shouldAccept("com.example.app", "screen_state_changed", 5_420L))
    }

    @Test
    fun emptyAccessibleTreeProducesUnavailableNotAnEmptySuccess() {
        val result = UiEvidenceCapture.snapshot(emptyList())
        assertNull(result.text)
        assertEquals("unavailable", result.status)
        assertTrue(result.redactedFields.contains("accessible_text_not_exposed"))
    }
}
