package com.locco.app

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase as FrameworkSQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * One-way, fail-closed migration of the documented v2 framework-SQLite store.
 *
 * The source is never opened for writing. A same-directory encrypted candidate is
 * built, row-verified, integrity-checked, and reopened before the source is renamed.
 * Source sidecars are rejected because silently discarding a WAL could lose committed data.
 */
internal object LegacyPlaintextDatabaseMigrator {
    private const val DATABASE_NAME = "locco_kernel.db"
    private const val LEGACY_VERSION = 2
    private const val CURRENT_VERSION = 6

    private data class Table(
        val name: String,
        val columns: List<String>,
        val orderBy: String
    )

    private val legacyTables = listOf(
        Table("sessions", listOf("id", "title", "created_at"), "id"),
        Table("notes", listOf("id", "session_id", "content", "created_at"), "id"),
        Table("sources", listOf("id", "kind", "uri", "title", "captured_at", "metadata_json"), "id"),
        Table("entities", listOf("id", "kind", "canonical_name", "description", "created_at", "metadata_json"), "id"),
        Table("entity_relations", listOf("id", "from_entity_id", "relation_type", "to_entity_id", "created_at", "metadata_json"), "id"),
        Table("kernel_events", listOf("id", "event_type", "occurred_at", "payload_json"), "id"),
        Table("note_sources", listOf("note_id", "source_id", "excerpt", "attached_at"), "note_id,source_id")
    )

    @Synchronized
    fun migrateIfNeeded(context: Context, key: ByteArray) {
        val main = context.applicationContext.getDatabasePath(DATABASE_NAME)
        val temp = File(main.parentFile, "$DATABASE_NAME.migrating")
        val backup = File(main.parentFile, "$DATABASE_NAME.plaintext-backup")
        main.parentFile?.mkdirs()

        recoverInterruptedSwap(main, temp, backup, key)
        if (!main.exists()) {
            check(!temp.exists()) { "An orphaned Locco migration candidate exists; refusing to create a new database." }
            return
        }
        if (!isPlaintextSQLite(main)) {
            check(!backup.exists()) { "Locco has an ambiguous database backup; existing files were preserved." }
            return
        }
        check(!backup.exists()) {
            "Locco found both a plaintext database and a migration backup. Both files were preserved for recovery."
        }
        requireNoSidecars(main)
        requireNoSidecars(temp)
        if (temp.exists()) check(temp.delete()) { "Unable to remove an incomplete Locco migration candidate." }

        val source = FrameworkSQLiteDatabase.openDatabase(
            main.absolutePath,
            null,
            FrameworkSQLiteDatabase.OPEN_READONLY
        )
        try {
            validateLegacySource(source)
            buildEncryptedCandidate(source, temp, key)
        } finally {
            source.close()
        }

        check(main.renameTo(backup)) {
            "Unable to preserve the original Locco database before migration."
        }
        if (!temp.renameTo(main)) {
            check(backup.renameTo(main)) {
                "Locco migration swap failed and the original database could not be restored; backup remains at ${backup.absolutePath}."
            }
            throw IllegalStateException("Unable to install the verified encrypted Locco database; original data was restored.")
        }

        try {
            verifyEncryptedDatabase(main, key)
        } catch (failure: Throwable) {
            main.delete()
            check(backup.renameTo(main)) {
                "Encrypted Locco database verification failed and original database restoration also failed; backup remains at ${backup.absolutePath}."
            }
            throw IllegalStateException("Encrypted Locco database verification failed; original database was restored.", failure)
        }

        check(backup.delete()) {
            "Locco migrated successfully but could not remove the preserved plaintext backup. Restart to retry cleanup."
        }
    }

    private fun recoverInterruptedSwap(main: File, temp: File, backup: File, key: ByteArray) {
        if (backup.exists()) {
            requireNoSidecars(backup)
            requireNoSidecars(temp)
            if (main.exists()) {
                if (isPlaintextSQLite(main)) {
                    throw IllegalStateException(
                        "Locco found an interrupted migration with both original and backup files. Both were preserved."
                    )
                }
                verifyEncryptedDatabase(main, key)
                check(backup.delete()) { "Unable to clean up the verified plaintext Locco backup." }
                if (temp.exists()) check(temp.delete()) { "Unable to remove the stale Locco migration candidate." }
                return
            }

            if (temp.exists() && runCatching { verifyEncryptedDatabase(temp, key) }.isSuccess) {
                check(temp.renameTo(main)) { "Unable to complete the encrypted Locco database swap." }
                try {
                    verifyEncryptedDatabase(main, key)
                } catch (failure: Throwable) {
                    main.delete()
                    check(backup.renameTo(main)) { "Locco encrypted candidate failed recovery; original backup remains." }
                    throw failure
                }
                check(backup.delete()) { "Unable to remove the recovered plaintext Locco backup." }
                return
            }

            if (temp.exists()) {
                check(temp.delete()) { "Unable to remove the invalid Locco migration candidate." }
            }
            check(backup.renameTo(main)) { "Unable to restore the original Locco database backup." }
            return
        }

        if (!main.exists() && temp.exists()) {
            throw IllegalStateException(
                "Locco found a migration candidate without its original or backup database. Candidate was preserved."
            )
        }
    }

