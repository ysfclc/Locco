package com.locco.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureKernelTest {
    @Test
    fun manualCaptureCreatesLinkedEvidenceWithContentHash() {
        val prepared = CaptureKernel.prepare(
            "  Preserve this exact fact.  ", "Research note", null,
            123L, "source-1", "evidence-1"
        )
        assertNotNull(prepared)
        assertEquals("manual_text", prepared!!.source.kind)
        assertEquals("Research note", prepared.source.title)
        assertEquals("Preserve this exact fact.", prepared.evidence.excerpt)
        assertEquals(prepared.source.id, prepared.evidence.sourceId)
        assertTrue(prepared.evidence.contentHash!!.matches(Regex("sha256:[0-9a-f]{64}")))
        assertEquals(123L, prepared.evidence.capturedAt)
    }

    @Test
    fun sharedUrlIsRecordedAsSourceProvenance() {
        val prepared = CaptureKernel.prepare(
            "https://example.test/a-page", "Example page", null,
            456L, "source-share", "evidence-share", "shared_text"
        )
        assertNotNull(prepared)
        assertEquals("shared_text", prepared!!.source.kind)
        assertEquals("https://example.test/a-page", prepared.source.locator)
        assertEquals("Example page", prepared.source.title)
    }

    @Test
    fun explicitUrlIsValidatedAndTitleDefaultsToFirstLine() {
        val prepared = CaptureKernel.prepare(
            "A short excerpt", null, " HTTPS://example.test/source ",
            10L, "source-url", "evidence-url"
        )
        assertEquals("https://example.test/source", prepared!!.source.locator)
        assertEquals("A short excerpt", prepared.source.title)
    }

    @Test
    fun blankCaptureReturnsNullAndOversizedCaptureIsRejected() {
        assertNull(CaptureKernel.prepare("  \n ", null, null, 1L, "source-blank", "evidence-blank"))
        assertThrows(IllegalArgumentException::class.java) {
            CaptureKernel.prepare(
                "x".repeat(CaptureKernel.MAX_CONTENT_BYTES + 1), null, null,
                1L, "source-large", "evidence-large"
            )
        }
    }

    @Test
    fun invalidOrCredentialBearingUrlsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            CaptureKernel.prepare("Excerpt", null, "file:///etc/passwd", 1L, "source-invalid", "evidence-invalid")
        }
        assertThrows(IllegalArgumentException::class.java) {
            CaptureKernel.prepare(
                "Excerpt", null, "https://user:password@example.test/private",
                1L, "source-credentials", "evidence-credentials"
            )
        }
    }

    @Test
    fun unicodeTitleTruncationKeepsWholeCodePointsWithinByteLimit() {
        val prepared = CaptureKernel.prepare(
            "Evidence", "😀".repeat(100), null, 1L, "source-unicode", "evidence-unicode"
        )
        val title = prepared!!.source.title!!
        assertFalse(title.isEmpty())
        assertTrue(title.startsWith("😀"))
        assertEquals(0, title.length % 2)
        assertTrue(title.toByteArray(Charsets.UTF_8).size <= 240)
    }
}
