package com.river.walklog.feature.history

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.domain.usecase.GetUserSettingsUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import com.river.walklog.core.model.UserSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getMonthlyRecap: GetMonthlyRecapUseCase
    private lateinit var getUserSettings: GetUserSettingsUseCase
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getMonthlyRecap = mockk()
        getUserSettings = mockk()
        crashReporter = mockk(relaxed = true)
        every { getUserSettings() } returns flowOf(UserSettings())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── 초기 상태 ─────────────────────────────────────────────────────────

    @Test
    fun `initial state shows current month label`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        val today = YearMonth.now()
        assertTrue(viewModel.state.value.monthLabel.contains("${today.year}년"))
        assertTrue(viewModel.state.value.monthLabel.contains("${today.monthValue}월"))
    }

    @Test
    fun `canNavigateForward is false for current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertFalse(viewModel.state.value.canNavigateForward)
    }

    @Test
    fun `isEmpty is true when recap has zero totalSteps`() = runTest {
        stubRecap(emptyRecap(totalSteps = 0))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `isEmpty is false when recap has steps`() = runTest {
        stubRecap(emptyRecap(totalSteps = 50_000))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertFalse(viewModel.state.value.isEmpty)
    }

    // ─── totalStepsText ────────────────────────────────────────────────────

    @Test
    fun `totalStepsText formats steps with comma separator`() = runTest {
        stubRecap(emptyRecap(totalSteps = 123_456))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertEquals("123,456 보", viewModel.state.value.totalStepsText)
    }

    @Test
    fun `totalStepsText is 0 보 for empty recap`() = runTest {
        stubRecap(emptyRecap(totalSteps = 0))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertEquals("0 보", viewModel.state.value.totalStepsText)
    }

    // ─── achievementRateText ───────────────────────────────────────────────

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
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertEquals("50%", viewModel.state.value.achievementRateText)
    }

    @Test
    fun `achievementRateText is 0 percent for empty recap`() = runTest {
        stubRecap(emptyRecap(achievedDays = 0, totalDays = 0))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

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
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        assertEquals("100%", viewModel.state.value.achievementRateText)
    }

    // ─── 달력 아이템 구조 ──────────────────────────────────────────────────

    @Test
    fun `calendar items start with 7 day labels`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        val labels = viewModel.state.value.items.take(7)
        assertTrue(labels.all { it is CalendarItem.DayLabel })
        assertEquals(
            listOf("월", "화", "수", "목", "금", "토", "일"),
            labels.map { (it as CalendarItem.DayLabel).label },
        )
    }

    @Test
    fun `calendar items contain correct number of Day cells for current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        val dayItems = viewModel.state.value.items.filterIsInstance<CalendarItem.Day>()
        assertEquals(YearMonth.now().lengthOfMonth(), dayItems.size)
    }

    @Test
    fun `day items are numbered 1 to length of month in order`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

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
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        val firstDay = viewModel.state.value.items
            .filterIsInstance<CalendarItem.Day>()
            .first()
        assertEquals(7_500, firstDay.steps)
        assertTrue(firstDay.hasData)
    }

    @Test
    fun `day item with no data has 0 steps and hasData false`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

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
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onDaySelected(firstDayEpoch)

        val summary = viewModel.state.value.selectedDaySummary
        assertEquals("10,000 보", summary?.stepsText)
        assertEquals("400 kcal", summary?.caloriesText)
        assertEquals("7.5 km", summary?.distanceText)
        assertEquals("목표 달성", summary?.targetStatusText)
        assertEquals("전날 기록 없음", summary?.comparisonText)
        assertEquals(true, summary?.hasData)
    }

    @Test
    fun `onDaySelected marks selected calendar day`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        stubRecap(emptyRecap(totalSteps = 1))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

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
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 4_500, targetSteps = 6_000),
            ),
        )
        stubRecap(recap)
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onDaySelected(firstDayEpoch)

        assertEquals("목표까지 5,500보", viewModel.state.value.selectedDaySummary?.targetStatusText)
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

        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)
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
        assertEquals("9,000 보", viewModel.state.value.selectedDaySummary?.stepsText)
    }

    @Test
    fun `onDaySelected shows previous day comparison`() = runTest {
        val today = YearMonth.now()
        val firstDayEpoch = today.atDay(1).toEpochDay()
        val secondDayEpoch = today.atDay(2).toEpochDay()
        val recap = emptyRecap(
            totalSteps = 13_200,
            dailyCounts = listOf(
                DailyStepCount(dateEpochDay = firstDayEpoch, steps = 5_000),
                DailyStepCount(dateEpochDay = secondDayEpoch, steps = 8_200),
            ),
        )
        stubRecap(recap)
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onDaySelected(secondDayEpoch)

        assertEquals("전날보다 +3,200보", viewModel.state.value.selectedDaySummary?.comparisonText)
    }

    @Test
    fun `onDaySelected shows no record message for day without data`() = runTest {
        val today = YearMonth.now()
        val secondDayEpoch = today.atDay(2).toEpochDay()
        stubRecap(emptyRecap(totalSteps = 1))
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onDaySelected(secondDayEpoch)

        val summary = viewModel.state.value.selectedDaySummary
        assertEquals("이날은 걸음 기록이 없어요", summary?.targetStatusText)
        assertEquals("기록이 없어 전날 비교가 없어요", summary?.comparisonText)
        assertEquals(false, summary?.hasData)
    }

    // ─── 월 이동 ───────────────────────────────────────────────────────────

    @Test
    fun `onPreviousMonth shows previous month label`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onPreviousMonth()

        val prevMonth = YearMonth.now().minusMonths(1)
        assertTrue(viewModel.state.value.monthLabel.contains("${prevMonth.year}년"))
        assertTrue(viewModel.state.value.monthLabel.contains("${prevMonth.monthValue}월"))
    }

    @Test
    fun `onPreviousMonth enables canNavigateForward`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)

        viewModel.onPreviousMonth()

        assertTrue(viewModel.state.value.canNavigateForward)
    }

    @Test
    fun `onNextMonth is no-op when canNavigateForward is false`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)
        val labelBefore = viewModel.state.value.monthLabel

        viewModel.onNextMonth()

        assertEquals(labelBefore, viewModel.state.value.monthLabel)
    }

    @Test
    fun `onNextMonth after onPreviousMonth returns to current month`() = runTest {
        stubRecap(emptyRecap())
        val viewModel = HistoryViewModel(getMonthlyRecap, getUserSettings, crashReporter)
        val originalLabel = viewModel.state.value.monthLabel

        viewModel.onPreviousMonth()
        viewModel.onNextMonth()

        assertEquals(originalLabel, viewModel.state.value.monthLabel)
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun stubRecap(recap: MonthlyRecap) {
        every { getMonthlyRecap(any(), any()) } returns flowOf(recap)
    }

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
