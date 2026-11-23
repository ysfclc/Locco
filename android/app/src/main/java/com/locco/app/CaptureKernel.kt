package com.locco.app

import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** A validated source/evidence pair ready for one atomic database write. */
internal data class PreparedCapture(val source: SourceEntry, val evidence: EvidenceEntry)

/** Explicit user-capture normalization. No Android APIs or background collection are used here. */
internal object CaptureKernel {
    const val MAX_CONTENT_BYTES = 64 * 1024
    private const val MAX_TITLE_BYTES = 240
    private const val MAX_LOCATOR_BYTES = 2048

    fun prepare(
        text: String,
        title: String?,
        locator: String?,
        capturedAt: Long,
        sourceId: String,
        evidenceId: String,
        sourceKind: String = "manual_text"
    ): PreparedCapture? {
        val excerpt = text.trim()
        if (excerpt.isEmpty()) return null
        val contentBytes = excerpt.toByteArray(StandardCharsets.UTF_8)
        require(contentBytes.size <= MAX_CONTENT_BYTES) { "Capture text is limited to 64 KiB." }
        require(capturedAt >= 0) { "Capture timestamp must be non-negative." }

        val normalizedLocator = normalizeLocator(locator, excerpt)
        val requestedTitle = title?.trim()?.takeIf { it.isNotEmpty() }
            ?: excerpt.lineSequence().firstOrNull { it.isNotBlank() }
            ?: "Capture"
        val normalizedTitle = utf8Prefix(requestedTitle, MAX_TITLE_BYTES)
        require(normalizedTitle.isNotBlank()) { "Capture title is empty." }

        val digest = MessageDigest.getInstance("SHA-256").digest(contentBytes)
        val hash = "sha256:" + digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        val source = SourceEntry(sourceId, sourceKind, normalizedLocator, normalizedTitle, capturedAt)
        val evidence = EvidenceEntry(evidenceId, sourceId, excerpt, hash, capturedAt)
        return PreparedCapture(source, evidence)
    }

    private fun normalizeLocator(explicit: String?, excerpt: String): String? {
        val candidate = explicit?.trim()?.takeIf { it.isNotEmpty() }
            ?: excerpt.takeIf { it.startsWith("https://", true) || it.startsWith("http://", true) }
            ?: return null
        require(candidate.toByteArray(StandardCharsets.UTF_8).size <= MAX_LOCATOR_BYTES) { "Source URL is longer than 2 KiB." }
        require(candidate.none(Char::isWhitespace)) { "Source URL cannot contain whitespace." }
        val parsed = runCatching { URI(candidate) }.getOrNull()
            ?: throw IllegalArgumentException("Source URL must be a valid http or https URL without embedded credentials.")
        val originalScheme = parsed.scheme
            ?: throw IllegalArgumentException("Source URL must be a valid http or https URL without embedded credentials.")
        val canonicalScheme = when (originalScheme.lowercase()) {
            "http" -> "http"
            "https" -> "https"
            else -> throw IllegalArgumentException("Source URL must be a valid http or https URL without embedded credentials.")
        }
        require(!parsed.host.isNullOrBlank() && parsed.rawUserInfo == null) {
            "Source URL must be a valid http or https URL without embedded credentials."
        }
        return URI(canonicalScheme + candidate.substring(originalScheme.length)).toASCIIString()
    }

    private fun utf8Prefix(value: String, maxBytes: Int): String {
        val result = StringBuilder()
        var offset = 0
        var usedBytes = 0
        while (offset < value.length) {
            val codePoint = value.codePointAt(offset)
            val piece = String(Character.toChars(codePoint))
            val bytes = piece.toByteArray(StandardCharsets.UTF_8).size
            if (usedBytes + bytes > maxBytes) break
            result.append(piece)
            usedBytes += bytes
            offset += Character.charCount(codePoint)
        }
        return result.toString().trim()
    }
}