    private fun validateLegacySource(source: FrameworkSQLiteDatabase) {
        check(source.version == LEGACY_VERSION) {
            "Unsupported Locco plaintext database version ${source.version}; expected $LEGACY_VERSION. Original data was preserved."
        }
        val actualTables = mutableListOf<String>()
        source.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) actualTables += cursor.getString(0)
        }
        // Android's framework SQLiteDatabase creates this platform-owned locale
        // table automatically. It is not application data and is intentionally not
        // copied into SQLCipher; accept only its exact one-column shape.
        val hasAndroidMetadata = "android_metadata" in actualTables
        val expectedTables = (legacyTables.map { it.name } +
            if (hasAndroidMetadata) listOf("android_metadata") else emptyList()).sorted()
        check(actualTables == expectedTables) {
            "Locco plaintext database has an unknown table set: $actualTables; original data was preserved."
        }
        if (hasAndroidMetadata) validateAndroidMetadata(source)

        legacyTables.forEach { table ->
            val columns = mutableListOf<String>()
            source.rawQuery("PRAGMA table_info(${table.name})", null).use { cursor ->
                while (cursor.moveToNext()) columns += cursor.getString(1)
            }
            check(columns == table.columns) {
                "Locco plaintext table ${table.name} has an unsupported column layout; original data was preserved."
            }
        }
        check(integrityCheck(source)) { "Locco plaintext database failed integrity_check; original data was preserved." }
        check(foreignKeyViolations(source) == 0) {
            "Locco plaintext database contains broken relationships; original data was preserved."
        }
    }

    private fun validateAndroidMetadata(source: FrameworkSQLiteDatabase) {
        val columns = mutableListOf<String>()
        source.rawQuery("PRAGMA table_info(android_metadata)", null).use { cursor ->
            while (cursor.moveToNext()) columns += cursor.getString(1)
        }
        check(columns == listOf("locale")) {
            "Locco Android metadata table has an unsupported layout; original data was preserved."
        }
        source.rawQuery("SELECT locale FROM android_metadata", null).use { cursor ->
            check(cursor.count <= 1) {
                "Locco Android metadata table has an unexpected row count; original data was preserved."
            }
            if (cursor.moveToFirst()) {
                check(!cursor.isNull(0) && cursor.getString(0).isNotBlank()) {
                    "Locco Android metadata locale is invalid; original data was preserved."
                }
            }
        }
    }

    private fun buildEncryptedCandidate(
        source: FrameworkSQLiteDatabase,
        temp: File,
        key: ByteArray
    ) {
        var destination: SQLiteDatabase? = null
        try {
            destination = SQLiteDatabase.openOrCreateDatabase(temp, key, null, null, null)
            destination.setForeignKeyConstraintsEnabled(true)
            destination.beginTransaction()
            try {
                for (version in 1..LEGACY_VERSION) {
                    NativeBridge.migrationStatements(version).forEach(destination::execSQL)
                }
                legacyTables.forEach { copyTable(source, destination, it) }
                for (version in (LEGACY_VERSION + 1)..CURRENT_VERSION) {
                    NativeBridge.migrationStatements(version).forEach(destination::execSQL)
                }
                destination.execSQL("PRAGMA user_version=$CURRENT_VERSION")
                check(foreignKeyViolations(destination) == 0) {
                    "Encrypted Locco candidate contains broken relationships."
                }
                legacyTables.forEach { table ->
                    check(rowDigest(source, table) == rowDigest(destination, table)) {
                        "Locco migration row verification failed for ${table.name}."
                    }
                }
                destination.setTransactionSuccessful()
            } finally {
                destination.endTransaction()
            }
            check(integrityCheck(destination)) { "Encrypted Locco candidate failed integrity_check." }
        } finally {
            destination?.close()
        }
        verifyEncryptedDatabase(temp, key)
    }

    private fun copyTable(
        source: FrameworkSQLiteDatabase,
        destination: SQLiteDatabase,
        table: Table
    ) {
        val columns = table.columns.joinToString(",")
        val placeholders = table.columns.joinToString(",") { "?" }
        source.rawQuery(
            "SELECT $columns FROM ${table.name} ORDER BY ${table.orderBy}",
            null
        ).use { cursor ->
            val sql = "INSERT INTO ${table.name}($columns) VALUES($placeholders)"
            while (cursor.moveToNext()) {
                val values = Array<Any?>(cursor.columnCount) { index ->
                    when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> null
                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(index)
                        else -> cursor.getString(index)
                    }
                }
                destination.execSQL(sql, values)
            }
        }
    }

    private fun rowDigest(db: FrameworkSQLiteDatabase, table: Table): String =
        digestCursor(db.rawQuery(
            "SELECT ${table.columns.joinToString(",")} FROM ${table.name} ORDER BY ${table.orderBy}",
            null
        ))

    private fun rowDigest(db: SQLiteDatabase, table: Table): String =
        digestCursor(db.rawQuery(
            "SELECT ${table.columns.joinToString(",")} FROM ${table.name} ORDER BY ${table.orderBy}",
            null
        ))

    private fun digestCursor(cursor: Cursor): String {
        val digest = MessageDigest.getInstance("SHA-256")
        cursor.use { rows ->
            while (rows.moveToNext()) {
                for (index in 0 until rows.columnCount) {
                    digest.update(rows.getType(index).toByte())
                    when (rows.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> Unit
                        Cursor.FIELD_TYPE_INTEGER -> digest.update(ByteBuffer.allocate(8).putLong(rows.getLong(index)).array())
                        Cursor.FIELD_TYPE_FLOAT -> digest.update(ByteBuffer.allocate(8).putDouble(rows.getDouble(index)).array())
                        Cursor.FIELD_TYPE_BLOB -> updateBytes(digest, rows.getBlob(index))
                        else -> updateBytes(digest, rows.getString(index).toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun updateBytes(digest: MessageDigest, bytes: ByteArray) {
        digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array())
        digest.update(bytes)
    }

    private fun integrityCheck(db: FrameworkSQLiteDatabase): Boolean =
        db.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }

    private fun integrityCheck(db: SQLiteDatabase): Boolean =
        db.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }

    private fun foreignKeyViolations(db: FrameworkSQLiteDatabase): Int =
        db.rawQuery("PRAGMA foreign_key_check", null).use { cursor ->
            var count = 0
            while (cursor.moveToNext()) count++
            count
        }

    private fun foreignKeyViolations(db: SQLiteDatabase): Int =
        db.rawQuery("PRAGMA foreign_key_check", null).use { cursor ->
            var count = 0
            while (cursor.moveToNext()) count++
            count
        }

    private fun verifyEncryptedDatabase(file: File, key: ByteArray) {
        check(file.exists() && !isPlaintextSQLite(file)) {
            "Locco migration candidate is missing or is not encrypted."
        }
        val db = SQLiteDatabase.openDatabase(
            file.absolutePath,
            key,
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null
        )
        try {
            check(integrityCheck(db)) { "Encrypted Locco database failed integrity_check." }
            db.rawQuery("PRAGMA user_version", null).use { cursor ->
                check(cursor.moveToFirst() && cursor.getInt(0) == CURRENT_VERSION) {
                    "Encrypted Locco database has an unexpected schema version."
                }
            }
            check(foreignKeyViolations(db) == 0) {
                "Encrypted Locco database contains broken relationships."
            }
        } finally {
            db.close()
        }
    }

    private fun requireNoSidecars(file: File) {
        // WAL/SHM may contain committed frames and are never discarded. A zero-byte
        // rollback-journal is SQLite's harmless post-close placeholder; a non-empty
        // journal may be hot and must remain untouched for recovery.
        listOf("-wal", "-shm").forEach { suffix ->
            check(!File(file.absolutePath + suffix).exists()) {
                "Locco database sidecar ${file.name}$suffix exists; migration was stopped without changing source data."
            }
        }
        val journal = File(file.absolutePath + "-journal")
        if (journal.exists()) {
            check(journal.length() == 0L) {
                "Locco database sidecar ${file.name}-journal is non-empty; migration was stopped without changing source data."
            }
            check(journal.delete()) {
                "Unable to remove the empty Locco rollback-journal placeholder; source database was preserved."
            }
        }
    }

    private fun isPlaintextSQLite(file: File): Boolean {
        val header = ByteArray(16)
        return runCatching {
            file.inputStream().use { it.read(header) == header.size } &&
                header.contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII))
        }.getOrDefault(false)
    }
}
