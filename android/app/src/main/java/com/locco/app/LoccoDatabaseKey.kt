package com.locco.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Wraps a random SQLCipher database key with a non-exportable Android Keystore AES key.
 * The unwrapped database key exists only in memory while opening the database.
 */
internal object LoccoDatabaseKey {
    private const val PREFS = "locco_database_key_v1"
    private const val ENVELOPE = "wrapped_key"
    private const val ALIAS = "com.locco.app.database.wrap.v1"
    private const val KEY_BYTES = 32
    private const val GCM_TAG_BITS = 128


    @Synchronized
    fun obtain(context: Context): ByteArray = obtainInternal(context, allowPlaintextMigration = false)

    /**
     * Only the database-open path may create an envelope for a recognized plaintext SQLite
     * file. The caller must immediately run the verified v2 migrator before SQLCipher opens it.
     */
    @Synchronized
    fun obtainForDatabaseOpen(context: Context): ByteArray =
        obtainInternal(context, allowPlaintextMigration = true)

    private fun obtainInternal(context: Context, allowPlaintextMigration: Boolean): ByteArray {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = prefs.getString(ENVELOPE, null)
        if (encoded != null) return unwrap(encoded)

        val database = app.getDatabasePath("locco_kernel.db")
        // Never initialize over an unknown, zero-byte, or otherwise unrecognized file.
        // A valid SQLite header is eligible only for the explicit migration entry point.
        if (database.exists() && !(allowPlaintextMigration && isPlaintextSQLite(database))) {
            throw IllegalStateException(
                "Locco database key is missing. The existing database was preserved and was not replaced."
            )
        }

        val raw = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, wrappingKey())
            val ciphertext = cipher.doFinal(raw)
            val envelope = cipher.iv + ciphertext
            check(prefs.edit().putString(ENVELOPE, Base64.encodeToString(envelope, Base64.NO_WRAP)).commit()) {
                "Unable to persist the encrypted Locco database key envelope"
            }
            return raw
        } catch (failure: Throwable) {
            raw.fill(0)
            throw failure
        }
    }


    private fun unwrap(encoded: String): ByteArray {
        val envelope = try {
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (failure: IllegalArgumentException) {
            throw IllegalStateException("Locco database key envelope is invalid; database access was blocked.", failure)
        }
        val ivSize = 12
        if (envelope.size <= ivSize + 16) {
            envelope.fill(0)
            throw IllegalStateException(
                "Locco database key envelope is truncated; database access was blocked."
            )
        }
        val iv = envelope.copyOfRange(0, ivSize)
        val ciphertext = envelope.copyOfRange(ivSize, envelope.size)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, existingWrappingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.doFinal(ciphertext).also {
                check(it.size == KEY_BYTES) { "Locco database key has an invalid length." }
            }
        } catch (failure: Throwable) {
            throw IllegalStateException(
                "Locco cannot unlock its database key. Existing database data was preserved.",
                failure
            )
        }
    }


    private fun existingWrappingKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey(ALIAS, null) as? SecretKey
            ?: throw IllegalStateException("Locco Android Keystore key is missing; existing database data was preserved.")
    }

    private fun wrappingKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }


    private fun isPlaintextSQLite(file: File): Boolean {
        val header = ByteArray(16)
        return runCatching {
            file.inputStream().use { input -> input.read(header) == header.size } &&
                header.contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII))
        }.getOrDefault(false)
    }
}
