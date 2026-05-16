package com.river.walklog.feature.history

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeStepRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var getMonthlyRecap: GetMonthlyRecapUseCase
    private lateinit var stepRepository: FakeStepRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        getMonthlyRecap = mockk()
        stepRepository = FakeStepRepository()
        userSettingsRepository = FakeUserSettingsRepository()
        crashReporter = mockk(relaxed = true)
    }

    @Test
    fun `initial state exposes current year month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        assertEquals(YearMonth.now(), viewModel.state.value.yearMonth)
    }

    @Test
    fun `canNavigateForward is false for current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.canNavigateForward)
    }

    @Test
    fun `isEmpty is true when recap has zero totalSteps`() = runTest {
        stubRecap(emptyRecap(totalSteps = 0))
        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `isEmpty is false when recap has steps`() = runTest {
        stubRecap(emptyRecap(totalSteps = 50_000))
        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.isEmpty)
    }

    // totalSteps

    @Test
    fun `totalSteps exposes recap total steps`() = runTest {
        stubRecap(emptyRecap(totalSteps = 123_456))
        val viewModel = createViewModel()

        assertEquals(123_456, viewModel.state.value.totalSteps)
    }

    @Test
    fun `totalSteps is 0 for empty recap`() = runTest {
        stubRecap(emptyRecap(totalSteps = 0))
        val viewModel = createViewModel()

        assertEquals(0, viewModel.state.value.totalSteps)
    }

    // achievementRateText

    @Test
    fun `achievementRateText is 50 percent for half achieved month`() = runTest {
        val today = YearMonth.now()
        val dailyCounts = (1..15).map { day ->
            DailyStepCount(
                dateEpochDay = today.atDay(day).toEpochDay(),
                steps = 10_000,
            )
        }
        stubRecap(emptyRecap(achievedDays = 15, totalDays = 30, dailyCounts = dailyCounts))
        val viewModel = createViewModel()

        assertEquals("50%", viewModel.state.value.achievementRateText)
    }

    @Test
    fun `achievementRateText is 0 percent for empty recap`() = runTest {
        stubRecap(emptyRecap(achievedDays = 0, totalDays = 0))
        val viewModel = createViewModel()

        assertEquals("0%", viewModel.state.value.achievementRateText)
    }

    @Test
    fun `achievementRateText is 100 percent for fully achieved month`() = runTest {
        val today = YearMonth.now()
        val dailyCounts = (1..28).map { day ->
            DailyStepCount(
                dateEpochDay = today.atDay(day).toEpochDay(),
                steps = 10_000,
            )
        }
        stubRecap(emptyRecap(achievedDays = 28, totalDays = 28, dailyCounts = dailyCounts))
        val viewModel = createViewModel()

        assertEquals("100%", viewModel.state.value.achievementRateText)
    }

    // 달력

    @Test
    fun `calendar items start with 7 day labels`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        val labels = viewModel.state.value.items.take(7)
        assertTrue(labels.all { it is CalendarItem.DayLabel })
        assertEquals(
            (1..7).toList(),
            labels.map { (it as CalendarItem.DayLabel).dayOfWeek },
        )
    }

    @Test
    fun `calendar items contain correct number of Day cells for current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        val dayItems = viewModel.state.value.items.filterIsInstance<CalendarItem.Day>()
        assertEquals(YearMonth.now().lengthOfMonth(), dayItems.size)
    }

    @Test
    fun `day items are numbered 1 to length of month in order`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        val dayNumbers = viewModel.state.value.items
            .filterIsInstance<CalendarItem.Day>()
            .map { it.dayNumber }
        assertEquals((1..YearMonth.now().lengthOfMonth()).toList(), dayNumbers)
    }

    @Test
    fun `day item has correct steps from dailyCounts`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val recap = emptyRecap(
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 7_500),
            ),
        )
        stubRecap(recap)
        val viewModel = createViewModel()

        val firstDay = viewModel.state.value.items
            .filterIsInstance<CalendarItem.Day>()
            .first()
        assertEquals(7_500, firstDay.steps)
        assertTrue(firstDay.hasData)
    }

    @Test
    fun `day item with no data has 0 steps and hasData false`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        val firstDay = viewModel.state.value.items
            .filterIsInstance<CalendarItem.Day>()
            .first()
        assertEquals(0, firstDay.steps)
        assertFalse(firstDay.hasData)
    }

    @Test
    fun `onDaySelected shows selected day summary`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val recap = emptyRecap(
            totalSteps = 10_000,
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 10_000),
            ),
        )
        stubRecap(recap)
        val viewModel = createViewModel()

        viewModel.onDaySelected(firstDayEpoch)

        val summary = viewModel.state.value.selectedDaySummary
        assertEquals(10_000, summary?.steps)
        assertEquals(400, summary?.calories)
        assertEquals(7.5f, summary?.distanceKm)
        assertEquals(0, summary?.remainingSteps)
        assertEquals(null, summary?.comparisonDiff)
        assertEquals(true, summary?.hasData)
        assertEquals(true, summary?.isAchieved)
        assertEquals(1, summary?.monthRank)
        assertEquals(1, summary?.activeDaysInMonth)
    }

    @Test
    fun `onDaySelected marks selected calendar day`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        stubRecap(emptyRecap(totalSteps = 1))
        val viewModel = createViewModel()

        viewModel.onDaySelected(firstDayEpoch)

        val dayItems = viewModel.state.value.items.filterIsInstance<CalendarItem.Day>()
        assertTrue(dayItems.first { it.dateEpochDay == firstDayEpoch }.isSelected)
        assertFalse(dayItems.drop(1).any { it.isSelected })
    }

    @Test
    fun `onDaySelected shows remaining steps when target is not achieved`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val recap = emptyRecap(
            totalSteps = 4_500,
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 4_500, targetSteps = 10_000),
            ),
        )
        stubRecap(recap)
        val viewModel = createViewModel()

        viewModel.onDaySelected(firstDayEpoch)

        assertEquals(5_500, viewModel.state.value.selectedDaySummary?.remainingSteps)
    }

    @Test
    fun `selected day remains selected when month data updates`() = runTest {
        val month = YearMonth.now()
        val firstDayEpoch = month.atDay(1).toEpochDay()
        val updates = MutableSharedFlow<MonthlyRecap>(replay = 1)
        every { getMonthlyRecap(any(), any()) } returns updates
        updates.tryEmit(
            emptyRecap(
                totalSteps = 8_000,
                dailyCounts = listOf(
                    DailyStepCount(dateEpochDay = firstDayEpoch, steps = 8_000),
                ),
            ),
        )

        val viewModel = createViewModel()
        viewModel.onDaySelected(firstDayEpoch)

        updates.emit(
            emptyRecap(
                totalSteps = 9_000,
                dailyCounts = listOf(
                    DailyStepCount(dateEpochDay = firstDayEpoch, steps = 9_000),
                ),
            ),
        )

        val selectedDay = viewModel.state.value.items
            .filterIsInstance<CalendarItem.Day>()
            .first { it.dateEpochDay == firstDayEpoch }

        assertTrue(selectedDay.isSelected)
        assertEquals(firstDayEpoch, viewModel.state.value.selectedDateEpochDay)
        assertEquals(9_000, viewModel.state.value.selectedDaySummary?.steps)
    }

    @Test
    fun `onDaySelected shows previous day comparison`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val secondDayEpoch = today.atDay(2).toEpochDay()
        val recap = emptyRecap(
            totalSteps = 13_200,
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 5_000, targetSteps = 10_000),
                DailyStepCount(dateEpochDay = secondDayEpoch, steps = 8_200, targetSteps = 10_000),
            ),
        )
        stubRecap(recap)
        val viewModel = createViewModel()

        viewModel.onDaySelected(secondDayEpoch)

        assertEquals(3_200, viewModel.state.value.selectedDaySummary?.comparisonDiff)
        assertEquals(1, viewModel.state.value.selectedDaySummary?.comparisonSign)
        assertEquals(1, viewModel.state.value.selectedDaySummary?.monthRank)
        assertEquals(2, viewModel.state.value.selectedDaySummary?.activeDaysInMonth)
    }

    @Test
    fun `onDaySelected loads selected day timeline from hourly steps`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val hourlySteps = FloatArray(24)
        hourlySteps[8] = 1_200f
        hourlySteps[13] = 2_000f
        hourlySteps[19] = 800f
        stepRepository.setHourlySteps(hourlySteps)
        stubRecap(
            emptyRecap(
                totalSteps = 4_000,
                dailyCounts = listOf(
                    DailyStepCount(dateEpochDay = firstDayEpoch, steps = 4_000),
                ),
            ),
        )
        val viewModel = createViewModel()

        viewModel.onDaySelected(firstDayEpoch)

        val segments = viewModel.state.value.selectedDaySummary?.timelineSegments.orEmpty()
        assertEquals(
            listOf(
                TimelineSegmentType.Morning,
                TimelineSegmentType.Afternoon,
                TimelineSegmentType.Evening,
            ),
            segments.map { it.type },
        )
        assertEquals(
            listOf(1_200, 2_000, 800),
            segments.map { it.steps },
        )
        assertEquals(1f, segments[1].fraction)
    }

    @Test
    fun `onDaySelected shows no record message for day without data`() = runTest {
        val today = YearMonth.now()
        val secondDayEpoch = today.atDay(2).toEpochDay()
        stubRecap(emptyRecap(totalSteps = 1))
        val viewModel = createViewModel()

        viewModel.onDaySelected(secondDayEpoch)

        val summary = viewModel.state.value.selectedDaySummary
        assertEquals(false, summary?.isAchieved)
        assertEquals(null, summary?.comparisonDiff)
        assertEquals(false, summary?.hasData)
    }

    // 월 이동

    @Test
    fun `onPreviousMonth exposes previous year month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        viewModel.onPreviousMonth()

        assertEquals(YearMonth.now().minusMonths(1), viewModel.state.value.yearMonth)
    }

    @Test
    fun `onPreviousMonth enables canNavigateForward`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()

        viewModel.onPreviousMonth()

        assertTrue(viewModel.state.value.canNavigateForward)
    }

    @Test
    fun `onNextMonth is no-op when canNavigateForward is false`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()
        val monthBefore = viewModel.state.value.yearMonth

        viewModel.onNextMonth()

        assertEquals(monthBefore, viewModel.state.value.yearMonth)
    }

    @Test
    fun `onNextMonth after onPreviousMonth returns to current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = createViewModel()
        val originalMonth = viewModel.state.value.yearMonth

        viewModel.onPreviousMonth()
        viewModel.onNextMonth()

        assertEquals(originalMonth, viewModel.state.value.yearMonth)
    }

    // helper

    private fun stubRecap(recap: MonthlyRecap) {
        every { getMonthlyRecap(any(), any()) } returns flowOf(recap)
    }

    private fun createViewModel(): HistoryViewModel = HistoryViewModel(
        getMonthlyRecap = getMonthlyRecap,
        stepRepository = stepRepository,
        userSettingsRepository = userSettingsRepository,
        crashReporter = crashReporter,
    )

    private fun emptyRecap(
        totalSteps: Int = 0,
        achievedDays: Int = 0,
        totalDays: Int = 30,
        dailyCounts: List<DailyStepCount> = emptyList(),
    ): MonthlyRecap {
        val today = YearMonth.now()
        return MonthlyRecap(
            year = today.year,
            month = today.monthValue,
            totalSteps = totalSteps,
            averageStepsPerDay = 0,
            bestDay = null,
            achievedDays = achievedDays,
            totalDays = totalDays,
            longestStreak = 0,
            activeDays = 0,
            estimatedCalories = 0,
            dailyCounts = dailyCounts,
        )
    }
}
