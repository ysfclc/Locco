package com.locco.app

import java.nio.charset.StandardCharsets

/** Canonical origin metadata. The source identifies where evidence came from, not its content. */
internal data class SourceEntry(
    val id: String,
    val kind: String,
    val locator: String?,
    val title: String?,
    val capturedAt: Long
) {
    init {
        require(validRecordId(id)) { "Invalid source ID" }
        require(validLabel(kind, 80)) { "Invalid source kind" }
        require(capturedAt >= 0) { "Source timestamp must be non-negative" }
    }
}

/** Immutable captured material linked to exactly one source. */
internal data class EvidenceEntry(
    val id: String,
    val sourceId: String,
    val excerpt: String?,
    val contentHash: String?,
    val capturedAt: Long
) {
    init {
        require(validRecordId(id)) { "Invalid evidence ID" }
        require(validRecordId(sourceId)) { "Invalid evidence source ID" }
        require(!excerpt.isNullOrEmpty() || !contentHash.isNullOrEmpty()) {
            "Evidence requires a non-empty excerpt or content hash"
        }
        require(capturedAt >= 0) { "Evidence timestamp must be non-negative" }
    }
}

private fun validRecordId(value: String): Boolean =
    value.isNotEmpty() &&
        value.toByteArray(StandardCharsets.UTF_8).size <= 128 &&
        value.trim() == value &&
        value.none(Char::isISOControl)

private fun validLabel(value: String, maxBytes: Int): Boolean =
    value.isNotEmpty() &&
        value.toByteArray(StandardCharsets.UTF_8).size <= maxBytes &&
        value.trim() == value &&
        value.none(Char::isISOControl)
