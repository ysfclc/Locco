package com.locco.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared, offline-first Locco kernel storage.
 * The session UI is only the first consumer; acquisition, entities and evidence
 * share the same database instead of owning feature-specific stores.
 */
internal class LoccoDatabase(context: Context) : SQLiteOpenHelper(context, "locco_kernel.db", null, 1) {
    init { setWriteAheadLoggingEnabled(true) }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
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
        importLegacyPrefs(db, context)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future versions must add explicit, sequential migrations. Never drop user data here.
    }

    fun readSessions(): List<Session> {
        val sessions = mutableListOf<Session>()
        readableDatabase.query("sessions", arrayOf("id", "title", "created_at"), null, null, null, null, "created_at DESC").use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val notes = mutableListOf<Note>()
                readableDatabase.query("notes", arrayOf("id", "content", "created_at"), "session_id=?", arrayOf(id), null, null, "created_at ASC").use { noteCursor ->
                    while (noteCursor.moveToNext()) notes += Note(noteCursor.getString(0), noteCursor.getString(1), noteCursor.getLong(2))
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
                db.execSQL("INSERT INTO sessions(id,title,created_at) VALUES(?,?,?)", arrayOf(session.id, session.title, session.time))
                session.notes.forEach { note ->
                    db.execSQL("INSERT INTO notes(id,session_id,content,created_at) VALUES(?,?,?,?)", arrayOf(note.id, session.id, note.text, note.time))
                }
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    private fun importLegacyPrefs(db: SQLiteDatabase, context: Context) {
        val prefs = context.getSharedPreferences("locco_core_v1", Context.MODE_PRIVATE)
        val json = prefs.getString("sessions", "[]") ?: "[]"
        runCatching {
            val arr = JSONArray(json)
            db.beginTransaction()
            try {
                for (i in 0 until arr.length()) {
                    val s = arr.getJSONObject(i)
                    db.execSQL("INSERT OR IGNORE INTO sessions(id,title,created_at) VALUES(?,?,?)", arrayOf(s.getString("id"), s.optString("title", "Session"), s.optLong("time")))
                    val notes = s.optJSONArray("notes") ?: JSONArray()
                    for (j in 0 until notes.length()) {
                        val n = notes.getJSONObject(j)
                        db.execSQL("INSERT OR IGNORE INTO notes(id,session_id,content,created_at) VALUES(?,?,?,?)", arrayOf(n.getString("id"), s.getString("id"), n.getString("text"), n.optLong("time")))
                    }
                }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
        }.onFailure {
            // Leave legacy preferences untouched for recovery; the database is still usable.
        }
    }
}
