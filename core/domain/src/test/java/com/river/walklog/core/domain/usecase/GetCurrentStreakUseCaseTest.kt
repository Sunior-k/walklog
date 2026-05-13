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
import java.time.LocalDate
import kotlin.test.assertEquals

class GetCurrentStreakUseCaseTest {

    private lateinit var repository: StepRepository
    private lateinit var useCase: GetCurrentStreakUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCurrentStreakUseCase(repository)
    }

    // today not achieved

    @Test
    fun `streak is 0 when today and yesterday are both not achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(1), steps = 1_000),
                stepCount(today, steps = 2_000),
            ),
        )

        useCase(today).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak counts from yesterday when today is not achieved but yesterday is`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(2), steps = 1_000), // not achieved
                stepCount(today.minusDays(1), steps = 6_000), // achieved
                stepCount(today, steps = 2_000), // not achieved — start from yesterday
            ),
        )

        useCase(today).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak counts consecutive days from yesterday when today is not achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(3), steps = 1_000), // not achieved — breaks streak
                stepCount(today.minusDays(2), steps = 7_000), // achieved
                stepCount(today.minusDays(1), steps = 8_000), // achieved
                stepCount(today, steps = 2_000), // not achieved
            ),
        )

        useCase(today).test {
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    // today achieved

    @Test
    fun `streak is 1 when only today is achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(1), steps = 1_000), // not achieved
                stepCount(today, steps = 6_000), // achieved
            ),
        )

        useCase(today).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak is 2 when today and yesterday are both achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(2), steps = 1_000), // not achieved — breaks streak
                stepCount(today.minusDays(1), steps = 6_000), // achieved
                stepCount(today, steps = 7_000), // achieved
            ),
        )

        useCase(today).test {
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak counts all achieved days when every day in range is achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 3)
        stubRepository(
            today,
            listOf(
                stepCount(today.minusDays(2), steps = 6_000),
                stepCount(today.minusDays(1), steps = 7_000),
                stepCount(today, steps = 8_000),
            ),
        )

        useCase(today).test {
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    // default parameter

    @Test
    fun `invoke with no explicit today uses LocalDate_now as the reference date`() = runTest {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        every {
            repository.getStepCountsForRange(
                fromEpochDay = monthStart.toEpochDay(),
                toEpochDay = today.toEpochDay(),
            )
        } returns flowOf(emptyList())

        useCase().test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    // edge cases

    @Test
    fun `streak is 0 when step data list is empty`() = runTest {
        val today = LocalDate.of(2026, 5, 1)
        stubRepository(today, emptyList())

        useCase(today).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak exactly meets target is counted as achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(stepCount(today, steps = 6_000, targetSteps = 6_000)),
        )

        useCase(today).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak one step below target is not counted as achieved`() = runTest {
        val today = LocalDate.of(2026, 5, 13)
        stubRepository(
            today,
            listOf(stepCount(today, steps = 5_999, targetSteps = 6_000)),
        )

        useCase(today).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    // helpers

    private fun stubRepository(today: LocalDate, counts: List<DailyStepCount>) {
        val monthStart = today.withDayOfMonth(1)
        every {
            repository.getStepCountsForRange(
                fromEpochDay = monthStart.toEpochDay(),
                toEpochDay = today.toEpochDay(),
            )
        } returns flowOf(counts)
    }

    private fun stepCount(
        date: LocalDate,
        steps: Int,
        targetSteps: Int = 6_000,
    ) = DailyStepCount(
        dateEpochDay = date.toEpochDay(),
        steps = steps,
        targetSteps = targetSteps,
    )
}
