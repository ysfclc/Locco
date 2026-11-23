package com.locco.app

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class LoccoDatabaseKeyInstrumentedTest {
    private lateinit var context: Context
    private lateinit var database: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = context.getDatabasePath("locco_kernel.db")
        clearState()
    }

    @After
    fun tearDown() {
        clearState()
    }

    @Test
    fun generatedDatabaseKeyCanBeUnwrappedAfterProcessLikeReopen() {
        val first = LoccoDatabaseKey.obtain(context)
        val second = LoccoDatabaseKey.obtain(context)
        try {
            assertArrayEquals(first, second)
            assertTrue(first.size == 32)
        } finally {
            first.fill(0)
            second.fill(0)
        }
    }

    @Test
    fun existingZeroByteDatabaseWithoutEnvelopeFailsClosed() {
        database.parentFile?.mkdirs()
        check(database.createNewFile())
        assertThrows(IllegalStateException::class.java) { LoccoDatabaseKey.obtain(context) }
        check(database.exists())
        assertTrue(database.length() == 0L)
    }

    @Test
    fun existingPlaintextDatabaseWithoutEnvelopeIsPreserved() {
        database.parentFile?.mkdirs()
        database.writeBytes("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(64))
        assertThrows(IllegalStateException::class.java) { LoccoDatabaseKey.obtain(context) }
        assertTrue(database.exists())
        assertTrue(database.readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun missingKeystoreWrappingKeyDoesNotReplaceExistingDatabase() {
        val key = LoccoDatabaseKey.obtain(context)
        key.fill(0)
        database.parentFile?.mkdirs()
        database.writeBytes(byteArrayOf(1, 2, 3, 4))
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry("com.locco.app.database.wrap.v1")
        assertThrows(IllegalStateException::class.java) { LoccoDatabaseKey.obtain(context) }
        assertTrue(database.exists())
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), database.readBytes())
    }

    @Test
    fun malformedKeyEnvelopeFailsClosed() {
        context.getSharedPreferences("locco_database_key_v1", Context.MODE_PRIVATE)
            .edit().putString("wrapped_key", "!").commit()
        assertThrows(IllegalStateException::class.java) { LoccoDatabaseKey.obtain(context) }
        assertTrue(!database.exists())
    }

    @Test
    fun actualSessionContentIsEncryptedOnDiskAndReadableAfterReopen() {
        val sentinel = "LOCCO_ENCRYPTION_SENTINEL_7f39b2c1"
        val session = Session(
            "session-encryption-check", "Encryption verification", 1_800_000_000_000L,
            listOf(Note("note-encryption-check", sentinel, 1_800_000_000_001L))
        )
        val firstOpen = LoccoDatabase.create(context)
        firstOpen.replaceSessions(listOf(session))
        assertEquals(listOf(session), firstOpen.readSessions())
        firstOpen.close()

        assertTrue(database.exists())
        assertFalse(database.readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)))
        val artifacts = listOf(
            database, File(database.path + "-wal"), File(database.path + "-shm"), File(database.path + "-journal")
        ).filter { it.exists() }
        val sentinelBytes = sentinel.toByteArray(Charsets.UTF_8)
        artifacts.forEach { file ->
            assertFalse("Plaintext sentinel leaked into ${file.name}", containsBytes(file.readBytes(), sentinelBytes))
        }

        val reopened = LoccoDatabase.create(context)
        try { assertEquals(listOf(session), reopened.readSessions()) } finally { reopened.close() }
    }

    @Test
    fun notificationObservationIsRedactedDeduplicatedAndStoredEncrypted() {
        val packageSentinel = "com.locco.observer.sentinel"
        val dedupe = "a".repeat(64)
        val first = LoccoDatabase.create(context)
        try {
            first.recordNotificationObservation(
                id = "observation-1", dedupeKey = dedupe, sourcePackage = packageSentinel,
                occurredAt = 1_800_000_000_100L, receivedAt = 1_800_000_000_200L, category = "message"
            )
            first.recordNotificationObservation(
                id = "observation-2", dedupeKey = dedupe, sourcePackage = packageSentinel,
                occurredAt = 1_800_000_000_100L, receivedAt = 1_800_000_000_300L, category = "message"
            )
            val events = first.readObservations()
            assertEquals(1, events.size)
            assertEquals(packageSentinel, events.single().sourcePackage)
            assertEquals("notification_posted", events.single().eventType)
            assertEquals("android_notification_listener", events.single().acquisitionMethod)
            assertTrue(events.single().redactedFields.contains("notification_text"))
        } finally { first.close() }

        assertFalse(containsBytes(database.readBytes(), packageSentinel.toByteArray(Charsets.UTF_8)))
        val reopened = LoccoDatabase.create(context)
        try {
            assertEquals(1, reopened.readObservations().size)
            assertEquals(packageSentinel, reopened.readObservations().single().sourcePackage)
        } finally { reopened.close() }
    }

    @Test
    fun usageLifecycleEventsAreDeduplicatedAndSurviveReopen() {
        val first = LoccoDatabase.create(context)
        try {
            first.recordUsageObservation(
                id = "usage-1", dedupeKey = "b".repeat(64), sourcePackage = "com.locco.usage.fixture",
                eventType = "app_foregrounded", occurredAt = 1_800_000_000_100L, receivedAt = 1_800_000_000_200L
            )
            first.recordUsageObservation(
                id = "usage-2", dedupeKey = "b".repeat(64), sourcePackage = "com.locco.usage.fixture",
                eventType = "app_foregrounded", occurredAt = 1_800_000_000_100L, receivedAt = 1_800_000_000_300L
            )
            assertEquals(1, first.readUsageObservations().size)
            assertEquals("android_usage_events", first.readUsageObservations().single().acquisitionMethod)
        } finally { first.close() }
        val reopened = LoccoDatabase.create(context)
        try {
            val event = reopened.readUsageObservations().single()
            assertEquals("app_foregrounded", event.eventType)
            assertEquals("com.locco.usage.fixture", event.sourcePackage)
        } finally { reopened.close() }
    }

    @Test
    fun sourcesAndEvidencePersistAcrossReopenAndCascadeOnSourceDelete() {
        val source = SourceEntry("source-evidence-roundtrip", "web_page", "https://example.test/article",
            "Fixture article", 1_800_000_000_000L)
        val excerptEvidence = EvidenceEntry("evidence-excerpt", source.id, "A preserved factual excerpt", null, 1_800_000_000_100L)
        val hashEvidence = EvidenceEntry("evidence-hash", source.id, null, "sha256:" + "c".repeat(64), 1_800_000_000_200L)

        val first = LoccoDatabase.create(context)
        first.insertSource(source)
        first.insertEvidence(excerptEvidence)
        first.insertEvidence(hashEvidence)
        assertEquals(listOf(source), first.readSources())
        assertEquals(listOf(hashEvidence, excerptEvidence), first.readEvidenceForSource(source.id))
        first.close()

        val reopened = LoccoDatabase.create(context)
        try {
            assertEquals(listOf(source), reopened.readSources())
            assertEquals(listOf(hashEvidence, excerptEvidence), reopened.readEvidenceForSource(source.id))
            assertTrue(reopened.deleteSource(source.id))
            assertTrue(reopened.readSources().isEmpty())
            assertTrue(reopened.readEvidenceForSource(source.id).isEmpty())
            assertFalse(reopened.deleteSource(source.id))
        } finally { reopened.close() }
    }

    @Test
    fun timelineCursorPagesStableOrderAndJoinedSourceMetadataWithoutDuplicates() {
        val db = LoccoDatabase.create(context)
        try {
            val sourceA = SourceEntry("timeline-source-a", "web_page", "https://a.test", "Source A", 10L)
            val sourceB = SourceEntry("timeline-source-b", "document", null, "Source B", 11L)
            db.insertSource(sourceA)
            db.insertSource(sourceB)
            db.insertEvidence(EvidenceEntry("timeline-01", sourceA.id, "newest", null, 100L))
            db.insertEvidence(EvidenceEntry("timeline-02a", sourceB.id, "same-time A", null, 90L))
            db.insertEvidence(EvidenceEntry("timeline-02b", sourceA.id, "same-time B", null, 90L))
            db.insertEvidence(EvidenceEntry("timeline-03", sourceB.id, "oldest", null, 80L))
            val first = db.readTimelinePage(limit = 2)
            assertEquals(listOf("timeline-01", "timeline-02a"), first.entries.map { it.evidence.id })
            assertEquals(sourceA, first.entries.first().source)
            assertEquals(sourceB, first.entries[1].source)
            assertEquals(TimelineCursor(90L, "timeline-02a"), first.nextCursor)
            val second = db.readTimelinePage(limit = 2, cursor = first.nextCursor)
            assertEquals(listOf("timeline-02b", "timeline-03"), second.entries.map { it.evidence.id })
            assertEquals(null, second.nextCursor)
            assertEquals(4, (first.entries + second.entries).map { it.evidence.id }.distinct().size)
        } finally { db.close() }
    }

    @Test
    fun capturedTextPersistsEncryptedWithProvenanceAndAppearsInTimelineAfterReopen() {
        val sentinel = "LOCCO_CAPTURE_FIXTURE_81d4"
        val prepared = CaptureKernel.prepare(
            text = sentinel, title = "Shared fixture", locator = "https://example.test/fixture",
            capturedAt = 1_800_000_000_321L, sourceId = "capture-source-v1",
            evidenceId = "capture-evidence-v1", sourceKind = "shared_text"
        )!!
        val first = LoccoDatabase.create(context)
        try {
            first.storeSourceWithEvidence(prepared.source, prepared.evidence)
            assertEquals(1, first.readTimelinePage(limit = 10).entries.size)
        } finally { first.close() }

        assertFalse(containsBytes(database.readBytes(), sentinel.toByteArray(Charsets.UTF_8)))
        val reopened = LoccoDatabase.create(context)
        try {
            val page = reopened.readTimelinePage(limit = 10)
            assertEquals(1, page.entries.size)
            assertEquals(sentinel, page.entries.single().evidence.excerpt)
            assertEquals("shared_text", page.entries.single().source.kind)
            assertEquals("Shared fixture", page.entries.single().source.title)
            assertEquals("https://example.test/fixture", page.entries.single().source.locator)
            assertTrue(page.entries.single().evidence.contentHash!!.startsWith("sha256:"))
            assertEquals(null, page.nextCursor)
        } finally { reopened.close() }
    }

    @Test
    fun evidenceRequiresPayloadAndAnExistingSource() {
        assertThrows(IllegalArgumentException::class.java) {
            EvidenceEntry("empty-evidence", "source-missing", null, null, 1L)
        }
        val db = LoccoDatabase.create(context)
        try {
            assertThrows(RuntimeException::class.java) {
                db.insertEvidence(EvidenceEntry("orphan-evidence", "missing-source", "excerpt", null, 1L))
            }
        } finally { db.close() }
    }

    @Test
    fun sourceAndFirstEvidenceAreCommittedAtomically() {
        val db = LoccoDatabase.create(context)
        try {
            val original = SourceEntry("source-original", "web_page", null, null, 1L)
            db.insertSource(original)
            db.insertEvidence(EvidenceEntry("evidence-unique", original.id, "existing", null, 2L))
            val attemptedSource = SourceEntry("source-should-rollback", "web_page", null, null, 3L)
            val conflictingEvidence = EvidenceEntry("evidence-unique", attemptedSource.id, "conflict", null, 4L)
            assertThrows(RuntimeException::class.java) {
                db.storeSourceWithEvidence(attemptedSource, conflictingEvidence)
            }
            assertTrue(db.readSources().none { it.id == attemptedSource.id })
            assertEquals(1, db.readEvidenceForSource(original.id).size)
        } finally { db.close() }
    }

    @Test
    fun sessionNoteCapturePersistsSessionEvidenceAndSourceLinkAcrossReopen() {
        val sentinel = "LOCCO_SESSION_CAPTURE_39A2"
        val emptySession = Session("session-capture-linked", "New session", 100L, emptyList())
        val capturedSession = SessionKernel.capture(emptySession, sentinel, 200L, "session-note-linked", emptySession.id)!!
        val note = capturedSession.notes.single()
        val prepared = CaptureKernel.prepare(
            text = sentinel, title = "Session source", locator = "https://example.test/session",
            capturedAt = note.time, sourceId = "session-source-linked", evidenceId = "session-evidence-linked"
        )!!

        val first = LoccoDatabase.create(context)
        try {
            first.insertSession(emptySession)
            first.storeSessionNoteWithEvidence(capturedSession, note, prepared.source, prepared.evidence)
            assertEquals(listOf(capturedSession), first.readSessions())
            assertEquals(listOf(prepared.source), first.readSourcesForNote(note.id))
            assertEquals(listOf(prepared.evidence), first.readEvidenceForSource(prepared.source.id))
        } finally { first.close() }

        assertFalse(containsBytes(database.readBytes(), sentinel.toByteArray(Charsets.UTF_8)))
        val reopened = LoccoDatabase.create(context)
        try {
            assertEquals(listOf(capturedSession), reopened.readSessions())
            assertEquals(listOf(prepared.source), reopened.readSourcesForNote(note.id))
            assertEquals(listOf(prepared.evidence), reopened.readEvidenceForSource(prepared.source.id))
        } finally { reopened.close() }
    }

    @Test
    fun sessionNoteCaptureRollsBackAllRecordsWhenEvidenceInsertFails() {
        val db = LoccoDatabase.create(context)
        try {
            val existingSource = SourceEntry("rollback-existing-source", "manual_text", null, "Existing", 1L)
            db.insertSource(existingSource)
            db.insertEvidence(EvidenceEntry("rollback-duplicate-evidence", existingSource.id, "existing", null, 2L))
            val emptySession = Session("rollback-new-session", "New session", 3L, emptyList())
            val nextSession = SessionKernel.capture(emptySession, "New note", 4L, "rollback-new-note", emptySession.id)!!
            val note = nextSession.notes.single()
            val attemptedSource = SourceEntry("rollback-new-source", "manual_text", null, "New", 4L)
            val attemptedEvidence = EvidenceEntry("rollback-duplicate-evidence", attemptedSource.id, "New note", null, 4L)
            assertThrows(RuntimeException::class.java) {
                db.storeSessionNoteWithEvidence(nextSession, note, attemptedSource, attemptedEvidence)
            }
            assertTrue(db.readSessions().none { it.id == emptySession.id })
            assertTrue(db.readSources().none { it.id == attemptedSource.id })
            assertTrue(db.readSourcesForNote(note.id).isEmpty())
            assertEquals(1, db.readEvidenceForSource(existingSource.id).size)
        } finally { db.close() }
    }
    @Test
    fun sessionsAndNotesUseStableIdOrderingForEqualTimestamps() {
        val sessionZ = Session("session-z", "Z", 100L, listOf(Note("note-z", "Z note", 200L)))
        val sessionA = Session("session-a", "A", 100L, listOf(Note("note-z2", "Z2 note", 200L), Note("note-a", "A note", 200L)))
        val db = LoccoDatabase.create(context)
        try {
            db.replaceSessions(listOf(sessionZ, sessionA))
            assertEquals(listOf("session-a", "session-z"), db.readSessions().map { it.id })
            assertEquals(listOf("note-a", "note-z2"), db.readSessions().first().notes.map { it.id })
        } finally { db.close() }
    }
    private fun containsBytes(haystack: ByteArray, needle: ByteArray): Boolean {
        if (needle.isEmpty() || haystack.size < needle.size) return false
        return (0..haystack.size - needle.size).any { start ->
            needle.indices.all { offset -> haystack[start + offset] == needle[start + offset] }
        }
    }

    private fun clearState() {
        database.delete()
        File(database.path + "-wal").delete()
        File(database.path + "-shm").delete()
        context.deleteSharedPreferences("locco_database_key_v1")
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry("com.locco.app.database.wrap.v1")
        }
    }
}
