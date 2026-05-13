package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
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

    private fun summaryWithSteps(weekStart: LocalDate, steps: Int = 5_000) =
        WeeklyStepSummary(
            weekStartEpochDay = weekStart.toEpochDay(),
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = weekStart.toEpochDay(), steps = steps),
            ),
        )

    @Test
    fun `archive returns recent weeks newest first and locks current week`() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 15)
        val currentWeekStart = LocalDate.of(2026, 4, 13)
        val previousWeekStart = LocalDate.of(2026, 4, 6)
        val twoWeeksAgoStart = LocalDate.of(2026, 3, 30)

        every {
            repository.getWeeklyStepSummary(currentWeekStart.toEpochDay())
        } returns flowOf(WeeklyStepSummary(weekStartEpochDay = currentWeekStart.toEpochDay(), dailyCounts = emptyList()))

        listOf(previousWeekStart, twoWeeksAgoStart).forEach { weekStart ->
            every {
                repository.getWeeklyStepSummary(weekStart.toEpochDay())
            } returns flowOf(summaryWithSteps(weekStart))
        }

        useCase(anchorDate = anchorDate, weekCount = 3).test {
            val items = awaitItem()

            assertEquals(
                listOf(
                    currentWeekStart.toEpochDay(),
                    previousWeekStart.toEpochDay(),
                    twoWeeksAgoStart.toEpochDay(),
                ),
                items.map { it.weekStartEpochDay },
            )
            assertTrue(items[0].isLocked)
            assertEquals(LocalDate.of(2026, 4, 20), items[0].unlockDate)
            assertFalse(items[1].isLocked)
            assertFalse(items[2].isLocked)
            awaitComplete()
        }
    }

    @Test
    fun `empty non-locked weeks are excluded from the archive`() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 15)
        val currentWeekStart = LocalDate.of(2026, 4, 13)
        val previousWeekStart = LocalDate.of(2026, 4, 6)

        every {
            repository.getWeeklyStepSummary(currentWeekStart.toEpochDay())
        } returns flowOf(WeeklyStepSummary(weekStartEpochDay = currentWeekStart.toEpochDay(), dailyCounts = emptyList()))

        every {
            repository.getWeeklyStepSummary(previousWeekStart.toEpochDay())
        } returns flowOf(WeeklyStepSummary(weekStartEpochDay = previousWeekStart.toEpochDay(), dailyCounts = emptyList()))

        useCase(anchorDate = anchorDate, weekCount = 2).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertTrue(items[0].isLocked)
            awaitComplete()
        }
    }

    @Test
    fun `achievement fields are computed correctly`() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 15)
        val previousWeekStart = LocalDate.of(2026, 4, 6)

        every {
            repository.getWeeklyStepSummary(LocalDate.of(2026, 4, 13).toEpochDay())
        } returns flowOf(WeeklyStepSummary(weekStartEpochDay = LocalDate.of(2026, 4, 13).toEpochDay(), dailyCounts = emptyList()))

        val targetSteps = 6_000
        every {
            repository.getWeeklyStepSummary(previousWeekStart.toEpochDay())
        } returns flowOf(
            WeeklyStepSummary(
                weekStartEpochDay = previousWeekStart.toEpochDay(),
                dailyCounts = listOf(
                    DailyStepCount(dateEpochDay = previousWeekStart.toEpochDay(), steps = 7_000, targetSteps = targetSteps),
                    DailyStepCount(dateEpochDay = previousWeekStart.toEpochDay() + 1, steps = 3_000, targetSteps = targetSteps),
                ),
            ),
        )

        useCase(anchorDate = anchorDate, weekCount = 2).test {
            val items = awaitItem()
            val prev = items.first { !it.isLocked }
            assertEquals(10_000, prev.totalSteps)
            assertEquals(1, prev.achievedDays)
            awaitComplete()
        }
    }
}
