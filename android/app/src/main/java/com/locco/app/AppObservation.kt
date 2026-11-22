package com.locco.app

/** A redacted, provenance-bearing receipt from an Android observation adapter. */
internal data class AppObservation(
    val id: String,
    val sourcePackage: String,
    val eventType: String,
    val occurredAt: Long,
    val receivedAt: Long,
    val acquisitionMethod: String,
    val category: String?,
    val redactedFields: String
)
