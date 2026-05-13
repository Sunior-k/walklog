package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.WalkingInsightsRepository
import com.river.walklog.core.model.WalkingInsightsAnalysis
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetWalkingInsightsUseCaseTest {

    private lateinit var stepRepository: StepRepository
    private lateinit var walkingInsightsRepository: WalkingInsightsRepository
    private lateinit var useCase: GetWalkingInsightsUseCase

    @Before
    fun setUp() {
        stepRepository = mockk()
        walkingInsightsRepository = mockk()
        useCase = GetWalkingInsightsUseCase(stepRepository, walkingInsightsRepository)
    }

    // null guard — MIN_DAYS_FOR_INSIGHTS = 3

    @Test
    fun `returns null when no days have any step data`() = runTest {
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns FloatArray(7 * 24)

        assertNull(useCase(6_000, 12))
    }

    @Test
    fun `returns null when only one day has step data`() = runTest {
        val hourlySteps = FloatArray(7 * 24)
        hourlySteps[0] = 500f // day 0 only
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps

        assertNull(useCase(6_000, 12))
    }

    @Test
    fun `returns null when only two days have step data`() = runTest {
        val hourlySteps = FloatArray(7 * 24)
        hourlySteps[0] = 500f  // day 0
        hourlySteps[24] = 500f // day 1
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps

        assertNull(useCase(6_000, 12))
    }

    @Test
    fun `returns non-null when exactly three days have step data`() = runTest {
        val hourlySteps = threeActiveDaysHourlySteps()
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps
        stubAnalysis(peakHour = 10)

        assertNotNull(useCase(6_000, 12))
    }

    // activeDays

    @Test
    fun `activeDays counts days that have at least one non-zero hour`() = runTest {
        val hourlySteps = FloatArray(7 * 24)
        hourlySteps[0] = 100f  // day 0
        hourlySteps[24] = 200f // day 1
        hourlySteps[48] = 150f // day 2
        // days 3–6: all zeros
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps
        stubAnalysis(peakHour = 0)

        assertEquals(3, useCase(6_000, 12)?.activeDays)
    }

    @Test
    fun `activeDays is 7 when all days have step data`() = runTest {
        val hourlySteps = FloatArray(7 * 24) { 100f }
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps
        stubAnalysis(peakHour = 0)

        assertEquals(7, useCase(6_000, 12)?.activeDays)
    }

    // totalDays

    @Test
    fun `totalDays is always 7`() = runTest {
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns threeActiveDaysHourlySteps()
        stubAnalysis(peakHour = 0)

        assertEquals(7, useCase(6_000, 12)?.totalDays)
    }

    // hourlyAverages

    @Test
    fun `hourlyAverages contains exactly 24 elements`() = runTest {
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns threeActiveDaysHourlySteps()
        stubAnalysis(peakHour = 0)

        assertEquals(24, useCase(6_000, 12)?.hourlyAverages?.size)
    }

    @Test
    fun `hourlyAverages are computed as mean across all 7 days for each hour`() = runTest {
        val hourlySteps = FloatArray(7 * 24)
        // Place 700 steps at hour 5 for every day
        for (day in 0 until 7) hourlySteps[day * 24 + 5] = 700f
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps
        stubAnalysis(peakHour = 5)

        val result = useCase(6_000, 12)
        assertEquals(700f, result?.hourlyAverages?.get(5))
    }

    // averageStepsAtPeakHour

    @Test
    fun `averageStepsAtPeakHour is average steps at peakHour across all 7 days`() = runTest {
        val peakHour = 10
        val hourlySteps = FloatArray(7 * 24)
        // 100 steps at peakHour for every day → avg = 100
        for (day in 0 until 7) hourlySteps[day * 24 + peakHour] = 100f
        // Make 3 days active via other hours too
        hourlySteps[0] = 50f
        hourlySteps[24] = 50f
        hourlySteps[48] = 50f
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns hourlySteps
        stubAnalysis(peakHour = peakHour)

        assertEquals(100, useCase(6_000, 12)?.averageStepsAtPeakHour)
    }

    @Test
    fun `averageStepsAtPeakHour is null when peakHour is out of 0-23 range`() = runTest {
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns threeActiveDaysHourlySteps()
        stubAnalysis(peakHour = -1)

        assertNull(useCase(6_000, 12)?.averageStepsAtPeakHour)
    }

    // streakRisk passthrough

    @Test
    fun `streakRisk is delegated from WalkingInsightsAnalysis`() = runTest {
        coEvery { stepRepository.getHourlyStepsForRange(any(), any()) } returns threeActiveDaysHourlySteps()
        stubAnalysis(peakHour = 0, streakRisk = 0.75f)

        assertEquals(0.75f, useCase(6_000, 12)?.streakRisk)
    }

    // helpers

    private fun threeActiveDaysHourlySteps(): FloatArray {
        val hourlySteps = FloatArray(7 * 24)
        hourlySteps[0] = 100f  // day 0
        hourlySteps[24] = 200f // day 1
        hourlySteps[48] = 150f // day 2
        return hourlySteps
    }

    private fun stubAnalysis(peakHour: Int, streakRisk: Float = 0.2f) {
        every {
            walkingInsightsRepository.analyzeHourlySteps(any(), any(), any())
        } returns WalkingInsightsAnalysis(
            peakHour = peakHour,
            weeklyTrend = 1f,
            recoveryDifficulty = 0.5f,
            streakRisk = streakRisk,
        )
    }
}
