package com.locco.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

internal data class SourceRecord(
    val id: String,
    val kind: String,
    val uri: String?,
    val title: String?,
    val capturedAt: Long,
    val metadataJson: String
)

internal data class NoteSourceEvidence(
    val noteId: String,
    val source: SourceRecord,
    val excerpt: String?,
    val attachedAt: Long
)

internal data class EntityRecord(
    val id: String,
    val kind: String,
    val canonicalName: String,
    val description: String?,
    val createdAt: Long,
    val metadataJson: String
)

internal data class EntityRelation(
    val id: String,
    val fromEntityId: String,
    val relationType: String,
    val toEntityId: String,
    val createdAt: Long,
    val metadataJson: String
)

internal data class KernelEvent(
    val id: String,
    val eventType: String,
    val occurredAt: Long,
    val payloadJson: String
)

/**
 * Shared, offline-first Locco kernel storage.
 * Source evidence, entities and events live beside sessions rather than in
 * feature-specific stores. Imported content is stored as data, never executed.
 */
internal class LoccoDatabase(context: Context) :
    SQLiteOpenHelper(context, "locco_kernel.db", null, DATABASE_VERSION) {

    init { setWriteAheadLoggingEnabled(true) }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createBaseSchema(db)
        createEvidenceSchema(db)
        importLegacyPrefs(db, context)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var version = oldVersion
        if (version < 2) {
            createEvidenceSchema(db)
            version = 2
        }
        // Future upgrades must be explicit and sequential. Never drop user data.
        check(version == newVersion) { "Missing Locco database migration: $version -> $newVersion" }
    }

    private fun createBaseSchema(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE sessions (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            created_at INTEGER NOT NULL
        )""")
        db.execSQL("""CREATE TABLE notes (
            id TEXT PRIMARY KEY NOT NULL,
            session_id TEXT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
            content TEXT NOT NULL,
            created_at INTEGER NOT NULL
        )""")
        db.execSQL("CREATE INDEX notes_session_time ON notes(session_id, created_at)")
        db.execSQL("""CREATE TABLE sources (
            id TEXT PRIMARY KEY NOT NULL,
            kind TEXT NOT NULL,
            uri TEXT,
            title TEXT,
            captured_at INTEGER NOT NULL,
            metadata_json TEXT NOT NULL DEFAULT '{}'
        )""")
        db.execSQL("""CREATE TABLE entities (
            id TEXT PRIMARY KEY NOT NULL,
            kind TEXT NOT NULL,
            canonical_name TEXT NOT NULL,
            description TEXT,
            created_at INTEGER NOT NULL,
            metadata_json TEXT NOT NULL DEFAULT '{}'
        )""")
        db.execSQL("""CREATE TABLE entity_relations (
            id TEXT PRIMARY KEY NOT NULL,
            from_entity_id TEXT NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
            relation_type TEXT NOT NULL,
            to_entity_id TEXT NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
            created_at INTEGER NOT NULL,
            metadata_json TEXT NOT NULL DEFAULT '{}'
        )""")
        db.execSQL("CREATE INDEX entity_relations_from ON entity_relations(from_entity_id)")
        db.execSQL("CREATE INDEX entity_relations_to ON entity_relations(to_entity_id)")
        db.execSQL("""CREATE TABLE kernel_events (
            id TEXT PRIMARY KEY NOT NULL,
            event_type TEXT NOT NULL,
            occurred_at INTEGER NOT NULL,
            payload_json TEXT NOT NULL DEFAULT '{}'
        )""")
    }

    /** Version 2: preserve the source excerpt and attachment time for each note/source pair. */
    private fun createEvidenceSchema(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS note_sources (
            note_id TEXT NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
            source_id TEXT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
            excerpt TEXT,
            attached_at INTEGER NOT NULL,
            PRIMARY KEY(note_id, source_id)
        )""")
        db.execSQL("CREATE INDEX IF NOT EXISTS note_sources_source ON note_sources(source_id, attached_at)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS entity_relation_identity ON entity_relations(from_entity_id, relation_type, to_entity_id)")
    }

    fun readSessions(): List<Session> {
        val sessions = mutableListOf<Session>()
        readableDatabase.query(
            "sessions", arrayOf("id", "title", "created_at"),
            null, null, null, null, "created_at DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val notes = mutableListOf<Note>()
                readableDatabase.query(
                    "notes", arrayOf("id", "content", "created_at"),
                    "session_id=?", arrayOf(id), null, null, "created_at ASC"
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

    /** Atomic replacement for the initial session UI. */
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
                session.notes.forEach { note ->
                    db.execSQL(
                        "INSERT INTO notes(id,session_id,content,created_at) VALUES(?,?,?,?)",
                        arrayOf(note.id, session.id, note.text, note.time)
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Insert or update a source without replacing its row (and cascading away evidence links). */
    fun upsertSource(source: SourceRecord) {
        require(source.id.isNotBlank() && source.kind.isNotBlank())
        val metadata = normalizedJsonObject(source.metadataJson)
        val values = ContentValues().apply {
            put("kind", source.kind)
            put("uri", source.uri)
            put("title", source.title)
            put("captured_at", source.capturedAt)
            put("metadata_json", metadata)
        }
        val db = writableDatabase
        if (db.update("sources", values, "id=?", arrayOf(source.id)) == 0) {
            values.put("id", source.id)
            db.insertOrThrow("sources", null, values)
        }
    }

    fun readSource(sourceId: String): SourceRecord? {
        readableDatabase.query(
            "sources",
            arrayOf("id", "kind", "uri", "title", "captured_at", "metadata_json"),
            "id=?", arrayOf(sourceId), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return sourceFromCursor(cursor)
        }
    }

    /** Attach evidence idempotently. Reattaching updates excerpt/time, not the source or note. */
    fun attachSourceToNote(noteId: String, sourceId: String, excerpt: String?, attachedAt: Long) {
        val values = ContentValues().apply {
            put("note_id", noteId)
            put("source_id", sourceId)
            put("excerpt", excerpt)
            put("attached_at", attachedAt)
        }
        writableDatabase.insertWithOnConflict("note_sources", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun readSourcesForNote(noteId: String): List<NoteSourceEvidence> {
        val result = mutableListOf<NoteSourceEvidence>()
        val sql = """SELECT ns.note_id, ns.excerpt, ns.attached_at,
            s.id, s.kind, s.uri, s.title, s.captured_at, s.metadata_json
            FROM note_sources ns JOIN sources s ON s.id=ns.source_id
            WHERE ns.note_id=? ORDER BY ns.attached_at ASC"""
        readableDatabase.rawQuery(sql, arrayOf(noteId)).use { cursor ->
            while (cursor.moveToNext()) {
                result += NoteSourceEvidence(
                    cursor.getString(0),
                    SourceRecord(
                        cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6),
                        cursor.getLong(7), cursor.getString(8)
                    ),
                    cursor.getString(1),
                    cursor.getLong(2)
                )
            }
        }
        return result
    }

    /** Update in place to avoid SQLite REPLACE's delete-and-cascade semantics. */
    fun upsertEntity(entity: EntityRecord) {
        require(entity.id.isNotBlank() && entity.kind.isNotBlank() && entity.canonicalName.isNotBlank())
        val values = ContentValues().apply {
            put("kind", entity.kind)
            put("canonical_name", entity.canonicalName)
            put("description", entity.description)
            put("created_at", entity.createdAt)
            put("metadata_json", normalizedJsonObject(entity.metadataJson))
        }
        val db = writableDatabase
        if (db.update("entities", values, "id=?", arrayOf(entity.id)) == 0) {
            values.put("id", entity.id)
            db.insertOrThrow("entities", null, values)
        }
    }

    fun readEntity(entityId: String): EntityRecord? {
        readableDatabase.query(
            "entities",
            arrayOf("id", "kind", "canonical_name", "description", "created_at", "metadata_json"),
            "id=?", arrayOf(entityId), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return EntityRecord(
                cursor.getString(0), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getLong(4), cursor.getString(5)
            )
        }
    }

    /** Returns false for an already-existing relationship; unknown entities fail via FK. */
    fun createRelation(relation: EntityRelation): Boolean {
        require(relation.id.isNotBlank() && relation.relationType.isNotBlank())
        val values = ContentValues().apply {
            put("id", relation.id)
            put("from_entity_id", relation.fromEntityId)
            put("relation_type", relation.relationType)
            put("to_entity_id", relation.toEntityId)
            put("created_at", relation.createdAt)
            put("metadata_json", normalizedJsonObject(relation.metadataJson))
        }
        return writableDatabase.insertWithOnConflict(
            "entity_relations", null, values, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    fun readRelationsForEntity(entityId: String): List<EntityRelation> {
        val result = mutableListOf<EntityRelation>()
        readableDatabase.query(
            "entity_relations",
            arrayOf("id", "from_entity_id", "relation_type", "to_entity_id", "created_at", "metadata_json"),
            "from_entity_id=? OR to_entity_id=?",
            arrayOf(entityId, entityId), null, null, "created_at ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += EntityRelation(
                    cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getLong(4), cursor.getString(5)
                )
            }
        }
        return result
    }

    fun appendEvent(event: KernelEvent) {
        require(event.id.isNotBlank() && event.eventType.isNotBlank())
        val values = ContentValues().apply {
            put("id", event.id)
            put("event_type", event.eventType)
            put("occurred_at", event.occurredAt)
            put("payload_json", normalizedJsonObject(event.payloadJson))
        }
        writableDatabase.insertOrThrow("kernel_events", null, values)
    }

    fun readRecentEvents(limit: Int = 100): List<KernelEvent> {
        require(limit in 1..1000)
        val result = mutableListOf<KernelEvent>()
        readableDatabase.query(
            "kernel_events",
            arrayOf("id", "event_type", "occurred_at", "payload_json"),
            null, null, null, null, "occurred_at DESC", limit.toString()
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += KernelEvent(
                    cursor.getString(0), cursor.getString(1),
                    cursor.getLong(2), cursor.getString(3)
                )
            }
        }
        return result
    }

    private fun sourceFromCursor(cursor: android.database.Cursor) = SourceRecord(
        cursor.getString(0), cursor.getString(1), cursor.getString(2),
        cursor.getString(3), cursor.getLong(4), cursor.getString(5)
    )

    private fun normalizedJsonObject(raw: String): String {
        val parsed = JSONObject(raw)
        return parsed.toString()
    }

    private fun importLegacyPrefs(db: SQLiteDatabase, context: Context) {
        val prefs = context.getSharedPreferences("locco_core_v1", Context.MODE_PRIVATE)
        val json = prefs.getString("sessions", "[]") ?: "[]"
        runCatching {
            val arr = JSONArray(json)
            db.beginTransaction()
            try {
                for (i in 0 until arr.length()) {
                    val session = arr.getJSONObject(i)
                    db.execSQL(
                        "INSERT OR IGNORE INTO sessions(id,title,created_at) VALUES(?,?,?)",
                        arrayOf(session.getString("id"), session.optString("title", "Session"), session.optLong("time"))
                    )
                    val notes = session.optJSONArray("notes") ?: JSONArray()
                    for (j in 0 until notes.length()) {
                        val note = notes.getJSONObject(j)
                        db.execSQL(
                            "INSERT OR IGNORE INTO notes(id,session_id,content,created_at) VALUES(?,?,?,?)",
                            arrayOf(note.getString("id"), session.getString("id"), note.getString("text"), note.optLong("time"))
                        )
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }.onFailure {
            // Keep legacy preferences untouched for recovery; the database remains usable.
        }
    }

    companion object {
        private const val DATABASE_VERSION = 2
    }
}
