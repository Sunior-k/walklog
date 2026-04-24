package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyStepSummary
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.test.assertEquals

class GetWeeklyStepSummaryUseCaseTest {

    private lateinit var repository: StepRepository
    private lateinit var useCase: GetWeeklyStepSummaryUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetWeeklyStepSummaryUseCase(repository)
    }

    @Test
    fun `invoke with explicit weekStartEpochDay delegates to repository`() = runTest {
        val targetEpochDay = 19_000L
        every {
            repository.getWeeklyStepSummary(targetEpochDay)
        } returns flowOf(WeeklyStepSummary.empty(targetEpochDay))

        useCase(targetEpochDay).test {
            assertEquals(targetEpochDay, awaitItem().weekStartEpochDay)
            awaitComplete()
        }
    }

    @Test
    fun `invoke without param uses Monday of current week`() = runTest {
        val capturedEpochDay = slot<Long>()
        every {
            repository.getWeeklyStepSummary(capture(capturedEpochDay))
        } returns flowOf(WeeklyStepSummary.empty(0L))

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        val resolvedDate = LocalDate.ofEpochDay(capturedEpochDay.captured)
        assertEquals(DayOfWeek.MONDAY, resolvedDate.dayOfWeek)
    }

    @Test
    fun `invoke without param resolves to Monday on or before today`() = runTest {
        val capturedEpochDay = slot<Long>()
        every {
            repository.getWeeklyStepSummary(capture(capturedEpochDay))
        } returns flowOf(WeeklyStepSummary.empty(0L))

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        val resolvedDate = LocalDate.ofEpochDay(capturedEpochDay.captured)
        val today = LocalDate.now()
        // The resolved Monday must be on or before today and at most 6 days in the past
        assert(!resolvedDate.isAfter(today)) {
            "Expected resolved Monday ($resolvedDate) to be on or before today ($today)"
        }
        assert(!resolvedDate.isBefore(today.minusDays(6))) {
            "Expected resolved Monday ($resolvedDate) within 6 days before today ($today)"
        }
    }
}
