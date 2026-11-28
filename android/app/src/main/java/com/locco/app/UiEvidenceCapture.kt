package com.locco.app

import java.nio.charset.StandardCharsets

/** Primitive-only copy of an accessibility node, safe to retain after the callback returns. */
internal data class UiNodeCandidate(
    val text: String? = null,
    val contentDescription: String? = null,
    val className: String? = null,
    val visibleToUser: Boolean = true,
    val password: Boolean = false,
    val editable: Boolean = false
)

internal data class CapturedUiSnapshot(
    val text: String?,
    val contentHash: String?,
    val redactedFields: Set<String>,
    val status: String
)

internal data class CapturedUiTarget(
    val label: String?,
    val className: String?,
    val redactedFields: Set<String>
)

/**
 * Bounded, best-effort capture of accessible labels.
 *
 * This is not a screenshot, complete screen representation, DOM, or guarantee that
 * every app exposes its content. Editable and password nodes are never copied.
 */
internal object UiEvidenceCapture {
    const val MAX_NODES = 80
    const val MAX_NODE_BYTES = 160
    const val MAX_TARGET_BYTES = 512
    const val MAX_SNAPSHOT_BYTES = 7_680
    const val MAX_CLASS_BYTES = 255

    fun snapshot(
        nodes: List<UiNodeCandidate>?,
        nodeLimitReached: Boolean = false
    ): CapturedUiSnapshot {
        if (nodes == null) {
            return CapturedUiSnapshot(
                text = null,
                contentHash = null,
                redactedFields = setOf("accessible_hierarchy_unavailable"),
                status = "unavailable"
            )
        }

        val redacted = linkedSetOf<String>()
        val lines = linkedSetOf<String>()
        var partial = nodeLimitReached
        if (nodeLimitReached) redacted += "node_limit_reached"

        nodes.take(MAX_NODES).forEach { node ->
            if (!node.visibleToUser) {
                partial = true
                redacted += "invisible_nodes_skipped"
                return@forEach
            }
            if (node.password) {
                partial = true
                redacted += "password_nodes_excluded"
                return@forEach
            }
            if (node.editable) {
                partial = true
                redacted += "editable_nodes_excluded"
                return@forEach
            }

            val rawLabel = node.contentDescription?.takeIf(String::isNotBlank)
                ?: node.text?.takeIf(String::isNotBlank)
                ?: return@forEach
            val normalized = normalize(rawLabel)
            val bounded = truncateUtf8(normalized, MAX_NODE_BYTES)
            if (bounded != normalized) {
                partial = true
                redacted += "node_text_truncated"
            }
            val safe = NotificationContentCapture.capture(null, bounded)
            if (safe == null || safe.text.isNullOrBlank()) {
                partial = true
                redacted += "sensitive_or_empty_node_text"
                safe?.redactedFields?.forEach { redacted += it }
                return@forEach
            }
            safe.redactedFields.forEach { redacted += it }
            if (safe.redactedFields.isNotEmpty()) partial = true

            val className = node.className?.let { truncateUtf8(normalize(it), MAX_CLASS_BYTES) }
                ?.takeIf(String::isNotBlank)
            val line = if (className == null) safe.text else "$className: ${safe.text}"
            lines += line
        }

        if (lines.isEmpty()) {
            val unavailableBecauseEmpty = redacted.isEmpty()
            redacted += "accessible_text_not_exposed"
            return CapturedUiSnapshot(
                text = null,
                contentHash = null,
                redactedFields = redacted,
                status = if (unavailableBecauseEmpty) "unavailable" else "partial"
            )
        }

        val assembled = lines.joinToString("\n")
        val boundedSnapshot = truncateUtf8(assembled, MAX_SNAPSHOT_BYTES)
        if (boundedSnapshot != assembled) {
            partial = true
            redacted += "snapshot_text_truncated"
        }
        val safeSnapshot = NotificationContentCapture.capture(null, boundedSnapshot)
        if (safeSnapshot == null || safeSnapshot.text.isNullOrBlank()) {
            redacted += "accessible_text_not_exposed"
            return CapturedUiSnapshot(null, null, redacted, "partial")
        }
        safeSnapshot.redactedFields.forEach { redacted += it }
        if (safeSnapshot.redactedFields.isNotEmpty()) partial = true
        return CapturedUiSnapshot(
            text = safeSnapshot.text,
            contentHash = safeSnapshot.contentHash,
            redactedFields = redacted,
            status = if (partial || redacted.isNotEmpty()) "partial" else "available"
        )
    }

    fun target(node: UiNodeCandidate?): CapturedUiTarget {
        if (node == null || !node.visibleToUser) {
            return CapturedUiTarget(null, null, setOf("target_node_unavailable"))
        }
        if (node.password) {
            return CapturedUiTarget(null, null, setOf("password_target_excluded"))
        }
        if (node.editable) {
            return CapturedUiTarget(null, null, setOf("editable_target_excluded"))
        }
        val rawLabel = node.contentDescription?.takeIf(String::isNotBlank)
            ?: node.text?.takeIf(String::isNotBlank)
        val redacted = linkedSetOf<String>()
        val normalized = rawLabel?.let(::normalize)
        val label = normalized?.let { truncateUtf8(it, MAX_TARGET_BYTES) }
        if (normalized != null && label != normalized) redacted += "target_label_truncated"
        val safe = NotificationContentCapture.capture(null, label)
        safe?.redactedFields?.forEach { redacted += it }
        val className = node.className?.let { truncateUtf8(normalize(it), MAX_CLASS_BYTES) }
            ?.takeIf(String::isNotBlank)
        return CapturedUiTarget(safe?.text, className, redacted)
    }

    private fun normalize(value: String): String = value
        .filter { ch -> !Character.isISOControl(ch) || ch == '\n' || ch == '\r' || ch == '\t' }
        .trim()

    private fun truncateUtf8(value: String, maxBytes: Int): String {
        var end = value.length
        while (end > 0 && value.substring(0, end).toByteArray(StandardCharsets.UTF_8).size > maxBytes) {
            end -= if (
                end >= 2 &&
                Character.isHighSurrogate(value[end - 2]) &&
                Character.isLowSurrogate(value[end - 1])
            ) 2 else 1
        }
        return value.substring(0, end)
    }
}
