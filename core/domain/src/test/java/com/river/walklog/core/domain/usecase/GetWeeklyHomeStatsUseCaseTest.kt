package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import com.river.walklog.core.model.WeeklyStepSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetWeeklyHomeStatsUseCaseTest {

    private lateinit var getWeeklyStepSummary: GetWeeklyStepSummaryUseCase
    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var useCase: GetWeeklyHomeStatsUseCase

    @Before
    fun setUp() {
        getWeeklyStepSummary = mockk()
        userSettingsRepository = mockk()
        useCase = GetWeeklyHomeStatsUseCase(getWeeklyStepSummary, userSettingsRepository)
    }

    // totalSteps

    @Test
    fun `totalSteps is sum of all daily step counts`() = runTest {
        stubSummary(listOf(3_000, 7_000))
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(10_000, awaitItem().totalSteps)
            awaitComplete()
        }
    }

    @Test
    fun `totalSteps is 0 when daily counts are empty`() = runTest {
        stubSummary(emptyList())
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(0, awaitItem().totalSteps)
            awaitComplete()
        }
    }

    // achievementPct

    @Test
    fun `achievementPct is 0 when no day reaches the daily step goal`() = runTest {
        stubSummary(listOf(1_000, 2_000, 3_000))
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(0, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    @Test
    fun `achievementPct is 100 when every day reaches the daily step goal`() = runTest {
        stubSummary(listOf(6_000, 7_000, 8_000))
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(100, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    @Test
    fun `achievementPct floors to nearest integer`() = runTest {
        // 1 out of 3 days achieved → 1 * 100 / 3 = 33
        stubSummary(listOf(1_000, 1_000, 6_000))
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(33, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    @Test
    fun `achievementPct is 0 when daily counts are empty`() = runTest {
        stubSummary(emptyList())
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(0, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    @Test
    fun `achievementPct is 0 when dailyStepGoal is 0`() = runTest {
        stubSummary(listOf(6_000, 7_000))
        stubSettings(dailyStepGoal = 0)

        useCase().test {
            assertEquals(0, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    @Test
    fun `a day exactly meeting the goal counts as achieved`() = runTest {
        stubSummary(listOf(6_000))
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(100, awaitItem().achievementPct)
            awaitComplete()
        }
    }

    // bestDayEpochDay

    @Test
    fun `bestDayEpochDay is the epoch day of the day with the most steps`() = runTest {
        val counts = listOf(
            DailyStepCount(dateEpochDay = 19_000L, steps = 3_000),
            DailyStepCount(dateEpochDay = 19_001L, steps = 9_000),
            DailyStepCount(dateEpochDay = 19_002L, steps = 5_000),
        )
        every { getWeeklyStepSummary() } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = 19_000L, dailyCounts = counts),
        )
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(19_001L, awaitItem().bestDayEpochDay)
            awaitComplete()
        }
    }

    @Test
    fun `bestDayEpochDay is null when daily counts are empty`() = runTest {
        stubSummary(emptyList())
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertNull(awaitItem().bestDayEpochDay)
            awaitComplete()
        }
    }

    @Test
    fun `bestDayEpochDay is null when all days have zero steps`() = runTest {
        stubSummary(listOf(0, 0, 0), weekStart = 19_000L)
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertNull(awaitItem().bestDayEpochDay)
            awaitComplete()
        }
    }

    @Test
    fun `bestDayEpochDay picks the first day when all days have equal steps`() = runTest {
        stubSummary(listOf(5_000, 5_000, 5_000), weekStart = 19_000L)
        stubSettings(dailyStepGoal = 6_000)

        useCase().test {
            assertEquals(19_000L, awaitItem().bestDayEpochDay)
            awaitComplete()
        }
    }

    // helpers

    private fun stubSummary(steps: List<Int>, weekStart: Long = 19_000L) {
        val counts = steps.mapIndexed { i, s ->
            DailyStepCount(dateEpochDay = weekStart + i, steps = s)
        }
        every { getWeeklyStepSummary() } returns flowOf(
            WeeklyStepSummary(weekStartEpochDay = weekStart, dailyCounts = counts),
        )
    }

    private fun stubSettings(dailyStepGoal: Int) {
        every { userSettingsRepository.settings } returns flowOf(
            UserSettings(
                isOnboardingCompleted = false,
                nickname = "",
                totalPoints = 0,
                dailyStepGoal = dailyStepGoal,
                notificationsEnabled = true,
                recoveryMissionSteps = 6_000,
                themeMode = ThemeMode.SYSTEM,
                lastDailyMissionAwardedDate = "",
                lastRecoveryMissionAwardedDate = "",
            ),
        )
    }
}
