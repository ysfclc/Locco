package com.locco.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationContentCaptureTest {
    @Test
    fun capturesActualNotificationTextAndStableContentHash() {
        val captured = NotificationContentCapture.capture("New message", "The package arrives tomorrow.")
        assertNotNull(captured)
        assertEquals("New message", captured!!.title)
        assertEquals("The package arrives tomorrow.", captured.text)
        assertEquals(64, captured.contentHash.length)
        assertTrue(captured.redactedFields.isEmpty())
    }

    @Test
    fun redactsObviousVerificationCodesCredentialsAndPaymentCardNumbers() {
        val captured = NotificationContentCapture.capture(
            "Security notice",
            "Your verification code is 482913. Password: hunter2. Card 4111 1111 1111 1111."
        )
        assertNotNull(captured)
        val text = captured!!.text!!
        assertFalse(text.contains("482913"))
        assertFalse(text.contains("hunter2"))
        assertFalse(text.contains("4111 1111 1111 1111"))
        assertEquals(3, captured.redactedFields.size)
        assertTrue(captured.redactedFields.contains("one_time_codes"))
        assertTrue(captured.redactedFields.contains("credential_like_values"))
        assertTrue(captured.redactedFields.contains("payment_card_like_values"))
    }

    @Test
    fun enforcesUtf8ByteBoundsWithoutSplittingSurrogatePairs() {
        val title = "🙂".repeat(300)
        val text = "é".repeat(10_000)
        val captured = NotificationContentCapture.capture(title, text)
        assertNotNull(captured)
        assertTrue(captured!!.title!!.toByteArray(Charsets.UTF_8).size <= NotificationContentCapture.MAX_TITLE_BYTES)
        assertTrue(captured.text!!.toByteArray(Charsets.UTF_8).size <= NotificationContentCapture.MAX_TEXT_BYTES)
        assertFalse(captured.title!!.last().isHighSurrogate())
        assertFalse(captured.text!!.last().isHighSurrogate())
    }

    @Test
    fun ignoresEmptyNotificationContent() {
        assertNull(NotificationContentCapture.capture(null, " \n\t "))
    }
}
