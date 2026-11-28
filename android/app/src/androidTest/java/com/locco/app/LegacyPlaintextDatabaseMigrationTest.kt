package com.locco.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase as FrameworkSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LegacyPlaintextDatabaseMigrationTest {
    private lateinit var context: Context
    private lateinit var database: File
    private lateinit var backup: File
    private lateinit var temp: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = context.getDatabasePath("locco_kernel.db")
        backup = File(database.parentFile, "locco_kernel.db.plaintext-backup")
        temp = File(database.parentFile, "locco_kernel.db.migrating")
        clearState()
    }

    @After
    fun tearDown() = clearState()

    @Test
    fun migratesEveryV2TableIntoVerifiedEncryptedSchemaV8() {
        createLegacyV2Fixture()
        val sourceBytes = database.readBytes()

        val migrated = LoccoDatabase.create(context)
        try {
            assertEquals(
                listOf(Session("legacy-session", "Legacy session", 100L,
                    listOf(Note("legacy-note", "preserve this note", 101L)))),
                migrated.readSessions()
            )
            assertEquals("legacy-source", migrated.readSourcesForNote("legacy-note").single().id)
        } finally {
            migrated.close()
        }

        assertTrue(database.exists())
        assertFalse(database.readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)))
        assertFalse(backup.exists())
        assertFalse(temp.exists())
        assertTrue(sourceBytes.isNotEmpty())

        val key = LoccoDatabaseKey.obtain(context)
        try {
            val encrypted = CipherSQLiteDatabase.openDatabase(
                database.absolutePath, key, null, CipherSQLiteDatabase.OPEN_READONLY, null
            )
            try {
                assertEquals(8, encrypted.rawQuery("PRAGMA user_version", null).use {
                    check(it.moveToFirst()); it.getInt(0)
                })
                mapOf(
                    "sessions" to 1, "notes" to 1, "sources" to 1, "entities" to 2,
                    "entity_relations" to 1, "kernel_events" to 1, "note_sources" to 1
                ).forEach { (table, count) ->
                    assertEquals(count, encrypted.rawQuery("SELECT COUNT(*) FROM $table", null).use {
                        check(it.moveToFirst()); it.getInt(0)
                    })
                }
                assertEquals("ok", encrypted.rawQuery("PRAGMA integrity_check", null).use {
                    check(it.moveToFirst()); it.getString(0)
                })
            } finally {
                encrypted.close()
            }
        } finally {
            key.fill(0)
        }
    }

    @Test
    fun unsupportedPlaintextVersionIsPreservedByteForByte() {
        database.parentFile?.mkdirs()
        val legacy = FrameworkSQLiteDatabase.openOrCreateDatabase(database, null)
        legacy.version = 99
        legacy.close()
        val original = database.readBytes()

        assertThrows(IllegalStateException::class.java) {
            LoccoDatabase.create(context)
        }
        assertTrue(database.exists())
        assertTrue(original.contentEquals(database.readBytes()))
        assertFalse(backup.exists())
    }

    @Test
    fun unknownPlaintextTableIsRejectedWithoutChangingOriginalDatabase() {
        createLegacyV2Fixture()
        val legacy = FrameworkSQLiteDatabase.openDatabase(
            database.absolutePath, null, FrameworkSQLiteDatabase.OPEN_READWRITE
        )
        try {
            legacy.execSQL("CREATE TABLE unexpected_private_data(id TEXT PRIMARY KEY, payload TEXT)")
            legacy.execSQL("INSERT INTO unexpected_private_data(id, payload) VALUES('keep-me', 'do not discard')")
        } finally {
            legacy.close()
        }
        val original = database.readBytes()

        assertThrows(IllegalStateException::class.java) {
            LoccoDatabase.create(context)
        }

        assertTrue(database.exists())
        assertTrue("Unknown-table rejection must preserve every original byte",
            original.contentEquals(database.readBytes()))
        assertFalse(backup.exists())
        assertFalse(temp.exists())
    }

    private fun createLegacyV2Fixture() {
        database.parentFile?.mkdirs()
        val legacy = FrameworkSQLiteDatabase.openOrCreateDatabase(database, null)
        try {
            for (version in 1..2) NativeBridge.migrationStatements(version).forEach(legacy::execSQL)
            legacy.execSQL("INSERT INTO sessions(id,title,created_at) VALUES('legacy-session','Legacy session',100)")
            legacy.execSQL("INSERT INTO notes(id,session_id,content,created_at) VALUES('legacy-note','legacy-session','preserve this note',101)")
            legacy.execSQL("INSERT INTO sources(id,kind,uri,title,captured_at,metadata_json) VALUES('legacy-source','web_page','https://example.test','Legacy source',102,'{}')")
            legacy.execSQL("INSERT INTO entities(id,kind,canonical_name,description,created_at,metadata_json) VALUES('entity-a','topic','A',NULL,103,'{}')")
            legacy.execSQL("INSERT INTO entities(id,kind,canonical_name,description,created_at,metadata_json) VALUES('entity-b','topic','B',NULL,104,'{}')")
            legacy.execSQL("INSERT INTO entity_relations(id,from_entity_id,relation_type,to_entity_id,created_at,metadata_json) VALUES('relation-a-b','entity-a','related_to','entity-b',105,'{}')")
            legacy.execSQL("INSERT INTO kernel_events(id,event_type,occurred_at,payload_json) VALUES('event-legacy','capture',106,'{}')")
            legacy.execSQL("INSERT INTO note_sources(note_id,source_id,excerpt,attached_at) VALUES('legacy-note','legacy-source','excerpt',107)")
            legacy.version = 2
        } finally {
            legacy.close()
        }
    }

    private fun clearState() {
        listOf(database, backup, temp, File(database.path + "-wal"),
            File(database.path + "-shm"), File(database.path + "-journal")).forEach { it.delete() }
        context.getSharedPreferences("locco_database_key_v1", Context.MODE_PRIVATE)
            .edit().clear().commit()
        runCatching {
            java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .deleteEntry("com.locco.app.database.wrap.v1")
        }
    }
}
