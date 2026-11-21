package com.locco.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SessionKernelTest {
    @Test fun captureWithoutSessionCreatesSessionAndKeepsFirstNote() {
        val session = SessionKernel.capture(null, "  First thought  ", 10L, "note-1", "session-1")
        assertNotNull(session)
        assertEquals("session-1", session!!.id)
        assertEquals("First thought", session.title)
        assertEquals(listOf("First thought"), session.notes.map { it.text })
        assertEquals(10L, session.notes.single().time)
    }

    @Test fun captureAppendsAndPreservesExistingTitle() {
        val original = Session("s", "Original title", 1L, listOf(Note("n1", "A", 1L)))
        val updated = SessionKernel.capture(original, "B", 2L, "n2", "unused")
        assertEquals("Original title", updated!!.title)
        assertEquals(listOf("A", "B"), updated.notes.map { it.text })
        assertEquals(listOf("n1", "n2"), updated.notes.map { it.id })
    }

    @Test fun captureIntoEmptySessionSetsTitleFromFirstNote() {
        val original = Session("s", "New session", 1L, emptyList())
        val updated = SessionKernel.capture(original, "First entry", 2L, "n1", "unused")
        assertEquals("First entry", updated!!.title)
        assertEquals(1, updated.notes.size)
    }

    @Test fun blankCaptureDoesNotChangeSession() {
        val original = Session("s", "Title", 1L, emptyList())
        assertEquals(original, SessionKernel.capture(original, "  \n ", 2L, "n", "s2"))
        assertNull(SessionKernel.capture(null, " ", 2L, "n", "s2"))
    }

    @Test fun longFirstNoteTitleIsLimitedToFortyCharacters() {
        val value = "123456789012345678901234567890123456789012345"
        val created = SessionKernel.capture(null, value, 3L, "n1", "s1")
        assertEquals(41, created!!.title.length)
        assertEquals("…", created.title.takeLast(1))
    }
}
