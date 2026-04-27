package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyStepSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetWeeklyReportArchiveUseCaseTest {

    private lateinit var repository: StepRepository
    private lateinit var useCase: GetWeeklyReportArchiveUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetWeeklyReportArchiveUseCase(repository)
    }

    @Test
    fun `archive returns recent weeks newest first and locks current week`() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 15)
        val currentWeekStart = LocalDate.of(2026, 4, 13)
        val previousWeekStart = LocalDate.of(2026, 4, 6)
        val twoWeeksAgoStart = LocalDate.of(2026, 3, 30)

        listOf(currentWeekStart, previousWeekStart, twoWeeksAgoStart).forEach { weekStart ->
            every {
                repository.getWeeklyStepSummary(weekStart.toEpochDay())
            } returns flowOf(WeeklyStepSummary.empty(weekStart.toEpochDay()))
        }

        useCase(anchorDate = anchorDate, weekCount = 3).test {
            val entries = awaitItem()

            assertEquals(
                listOf(
                    currentWeekStart.toEpochDay(),
                    previousWeekStart.toEpochDay(),
                    twoWeeksAgoStart.toEpochDay(),
                ),
                entries.map { it.weekStartEpochDay },
            )
            assertTrue(entries[0].isLocked)
            assertEquals(LocalDate.of(2026, 4, 20), entries[0].unlockDate)
            assertFalse(entries[1].isLocked)
            assertFalse(entries[2].isLocked)
            awaitComplete()
        }
    }
}
