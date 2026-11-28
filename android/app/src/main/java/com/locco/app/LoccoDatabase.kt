package com.locco.app

import android.content.Context
import android.database.Cursor
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import org.json.JSONArray

/**
 * SQLCipher storage adapter for the Rust-owned Locco kernel schema.
 * Existing plaintext databases are intentionally blocked until the verified
 * copy-and-verify migration is implemented; they are never opened as empty stores.
 */
internal class LoccoDatabase private constructor(
    context: Context,
    password: ByteArray
) : SQLiteOpenHelper(
    context,
    "locco_kernel.db",
    password,
    null,
    NativeBridge.databaseSchemaVersion(),
    0,
    null,
    null,
    true
) {
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        migrate(db, 0, NativeBridge.databaseSchemaVersion())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        migrate(db, oldVersion, newVersion)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Locco database downgrade is not supported ($oldVersion -> $newVersion)")
    }

    private fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        require(oldVersion <= newVersion) { "Invalid Locco migration range: $oldVersion -> $newVersion" }
        for (version in (oldVersion + 1)..newVersion) {
            NativeBridge.migrationStatements(version).forEach(db::execSQL)
        }
    }

    fun readSessions(): List<Session> {
        val sessions = mutableListOf<Session>()
        readableDatabase.query(
            "sessions",
            arrayOf("id", "title", "created_at"),
            null,
            null,
            null,
            null,
            "created_at DESC,id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val notes = mutableListOf<Note>()
                readableDatabase.query(
                    "notes",
                    arrayOf("id", "content", "created_at"),
                    "session_id=?",
                    arrayOf(id),
                    null,
                    null,
                    "sort_order ASC"
                ).use { noteCursor ->
                    while (noteCursor.moveToNext()) {
                        notes += Note(noteCursor.getString(0), noteCursor.getString(1), noteCursor.getLong(2))
                    }
                }
                sessions += Session(id, cursor.getString(1), cursor.getLong(2), notes)
            }
        }
        return sessions
    }

    /** Atomic replace used by the initial session UI. IDs remain stable and the FK keeps notes scoped. */
    fun replaceSessions(sessions: List<Session>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("sessions", null, null)
            sessions.forEach { session ->
                db.execSQL(
                    "INSERT INTO sessions(id,title,created_at) VALUES(?,?,?)",
                    arrayOf(session.id, session.title, session.time)
                )
                session.notes.forEachIndexed { sortOrder, note ->
                    db.execSQL(
                        "INSERT INTO notes(id,session_id,content,created_at,sort_order) VALUES(?,?,?,?,?)",
                        arrayOf(note.id, session.id, note.text, note.time, sortOrder)
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Create an empty session without replacing any existing user's notes. */
    fun insertSession(session: Session) {
        require(session.notes.isEmpty()) { "New session must not contain notes" }
        writableDatabase.execSQL("INSERT INTO sessions(id,title,created_at) VALUES(?,?,?)",
            arrayOf(session.id, session.title, session.time))
    }

    /** Atomically append a session note and attach its source, evidence and provenance link. */
    fun storeSessionNoteWithEvidence(session: Session, note: Note, source: SourceEntry, evidence: EvidenceEntry) {
        require(session.notes.any { it == note }) { "The note must belong to the supplied session" }
        require(evidence.sourceId == source.id) { "Evidence must reference the supplied source" }
        require(note.text.isNotBlank()) { "Session note cannot be blank" }
        require(evidence.excerpt == note.text) { "Session note must match its linked evidence excerpt" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("INSERT OR IGNORE INTO sessions(id,title,created_at) VALUES(?,?,?)",
                arrayOf(session.id, session.title, session.time))
            db.execSQL("UPDATE sessions SET title=? WHERE id=?", arrayOf(session.title, session.id))
            db.execSQL(
                "INSERT INTO notes(id,session_id,content,created_at,sort_order) " +
                    "SELECT ?,?,?,?,COALESCE(MAX(sort_order), -1)+1 FROM notes WHERE session_id=?",
                arrayOf(note.id, session.id, note.text, note.time, session.id)
            )
            db.execSQL("INSERT INTO sources(id,kind,uri,title,captured_at) VALUES(?,?,?,?,?)",
                arrayOf(source.id, source.kind, source.locator, source.title, source.capturedAt))
            db.execSQL("INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES(?,?,?,?,?)",
                arrayOf(evidence.id, evidence.sourceId, evidence.excerpt, evidence.contentHash, evidence.capturedAt))
            db.execSQL("INSERT INTO note_sources(note_id,source_id,excerpt,attached_at) VALUES(?,?,NULL,?)",
                arrayOf(note.id, source.id, evidence.capturedAt))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    /** Stores a bounded, user-authorized notification excerpt and its explicit redaction record. */
    fun recordNotificationObservation(
        id: String,
        dedupeKey: String,
        sourcePackage: String,
        occurredAt: Long,
        receivedAt: Long,
        category: String?,
        content: CapturedNotificationContent? = null
    ) {
        val redactedFields = JSONArray().apply {
            if (content == null) {
                put("notification_title")
                put("notification_text")
            }
            put("notification_extras")
            put("notification_actions")
            put("notification_images")
            put("notification_pending_intents")
            content?.redactedFields?.sorted()?.forEach(::put)
        }.toString()
        writableDatabase.execSQL(
            "INSERT OR IGNORE INTO app_observations(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method,category,content_title,content_text,content_hash,redacted_fields_json) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            arrayOf(
                id, dedupeKey, sourcePackage, "notification_posted", occurredAt, receivedAt,
                "android_notification_listener", category, content?.title, content?.text,
                content?.contentHash, redactedFields
            )
        )
    }

    /** Persist one bounded UI observation in the separate v8 evidence ledger. */
    fun recordUiObservation(observation: UiObservationRecord) {
        require(observation.eventType in setOf("screen_state_changed", "control_clicked", "container_scrolled"))
        require(observation.sourcePackage.length in 3..255)
        require(observation.dedupeKey.matches(Regex("[0-9a-f]{64}")))
        require(observation.contentHash == null || observation.contentHash.matches(Regex("[0-9a-f]{64}")))
        require(observation.contentText == null ||
            observation.contentText.toByteArray(Charsets.UTF_8).size <= 7_680)
        require(observation.snapshotStatus in setOf("available", "partial", "unavailable"))
        val redactedFields = JSONArray().apply {
            observation.redactedFields.distinct().sorted().forEach(::put)
        }.toString()
        writableDatabase.execSQL(
            "INSERT OR IGNORE INTO app_ui_observations(" +
                "id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method," +
                "screen_class,target_label,target_class,content_text,content_hash,redacted_fields_json,snapshot_status" +
                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            arrayOf(
                observation.id, observation.dedupeKey, observation.sourcePackage, observation.eventType,
                observation.occurredAt, observation.receivedAt, "android_accessibility_service",
                observation.screenClass, observation.targetLabel, observation.targetClass,
                observation.contentText, observation.contentHash, redactedFields, observation.snapshotStatus
            )
        )
    }

    /** Persist package lifecycle transitions without activity or screen details. */
    fun recordUsageObservation(id: String, dedupeKey: String, sourcePackage: String, eventType: String, occurredAt: Long, receivedAt: Long) {
        require(eventType == "app_foregrounded" || eventType == "app_backgrounded")
        writableDatabase.execSQL(
            "INSERT OR IGNORE INTO app_usage_events(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method) VALUES(?,?,?,?,?,?,?)",
            arrayOf(id, dedupeKey, sourcePackage, eventType, occurredAt, receivedAt, "android_usage_events")
        )
    }

    fun readUsageObservations(limit: Int = 100): List<AppObservation> {
        val result = mutableListOf<AppObservation>()
        readableDatabase.rawQuery(
            "SELECT id,source_package,event_type,occurred_at,received_at,acquisition_method FROM app_usage_events ORDER BY received_at DESC LIMIT ?",
            arrayOf(limit.coerceIn(1, 500).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += AppObservation(
                    id = cursor.getString(0), sourcePackage = cursor.getString(1),
                    eventType = cursor.getString(2), occurredAt = cursor.getLong(3),
                    receivedAt = cursor.getLong(4), acquisitionMethod = cursor.getString(5),
                    category = null, redactedFields = "[]"
                )
            }
        }
        return result
    }

    fun readObservations(limit: Int = 100): List<AppObservation> {
        val pageSize = limit.coerceIn(1, 500)
        val result = mutableListOf<AppObservation>()
        readableDatabase.rawQuery(
            "SELECT id,source_package,event_type,occurred_at,received_at,acquisition_method,category,redacted_fields_json,content_title,content_text,content_hash " +
                "FROM app_observations ORDER BY occurred_at DESC,received_at DESC,id ASC LIMIT ?",
            arrayOf(pageSize.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += AppObservation(
                    id = cursor.getString(0),
                    sourcePackage = cursor.getString(1),
                    eventType = cursor.getString(2),
                    occurredAt = cursor.getLong(3),
                    receivedAt = cursor.getLong(4),
                    acquisitionMethod = cursor.getString(5),
                    category = if (cursor.isNull(6)) null else cursor.getString(6),
                    redactedFields = cursor.getString(7),
                    contentTitle = if (cursor.isNull(8)) null else cursor.getString(8),
                    contentText = if (cursor.isNull(9)) null else cursor.getString(9),
                    contentHash = if (cursor.isNull(10)) null else cursor.getString(10)
                )
            }
        }
        readableDatabase.rawQuery(
            "SELECT id,source_package,event_type,occurred_at,received_at,acquisition_method,screen_class," +
                "target_label,target_class,content_text,content_hash,redacted_fields_json,snapshot_status " +
                "FROM app_ui_observations ORDER BY occurred_at DESC,received_at DESC,id ASC LIMIT ?",
            arrayOf(pageSize.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += AppObservation(
                    id = cursor.getString(0),
                    sourcePackage = cursor.getString(1),
                    eventType = cursor.getString(2),
                    occurredAt = cursor.getLong(3),
                    receivedAt = cursor.getLong(4),
                    acquisitionMethod = cursor.getString(5),
                    category = null,
                    redactedFields = cursor.getString(11),
                    contentText = if (cursor.isNull(9)) null else cursor.getString(9),
                    contentHash = if (cursor.isNull(10)) null else cursor.getString(10),
                    screenClass = if (cursor.isNull(6)) null else cursor.getString(6),
                    targetLabel = if (cursor.isNull(7)) null else cursor.getString(7),
                    targetClass = if (cursor.isNull(8)) null else cursor.getString(8),
                    snapshotStatus = cursor.getString(12)
                )
            }
        }
        return result
            .distinctBy { "${it.acquisitionMethod}:${it.id}" }
            .sortedWith(
                compareByDescending<AppObservation> { it.occurredAt }
                    .thenByDescending { it.receivedAt }
                    .thenBy { it.id }
            )
            .take(pageSize)
    }

    /** Insert a source and its first evidence as one indivisible capture operation. */
    fun storeSourceWithEvidence(source: SourceEntry, evidence: EvidenceEntry) {
        require(evidence.sourceId == source.id) { "Evidence must reference the supplied source" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL(
                "INSERT INTO sources(id,kind,uri,title,captured_at) VALUES(?,?,?,?,?)",
                arrayOf(source.id, source.kind, source.locator, source.title, source.capturedAt)
            )
            db.execSQL(
                "INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES(?,?,?,?,?)",
                arrayOf(evidence.id, evidence.sourceId, evidence.excerpt, evidence.contentHash, evidence.capturedAt)
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Persist a canonical source. Duplicate IDs fail rather than silently overwrite provenance. */
    fun insertSource(source: SourceEntry) {
        writableDatabase.execSQL(
            "INSERT INTO sources(id,kind,uri,title,captured_at) VALUES(?,?,?,?,?)",
            arrayOf(source.id, source.kind, source.locator, source.title, source.capturedAt)
        )
    }

    /** Persist immutable evidence. The foreign key rejects evidence without a known source. */
    fun insertEvidence(evidence: EvidenceEntry) {
        writableDatabase.execSQL(
            "INSERT INTO evidence(id,source_id,excerpt,content_hash,captured_at) VALUES(?,?,?,?,?)",
            arrayOf(evidence.id, evidence.sourceId, evidence.excerpt, evidence.contentHash, evidence.capturedAt)
        )
    }

    fun readSources(limit: Int = 100): List<SourceEntry> {
        val result = mutableListOf<SourceEntry>()
        readableDatabase.rawQuery(
            "SELECT id,kind,uri,title,captured_at FROM sources ORDER BY captured_at DESC,id ASC LIMIT ?",
            arrayOf(limit.coerceIn(1, 500).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += SourceEntry(
                    id = cursor.getString(0),
                    kind = cursor.getString(1),
                    locator = if (cursor.isNull(2)) null else cursor.getString(2),
                    title = if (cursor.isNull(3)) null else cursor.getString(3),
                    capturedAt = cursor.getLong(4)
                )
            }
        }
        return result
    }

    fun readEvidenceForSource(sourceId: String, limit: Int = 100): List<EvidenceEntry> {
        require(sourceId.isNotBlank()) { "Source ID is required" }
        val result = mutableListOf<EvidenceEntry>()
        readableDatabase.rawQuery(
            "SELECT id,source_id,excerpt,content_hash,captured_at FROM evidence WHERE source_id=? ORDER BY captured_at DESC,id ASC LIMIT ?",
            arrayOf(sourceId, limit.coerceIn(1, 500).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += EvidenceEntry(
                    id = cursor.getString(0),
                    sourceId = cursor.getString(1),
                    excerpt = if (cursor.isNull(2)) null else cursor.getString(2),
                    contentHash = if (cursor.isNull(3)) null else cursor.getString(3),
                    capturedAt = cursor.getLong(4)
                )
            }
        }
        return result
    }

    /** Read source provenance attached to one session note. */
    fun readSourcesForNote(noteId: String): List<SourceEntry> {
        require(noteId.isNotBlank()) { "Note ID is required" }
        val result = mutableListOf<SourceEntry>()
        readableDatabase.rawQuery(
            "SELECT s.id,s.kind,s.uri,s.title,s.captured_at FROM sources s " +
                "INNER JOIN note_sources ns ON ns.source_id=s.id WHERE ns.note_id=? " +
                "ORDER BY ns.attached_at DESC,s.id ASC",
            arrayOf(noteId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += SourceEntry(
                    id = cursor.getString(0), kind = cursor.getString(1),
                    locator = if (cursor.isNull(2)) null else cursor.getString(2),
                    title = if (cursor.isNull(3)) null else cursor.getString(3),
                    capturedAt = cursor.getLong(4)
                )
            }
        }
        return result
    }
    /** Read the global evidence stream in stable descending-time/ascending-ID order. */
    fun readTimelinePage(limit: Int = 100, cursor: TimelineCursor? = null): TimelinePage {
        val pageLimit = limit.coerceIn(1, 500)
        val result = mutableListOf<TimelineEntry>()
        val sql = buildString {
            append("SELECT e.id,e.source_id,e.excerpt,e.content_hash,e.captured_at,")
            append("s.kind,s.uri,s.title,s.captured_at ")
            append("FROM evidence e INNER JOIN sources s ON s.id=e.source_id ")
            if (cursor != null) append("WHERE (e.captured_at < ? OR (e.captured_at = ? AND e.id > ?)) ")
            append("ORDER BY e.captured_at DESC,e.id ASC LIMIT ?")
        }
        val args = if (cursor == null) arrayOf((pageLimit + 1).toString()) else arrayOf(
            cursor.capturedAt.toString(), cursor.capturedAt.toString(), cursor.evidenceId,
            (pageLimit + 1).toString()
        )
        readableDatabase.rawQuery(sql, args).use { rows ->
            while (rows.moveToNext()) {
                result += TimelineEntry(
                    evidence = EvidenceEntry(
                        id = rows.getString(0), sourceId = rows.getString(1),
                        excerpt = if (rows.isNull(2)) null else rows.getString(2),
                        contentHash = if (rows.isNull(3)) null else rows.getString(3),
                        capturedAt = rows.getLong(4)
                    ),
                    source = SourceEntry(
                        id = rows.getString(1), kind = rows.getString(5),
                        locator = if (rows.isNull(6)) null else rows.getString(6),
                        title = if (rows.isNull(7)) null else rows.getString(7),
                        capturedAt = rows.getLong(8)
                    )
                )
            }
        }
        val hasMore = result.size > pageLimit
        val page = if (hasMore) result.take(pageLimit) else result
        val next = if (hasMore) page.lastOrNull()?.let { TimelineCursor(it.evidence.capturedAt, it.evidence.id) } else null
        return TimelinePage(page, next)
    }


    /** Source deletion intentionally cascades to its evidence and note-source links. */
    fun deleteSource(sourceId: String): Boolean {
        require(sourceId.isNotBlank()) { "Source ID is required" }
        return writableDatabase.delete("sources", "id=?", arrayOf(sourceId)) > 0
    }

    /**
     * Idempotently imports the legacy session preference on every database open.
     * The plaintext key is removed only after the encrypted rows have been verified.
     * A failed import or failed preference commit leaves the original JSON available for retry.
     */
    private fun importLegacyPrefs(context: Context) {
        val prefs = context.getSharedPreferences("locco_core_v1", Context.MODE_PRIVATE)
        if (!prefs.contains("sessions")) return
        val json = prefs.getString("sessions", null)
            ?: throw IllegalStateException("Legacy Locco sessions are not a readable string; the original data was preserved.")

        val legacySessions: List<Session> = try {
            val array = JSONArray(json)
            val parsedSessions = mutableListOf<Session>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val id = item.getString("id")
                val notesJson = when {
                    !item.has("notes") || item.isNull("notes") -> JSONArray()
                    else -> item.optJSONArray("notes")
                        ?: throw IllegalArgumentException("Legacy session notes must be an array.")
                }
                val parsedNotes = mutableListOf<Note>()
                for (j in 0 until notesJson.length()) {
                    val note = notesJson.getJSONObject(j)
                    parsedNotes.add(Note(note.getString("id"), note.getString("text"), note.optLong("time")))
                }
                parsedSessions.add(Session(id, item.optString("title", "Session"), item.optLong("time"), parsedNotes))
            }
            parsedSessions
        } catch (failure: Exception) {
            throw IllegalStateException(
                "Locco could not read legacy sessions. The original preference data was preserved.",
                failure
            )
        }

        val sessionIds = legacySessions.map { it.id }
        val noteIds = legacySessions.flatMap { it.notes }.map { it.id }
        check(sessionIds.distinct().size == sessionIds.size && noteIds.distinct().size == noteIds.size) {
            "Legacy Locco sessions contain duplicate IDs; the original preference data was preserved."
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            legacySessions.forEach { session ->
                db.execSQL(
                    "INSERT OR IGNORE INTO sessions(id,title,created_at) VALUES(?,?,?)",
                    arrayOf(session.id, session.title, session.time)
                )
                session.notes.forEachIndexed { index, note ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO notes(id,session_id,content,created_at,sort_order) VALUES(?,?,?,?,?)",
                        arrayOf(note.id, session.id, note.text, note.time, index)
                    )
                }
            }

            // Verify every legacy record before committing. Existing session titles may have
            // legitimately changed since an earlier import, so verify identity and note payloads.
            legacySessions.forEach { session ->
                db.query("sessions", arrayOf("id", "created_at"), "id=?", arrayOf(session.id), null, null, null).use { rows ->
                    check(rows.moveToFirst() && rows.getString(0) == session.id && rows.getLong(1) == session.time) {
                        "Legacy session ${session.id} failed encrypted-store verification."
                    }
                }
                session.notes.forEach { note ->
                    db.query(
                        "notes", arrayOf("session_id", "content", "created_at"),
                        "id=?", arrayOf(note.id), null, null, null
                    ).use { rows ->
                        check(rows.moveToFirst() &&
                            rows.getString(0) == session.id &&
                            rows.getString(1) == note.text &&
                            rows.getLong(2) == note.time
                        ) { "Legacy note ${note.id} failed encrypted-store verification." }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        check(prefs.edit().remove("sessions").commit()) {
            "Locco imported legacy sessions but could not clear the plaintext preference copy; it will retry safely."
        }
    }

    companion object {
        fun create(context: Context): LoccoDatabase {
            System.loadLibrary("sqlcipher")
            val app = context.applicationContext
            val key = LoccoDatabaseKey.obtainForDatabaseOpen(app)
            try {
                // Migrate a recognized v2 plaintext database before SQLiteOpenHelper can create
                // or open the encrypted store. The source is retained until the candidate passes
                // row digests, foreign-key checks, integrity_check, and encrypted reopen.
                LegacyPlaintextDatabaseMigrator.migrateIfNeeded(app, key)
                val database = LoccoDatabase(app, key)
                return try {
                    database.writableDatabase
                    database.importLegacyPrefs(app)
                    database
                } catch (failure: Throwable) {
                    database.close()
                    throw failure
                }
            } finally {
                key.fill(0)
            }
        }
    }
}
