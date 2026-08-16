package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.model.PointsLedgerEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPointsHistoryUseCase @Inject constructor(
    private val pointsLedgerRepository: PointsLedgerRepository,
) {
    operator fun invoke(): Flow<List<PointsLedgerEntry>> = pointsLedgerRepository.observeEntries()
}
