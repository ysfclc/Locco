package com.locco.app

/**
 * Pure domain operations for the first Locco capture kernel.
 * No Android APIs, persistence, or UI state are allowed here.
 */
internal object SessionKernel {
    fun capture(current: Session?, text: String, timestamp: Long, noteId: String, sessionId: String): Session? {
        val value = text.trim()
        if (value.isEmpty()) return current
        val note = Note(noteId, value, timestamp)
        if (current == null) {
            return Session(sessionId, titleFor(value), timestamp, listOf(note))
        }
        val title = if (current.notes.isEmpty()) titleFor(value) else current.title
        return current.copy(title = title, notes = current.notes + note)
    }

    private fun titleFor(text: String): String =
        text.take(40).let { if (text.length > 40) it + "…" else it }
}
