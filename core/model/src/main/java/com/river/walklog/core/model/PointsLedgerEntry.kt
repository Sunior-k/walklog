package com.river.walklog.core.model

data class PointsLedgerEntry(
    val id: Long,
    val deltaPoints: Int,
    val reason: String,
    val createdAtEpochMillis: Long,
)
