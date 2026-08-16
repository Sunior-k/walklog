package com.river.walklog.core.data.repository

import com.river.walklog.core.model.PointsLedgerEntry
import kotlinx.coroutines.flow.Flow

interface PointsLedgerRepository {
    fun observeEntries(): Flow<List<PointsLedgerEntry>>
    suspend fun record(deltaPoints: Int, reason: String)
}
