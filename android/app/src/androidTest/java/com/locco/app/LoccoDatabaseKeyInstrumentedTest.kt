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
        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
        check(database.exists())
        assertTrue(database.length() == 0L)
    }

    @Test
    fun existingPlaintextDatabaseWithoutEnvelopeIsPreserved() {
        database.parentFile?.mkdirs()
        database.writeBytes("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(64))
        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
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
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            .deleteEntry("com.locco.app.database.wrap.v1")

        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
        assertTrue(database.exists())
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), database.readBytes())
    }

    @Test
    fun malformedKeyEnvelopeFailsClosed() {
        context.getSharedPreferences("locco_database_key_v1", Context.MODE_PRIVATE)
            .edit().putString("wrapped_key", "!").commit()
        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
        assertTrue(!database.exists())
    }

    @Test
    fun actualSessionContentIsEncryptedOnDiskAndReadableAfterReopen() {
        val sentinel = "LOCCO_ENCRYPTION_SENTINEL_7f39b2c1"
        val session = Session(
            id = "session-encryption-check",
            title = "Encryption verification",
            time = 1_800_000_000_000L,
            notes = listOf(Note("note-encryption-check", sentinel, 1_800_000_000_001L))
        )

        val firstOpen = LoccoDatabase.create(context)
        firstOpen.replaceSessions(listOf(session))
        assertEquals(listOf(session), firstOpen.readSessions())
        firstOpen.close()

        assertTrue(database.exists())
        assertFalse(
            database.readBytes().take(16).toByteArray()
                .contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII))
        )
        val artifacts = listOf(
            database,
            File(database.path + "-wal"),
            File(database.path + "-shm"),
            File(database.path + "-journal")
        ).filter { it.exists() }
        val sentinelBytes = sentinel.toByteArray(Charsets.UTF_8)
        artifacts.forEach { file ->
            assertFalse("Plaintext sentinel leaked into ${file.name}", containsBytes(file.readBytes(), sentinelBytes))
        }

        val reopened = LoccoDatabase.create(context)
        try {
            assertEquals(listOf(session), reopened.readSessions())
        } finally {
            reopened.close()
        }
    }

    private fun containsBytes(haystack: ByteArray, needle: ByteArray): Boolean {
        if (needle.isEmpty() || haystack.size < needle.size) return false
        return (0..haystack.size - needle.size).any { start ->
            needle.indices.all { offset -> haystack[start + offset] == needle[offset] }
        }
    }

    private fun clearState() {
        database.delete()
        File(database.path + "-wal").delete()
        File(database.path + "-shm").delete()
        context.deleteSharedPreferences("locco_database_key_v1")
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .deleteEntry("com.locco.app.database.wrap.v1")
        }
    }
}
