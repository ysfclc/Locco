package com.locco.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray

/**
 * Android SQLite execution adapter for the Rust-owned Locco kernel schema.
 *
 * Kotlin owns SQLite lifecycle and transactions only. Canonical schema SQL,
 * version sequencing and migration contents are supplied by locco-kernel.
 */
internal class LoccoDatabase(context: Context) : SQLiteOpenHelper(
    context,
    "locco_kernel.db",
    null,
    NativeBridge.databaseSchemaVersion()
) {
    init { setWriteAheadLoggingEnabled(true) }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        migrate(db, 0, NativeBridge.databaseSchemaVersion())
        importLegacyPrefs(db, context = context)
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
            // Leave legacy preferences untouched for recovery; the database remains usable.
        }
    }
}
