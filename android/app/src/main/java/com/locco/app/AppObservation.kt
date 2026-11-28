package com.locco.app

/** A provenance-bearing receipt from an Android observation adapter. */
internal data class AppObservation(
    val id: String,
    val sourcePackage: String,
    val eventType: String,
    val occurredAt: Long,
    val receivedAt: Long,
    val acquisitionMethod: String,
    val category: String?,
    val redactedFields: String,
    val contentTitle: String? = null,
    val contentText: String? = null,
    val contentHash: String? = null,
    val screenClass: String? = null,
    val targetLabel: String? = null,
    val targetClass: String? = null,
    val snapshotStatus: String? = null
)

/** Bounded, provenance-bearing UI event copied from an Android accessibility callback. */
internal data class UiObservationRecord(
    val id: String,
    val dedupeKey: String,
    val sourcePackage: String,
    val eventType: String,
    val occurredAt: Long,
    val receivedAt: Long,
    val screenClass: String?,
    val targetLabel: String?,
    val targetClass: String?,
    val contentText: String?,
    val contentHash: String?,
    val redactedFields: List<String>,
    val snapshotStatus: String
)
