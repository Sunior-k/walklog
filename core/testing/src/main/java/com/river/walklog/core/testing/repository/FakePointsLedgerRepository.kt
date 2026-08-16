package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.model.PointsLedgerEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePointsLedgerRepository : PointsLedgerRepository {

    private val _entries = MutableStateFlow<List<PointsLedgerEntry>>(emptyList())
    val entries: StateFlow<List<PointsLedgerEntry>> = _entries

    override fun observeEntries(): StateFlow<List<PointsLedgerEntry>> = _entries

    override suspend fun record(deltaPoints: Int, reason: String) {
        val entry = PointsLedgerEntry(
            id = (_entries.value.size + 1).toLong(),
            deltaPoints = deltaPoints,
            reason = reason,
            createdAtEpochMillis = System.currentTimeMillis(),
        )
        _entries.value = listOf(entry) + _entries.value
    }
}
