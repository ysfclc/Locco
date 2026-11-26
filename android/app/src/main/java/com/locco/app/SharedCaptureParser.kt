package com.locco.app

import java.nio.charset.StandardCharsets

/**
 * Parses explicit Android text-share payloads without depending on Android classes.
 * MIME parameters are ignored for matching, but only text/plain is accepted.
 */
internal object SharedCaptureParser {
    private const val ACTION_SEND = "android.intent.action.SEND"

    fun parse(
        action: String?,
        mimeType: String?,
        sharedText: CharSequence?,
        subject: CharSequence?,
        fallbackTitle: CharSequence?,
        token: String
    ): SharedCapture? {
        val baseMimeType = mimeType?.substringBefore(';')?.trim()
        if (action != ACTION_SEND || !baseMimeType.equals("text/plain", ignoreCase = true)) return null
        val value = sharedText?.toString()?.trim().orEmpty()
        if (value.isEmpty()) return null
        val title = subject?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            ?: fallbackTitle?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        if (value.toByteArray(StandardCharsets.UTF_8).size > CaptureKernel.MAX_CONTENT_BYTES) {
            return SharedCapture(token, "", title, "The shared text is larger than Locco's 64 KiB capture limit.")
        }
        return SharedCapture(token = token, text = value, title = title)
    }
}
