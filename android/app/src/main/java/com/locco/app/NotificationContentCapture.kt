package com.locco.app

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Bounded notification content that has passed Locco's conservative secret filters. */
internal data class CapturedNotificationContent(
    val title: String?,
    val text: String?,
    val contentHash: String,
    val redactedFields: Set<String>
)

/**
 * Notification content is captured only after an explicit user opt-in and only
 * for packages already present in the app-observation allowlist.
 *
 * This is a minimisation filter, not a guarantee that every sensitive value can
 * be identified. The user must review the scope before enabling content capture.
 */
internal object NotificationContentCapture {
    const val MAX_TITLE_BYTES = 512
    const val MAX_TEXT_BYTES = 7_680

    private val labelledCode = Regex(
        """(?i)\b(?:verification|security|one[- ]time|login|authentication|confirmation)\s+(?:code|otp|pin|passcode)\s*(?:is|:|-)?\s*[A-Z0-9-]{4,10}\b"""
    )
    private val labelledSecret = Regex(
        """(?i)\b(?:password|passcode|otp|pin)\s*[:=]\s*\S+"""
    )
    private val paymentCard = Regex("""(?<!\d)(?:\d[ -]?){13,19}(?!\d)""")

    fun capture(title: String?, text: String?): CapturedNotificationContent? {
        val redacted = linkedSetOf<String>()
        val safeTitle = redact(normalize(title, MAX_TITLE_BYTES), redacted).ifBlank { null }
        val safeText = redact(normalize(text, MAX_TEXT_BYTES), redacted).ifBlank { null }
        if (safeTitle == null && safeText == null) return null

        val digestInput = listOfNotNull(safeTitle, safeText).joinToString("\n")
        return CapturedNotificationContent(
            title = safeTitle,
            text = safeText,
            contentHash = sha256(digestInput),
            redactedFields = redacted
        )
    }

    private fun normalize(value: String?, maxBytes: Int): String {
        if (value.isNullOrBlank()) return ""
        val cleaned = value
            .filter { ch -> !Character.isISOControl(ch) || ch == '\n' || ch == '\r' || ch == '\t' }
            .trim()
        return truncateUtf8(cleaned, maxBytes)
    }

    private fun redact(value: String, redacted: MutableSet<String>): String {
        var result = labelledCode.replace(value) {
            redacted += "one_time_codes"
            "[REDACTED]"
        }
        result = labelledSecret.replace(result) {
            redacted += "credential_like_values"
            "[REDACTED]"
        }
        result = paymentCard.replace(result) {
            redacted += "payment_card_like_values"
            "[REDACTED]"
        }
        return result
    }

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

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
