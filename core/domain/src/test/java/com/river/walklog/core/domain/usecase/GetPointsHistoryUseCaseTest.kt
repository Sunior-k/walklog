package com.river.walklog.core.domain.usecase

import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetPointsHistoryUseCaseTest {

    private lateinit var pointsLedgerRepository: FakePointsLedgerRepository
    private lateinit var useCase: GetPointsHistoryUseCase

    @Before
    fun setUp() {
        pointsLedgerRepository = FakePointsLedgerRepository()
        useCase = GetPointsHistoryUseCase(pointsLedgerRepository)
    }

    @Test
    fun `returns recorded ledger entries`() = runTest {
        pointsLedgerRepository.record(20, "DAILY")
        pointsLedgerRepository.record(-200, "REDEEM_walk-badge-gold")

        val entries = useCase().first()

        assertEquals(2, entries.size)
    }

    @Test
    fun `returns empty list when no entries recorded`() = runTest {
        val entries = useCase().first()

        assertEquals(emptyList(), entries)
    }
}
