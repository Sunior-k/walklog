package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetMonthlyRecapUseCaseTest {

    private lateinit var repository: StepRepository
    private lateinit var useCase: GetMonthlyRecapUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetMonthlyRecapUseCase(repository)
    }

    // ─── 기본 집계 ─────────────────────────────────────────────────────────

    @Test
    fun `totalSteps is sum of all daily steps`() = runTest {
        val days = listOf(
            stepCount(steps = 3_000),
            stepCount(steps = 7_000),
        )
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(10_000, awaitItem().totalSteps)
            awaitComplete()
        }
    }

    @Test
    fun `activeDays excludes days with zero steps`() = runTest {
        val days = listOf(
            stepCount(steps = 5_000),
            stepCount(steps = 0),
            stepCount(steps = 3_000),
            stepCount(steps = 0),
        )
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(2, awaitItem().activeDays)
            awaitComplete()
        }
    }

    @Test
    fun `averageStepsPerDay is based on activeDays not totalDays`() = runTest {
        // activeDays = 2, totalSteps = 10_000 → avg = 5_000
        val days = listOf(
            stepCount(steps = 4_000),
            stepCount(steps = 6_000),
            stepCount(steps = 0),
            stepCount(steps = 0),
        )
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(5_000, awaitItem().averageStepsPerDay)
            awaitComplete()
        }
    }

    @Test
    fun `averageStepsPerDay is zero when no active days`() = runTest {
        stubRepository(listOf(stepCount(steps = 0), stepCount(steps = 0)))

        useCase(2025, 3).test {
            assertEquals(0, awaitItem().averageStepsPerDay)
            awaitComplete()
        }
    }

    // ─── bestDay ───────────────────────────────────────────────────────────

    @Test
    fun `bestDay is the day with most steps`() = runTest {
        val best = stepCount(dateEpochDay = 19_002L, steps = 12_000)
        val days = listOf(
            stepCount(dateEpochDay = 19_000L, steps = 3_000),
            best,
            stepCount(dateEpochDay = 19_001L, steps = 8_000),
        )
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(best, awaitItem().bestDay)
            awaitComplete()
        }
    }

    @Test
    fun `bestDay is null when all days have zero steps`() = runTest {
        stubRepository(listOf(stepCount(steps = 0), stepCount(steps = 0)))

        useCase(2025, 3).test {
            assertNull(awaitItem().bestDay)
            awaitComplete()
        }
    }

    // ─── longestStreak ─────────────────────────────────────────────────────

    @Test
    fun `longestStreak counts consecutive achieved days`() = runTest {
        // achieved, achieved, not, achieved → longest streak = 2
        val days = listOf(
            stepCount(steps = 6_000, targetSteps = 6_000), // achieved
            stepCount(steps = 7_000, targetSteps = 6_000), // achieved
            stepCount(steps = 5_000, targetSteps = 6_000), // not achieved
            stepCount(steps = 8_000, targetSteps = 6_000), // achieved
        )
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(2, awaitItem().longestStreak)
            awaitComplete()
        }
    }

    @Test
    fun `longestStreak is full week when all days are achieved`() = runTest {
        val days = List(7) { stepCount(steps = 6_000, targetSteps = 6_000) }
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(7, awaitItem().longestStreak)
            awaitComplete()
        }
    }

    @Test
    fun `longestStreak is zero when no days are achieved`() = runTest {
        val days = List(7) { stepCount(steps = 1_000, targetSteps = 6_000) }
        stubRepository(days)

        useCase(2025, 3).test {
            assertEquals(0, awaitItem().longestStreak)
            awaitComplete()
        }
    }

    // ─── estimatedCalories ─────────────────────────────────────────────────

    @Test
    fun `estimatedCalories uses 0_04 kcal per step`() = runTest {
        // 10_000 steps × 0.04 = 400 kcal
        stubRepository(listOf(stepCount(steps = 10_000)))

        useCase(2025, 3).test {
            assertEquals(400, awaitItem().estimatedCalories)
            awaitComplete()
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun stubRepository(days: List<DailyStepCount>) {
        every {
            repository.getStepCountsForRange(any(), any())
        } returns flowOf(days)
    }

    private fun stepCount(
        dateEpochDay: Long = 19_000L,
        steps: Int = 0,
        targetSteps: Int = 6_000,
    ) = DailyStepCount(
        dateEpochDay = dateEpochDay,
        steps = steps,
        targetSteps = targetSteps,
    )
}
