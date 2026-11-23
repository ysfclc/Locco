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
    private val appContext = context.applicationContext

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        migrate(db, 0, NativeBridge.databaseSchemaVersion())
        importLegacyPrefs(db, context = appContext)
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
            "created_at DESC"
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
                    "created_at ASC"
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

    /** Stores only a redacted system receipt; notification title/text/extras never cross this boundary. */
    fun recordNotificationObservation(
        id: String,
        dedupeKey: String,
        sourcePackage: String,
        occurredAt: Long,
        receivedAt: Long,
        category: String?
    ) {
        writableDatabase.execSQL(
            "INSERT OR IGNORE INTO app_observations(id,dedupe_key,source_package,event_type,occurred_at,received_at,acquisition_method,category) VALUES(?,?,?,?,?,?,?,?)",
            arrayOf(id, dedupeKey, sourcePackage, "notification_posted", occurredAt, receivedAt, "android_notification_listener", category)
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
        val result = mutableListOf<AppObservation>()
        readableDatabase.rawQuery(
            "SELECT id,source_package,event_type,occurred_at,received_at,acquisition_method,category,redacted_fields_json FROM app_observations ORDER BY received_at DESC LIMIT ?",
            arrayOf(limit.coerceIn(1, 500).toString())
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
                    redactedFields = cursor.getString(7)
                )
            }
        }
        return result
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
            "SELECT id,kind,uri,title,captured_at FROM sources ORDER BY captured_at DESC LIMIT ?",
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
            "SELECT id,source_id,excerpt,content_hash,captured_at FROM evidence WHERE source_id=? ORDER BY captured_at DESC LIMIT ?",
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

    /** Source deletion intentionally cascades to its evidence and note-source links. */
    fun deleteSource(sourceId: String): Boolean {
        require(sourceId.isNotBlank()) { "Source ID is required" }
        return writableDatabase.delete("sources", "id=?", arrayOf(sourceId)) > 0
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
            // Leave legacy preferences untouched for recovery; the encrypted database remains usable.
        }
    }

    companion object {
        fun create(context: Context): LoccoDatabase {
            System.loadLibrary("sqlcipher")
            val key = LoccoDatabaseKey.obtain(context)
            return try {
                LoccoDatabase(context.applicationContext, key)
            } finally {
                key.fill(0)
            }
        }
    }
}
