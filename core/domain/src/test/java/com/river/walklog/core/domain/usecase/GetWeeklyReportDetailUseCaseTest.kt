package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetWeeklyReportDetailUseCaseTest {

    private lateinit var stepRepository: StepRepository
    private lateinit var getWeeklyBestHour: GetWeeklyBestHourUseCase
    private lateinit var useCase: GetWeeklyReportDetailUseCase

    @Before
    fun setUp() {
        stepRepository = mockk()
        getWeeklyBestHour = mockk()
        useCase = GetWeeklyReportDetailUseCase(stepRepository, getWeeklyBestHour)
    }

    // isEmpty

    @Test
    fun `isEmpty is true when all daily counts have zero steps`() = runTest {
        stubSummary(List(7) { 0 })
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertTrue(awaitItem().isEmpty)
            awaitComplete()
        }
    }

    @Test
    fun `isEmpty is false when at least one day has non-zero steps`() = runTest {
        stubSummary(listOf(0, 0, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertFalse(awaitItem().isEmpty)
            awaitComplete()
        }
    }

    // weekCounts always 7 entries

    @Test
    fun `weekCounts always contains exactly 7 entries regardless of summary size`() = runTest {
        val counts = listOf(
            DailyStepCount(dateEpochDay = WEEK_START, steps = 5_000),
            DailyStepCount(dateEpochDay = WEEK_START + 1, steps = 7_000),
        )
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(7, awaitItem().weekCounts.size)
            awaitComplete()
        }
    }

    @Test
    fun `missing days in weekCounts are filled with 0 steps`() = runTest {
        val counts = listOf(DailyStepCount(dateEpochDay = WEEK_START, steps = 5_000))
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            val item = awaitItem()
            assertEquals(5_000, item.weekCounts[0].steps)
            assertEquals(0, item.weekCounts[1].steps)
            assertEquals(0, item.weekCounts[6].steps)
            awaitComplete()
        }
    }

    @Test
    fun `weekCounts preserves correct epochDay for each day`() = runTest {
        stubSummary(listOf(0, 0, 3_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            val counts = awaitItem().weekCounts
            for (i in 0..6) {
                assertEquals(WEEK_START + i, counts[i].dateEpochDay)
            }
            awaitComplete()
        }
    }

    // totalSteps

    @Test
    fun `totalSteps is sum of all step counts in the summary`() = runTest {
        stubSummary(listOf(3_000, 7_000, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(15_000, awaitItem().totalSteps)
            awaitComplete()
        }
    }

    // achievedDays

    @Test
    fun `achievedDays counts days that meet or exceed their target`() = runTest {
        val counts = listOf(
            DailyStepCount(WEEK_START, steps = 6_000, targetSteps = 6_000),     // achieved
            DailyStepCount(WEEK_START + 1, steps = 7_000, targetSteps = 6_000), // achieved
            DailyStepCount(WEEK_START + 2, steps = 3_000, targetSteps = 6_000), // not achieved
        )
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(2, awaitItem().achievedDays)
            awaitComplete()
        }
    }

    // achievementRate

    @Test
    fun `achievementRate is achievedDays divided by 7`() = runTest {
        val counts = listOf(
            DailyStepCount(WEEK_START, steps = 6_000, targetSteps = 6_000),
            DailyStepCount(WEEK_START + 1, steps = 6_000, targetSteps = 6_000),
        )
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(2 / 7f, awaitItem().achievementRate, absoluteTolerance = 0.001f)
            awaitComplete()
        }
    }

    // bestDayEpochDay

    @Test
    fun `bestDayEpochDay is the epoch day of the day with the most steps`() = runTest {
        stubSummary(listOf(3_000, 9_000, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(WEEK_START + 1, awaitItem().bestDayEpochDay)
            awaitComplete()
        }
    }

    // bestHour

    @Test
    fun `bestHour comes from GetWeeklyBestHourUseCase`() = runTest {
        stubSummary(listOf(0, 0, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns 14

        useCase(WEEK_START).test {
            assertEquals(14, awaitItem().bestHour)
            awaitComplete()
        }
    }

    @Test
    fun `bestHour is null when GetWeeklyBestHourUseCase returns null`() = runTest {
        stubSummary(listOf(0, 0, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertNull(awaitItem().bestHour)
            awaitComplete()
        }
    }

    // longestAchievedStreak

    @Test
    fun `longestAchievedStreak is 0 when no days are achieved`() = runTest {
        val counts = List(3) { i -> DailyStepCount(WEEK_START + i, steps = 1_000, targetSteps = 6_000) }
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(0, awaitItem().longestAchievedStreak)
            awaitComplete()
        }
    }

    @Test
    fun `longestAchievedStreak picks the longest consecutive run`() = runTest {
        // achieved, achieved, not, achieved → longest = 2
        val counts = listOf(
            DailyStepCount(WEEK_START,     steps = 6_000, targetSteps = 6_000), // achieved
            DailyStepCount(WEEK_START + 1, steps = 7_000, targetSteps = 6_000), // achieved
            DailyStepCount(WEEK_START + 2, steps = 1_000, targetSteps = 6_000), // not
            DailyStepCount(WEEK_START + 3, steps = 6_000, targetSteps = 6_000), // achieved
        )
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(2, awaitItem().longestAchievedStreak)
            awaitComplete()
        }
    }

    @Test
    fun `longestAchievedStreak is 7 when all days are achieved`() = runTest {
        val counts = List(7) { i -> DailyStepCount(WEEK_START + i, steps = 6_000, targetSteps = 6_000) }
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(7, awaitItem().longestAchievedStreak)
            awaitComplete()
        }
    }

    // totalDays

    @Test
    fun `totalDays is always 7`() = runTest {
        stubSummary(listOf(0, 0, 5_000, 0, 0, 0, 0))
        coEvery { getWeeklyBestHour(any()) } returns null

        useCase(WEEK_START).test {
            assertEquals(7, awaitItem().totalDays)
            awaitComplete()
        }
    }

    // helpers

    private fun stubSummary(steps: List<Int>) {
        val counts = steps.mapIndexed { i, s ->
            DailyStepCount(dateEpochDay = WEEK_START + i, steps = s, targetSteps = 6_000)
        }
        every { stepRepository.getWeeklyStepSummary(WEEK_START) } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = WEEK_START, dailyCounts = counts),
        )
    }

    private companion object {
        const val WEEK_START = 19_000L
    }
}
