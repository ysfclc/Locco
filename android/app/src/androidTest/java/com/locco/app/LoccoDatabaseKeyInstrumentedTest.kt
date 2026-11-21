package com.locco.app

import android.content.Context
import android.security.keystore.KeyProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
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
            assertEqualsKeyLength(first)
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
        assert(database.length() == 0L)
    }

    @Test
    fun existingPlaintextDatabaseWithoutEnvelopeIsPreserved() {
        database.parentFile?.mkdirs()
        database.writeBytes("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(64))
        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
        assert(database.exists())
        assert(database.readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun malformedKeyEnvelopeFailsClosed() {
        context.getSharedPreferences("locco_database_key_v1", Context.MODE_PRIVATE)
            .edit().putString("wrapped_key", "!").commit()
        assertThrows(IllegalStateException::class.java) {
            LoccoDatabaseKey.obtain(context)
        }
        assert(!database.exists())
    }

    private fun assertEqualsKeyLength(key: ByteArray) {
        assert(key.size == 32)
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
