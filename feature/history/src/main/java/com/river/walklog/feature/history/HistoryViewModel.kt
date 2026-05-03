package com.river.walklog.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.model.DailyStepCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sign

private val DAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")
private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
private val SELECTED_DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
private const val CALORIES_PER_STEP = 0.04f
private const val KILOMETERS_PER_STEP = 0.00075f

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getMonthlyRecap: GetMonthlyRecapUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private var currentYearMonth: YearMonth = YearMonth.now()
    private var collectJob: Job? = null

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.HISTORY)
        loadMonth(currentYearMonth)
    }

    fun onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1)
        loadMonth(currentYearMonth)
    }

    fun onNextMonth() {
        if (!_state.value.canNavigateForward) return
        currentYearMonth = currentYearMonth.plusMonths(1)
        loadMonth(currentYearMonth)
    }

    fun onDaySelected(dateEpochDay: Long) {
        val dayItems = _state.value.items.filterIsInstance<CalendarItem.Day>()
        val selectedDay = dayItems
            .firstOrNull { it.dateEpochDay == dateEpochDay }
            ?: return
        val previousDay = dayItems.firstOrNull { it.dateEpochDay == dateEpochDay - 1 }

        _state.update {
            it.copy(
                selectedDateEpochDay = dateEpochDay,
                items = it.items.map { item ->
                    if (item is CalendarItem.Day) {
                        item.copy(isSelected = item.dateEpochDay == dateEpochDay)
                    } else {
                        item
                    }
                },
                selectedDaySummary = selectedDay.toSelectedDaySummary(previousDay),
            )
        }
    }

    private fun loadMonth(yearMonth: YearMonth) {
        val today = YearMonth.now()
        collectJob?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                monthLabel = yearMonth.format(MONTH_FORMATTER),
                selectedDateEpochDay = null,
                selectedDaySummary = null,
                canNavigateForward = yearMonth < today,
            )
        }
        collectJob = getMonthlyRecap(yearMonth.year, yearMonth.monthValue)
            .onEach { recap ->
                val targetSteps = userSettingsRepository.settings.first().dailyStepGoal
                val dailyCounts = recap.dailyCounts
                val selectedDateEpochDay = _state.value.selectedDateEpochDay
                val calendarItems = buildCalendarItems(
                    yearMonth = yearMonth,
                    dailyCounts = dailyCounts,
                    targetSteps = targetSteps,
                    selectedDateEpochDay = selectedDateEpochDay,
                )
                val dayItems = calendarItems.filterIsInstance<CalendarItem.Day>()
                val selectedDay = selectedDateEpochDay?.let { selectedEpochDay ->
                    dayItems.firstOrNull { it.dateEpochDay == selectedEpochDay }
                }
                val previousDay = selectedDay?.let { selected ->
                    dayItems.firstOrNull { it.dateEpochDay == selected.dateEpochDay - 1 }
                }
                val achievedDays = dailyCounts.count { it.steps >= targetSteps }
                val totalDays = recap.totalDays
                val achievedPct = if (totalDays > 0) (achievedDays * 100 / totalDays) else 0
                val totalSteps = recap.totalSteps
                _state.update {
                    it.copy(
                        items = calendarItems,
                        totalStepsText = "%,d 보".format(totalSteps),
                        achievementRateText = "$achievedPct%",
                        selectedDaySummary = selectedDay?.toSelectedDaySummary(previousDay),
                        isLoading = false,
                        isEmpty = totalSteps == 0,
                    )
                }
            }
            .catch { e -> crashReporter.recordException(e) }
            .launchIn(viewModelScope)
    }

    private fun buildCalendarItems(
        yearMonth: YearMonth,
        dailyCounts: List<DailyStepCount>,
        targetSteps: Int,
        selectedDateEpochDay: Long?,
    ): List<CalendarItem> {
        val today = LocalDate.now()
        val firstDay = yearMonth.atDay(1)
        // 월요일=1, 일요일=7 → 월요일 기준 오프셋 (0~6)
        val startOffset = (firstDay.dayOfWeek.value - 1)
        val stepMap = dailyCounts.associateBy { it.dateEpochDay }

        val items = mutableListOf<CalendarItem>()

        // 요일 헤더
        DAY_LABELS.forEach { items.add(CalendarItem.DayLabel(it)) }

        // 앞쪽 빈 셀
        repeat(startOffset) { index -> items.add(CalendarItem.Empty(index)) }

        // 날짜 셀
        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            val epochDay = date.toEpochDay()
            val stepCount = stepMap[epochDay]
            items.add(
                CalendarItem.Day(
                    dateEpochDay = epochDay,
                    dayNumber = day,
                    steps = stepCount?.steps ?: 0,
                    targetSteps = targetSteps,
                    isAchieved = (stepCount?.steps ?: 0) >= targetSteps,
                    isToday = date == today,
                    hasData = stepCount != null && stepCount.steps > 0,
                    isSelected = epochDay == selectedDateEpochDay,
                ),
            )
        }

        return items
    }

    private fun CalendarItem.Day.toSelectedDaySummary(
        previousDay: CalendarItem.Day?,
    ): SelectedDaySummary {
        val calories = (steps * CALORIES_PER_STEP).toInt()
        val distance = steps * KILOMETERS_PER_STEP
        return SelectedDaySummary(
            dateText = LocalDate.ofEpochDay(dateEpochDay).format(SELECTED_DATE_FORMATTER),
            stepsText = "%,d".format(steps),
            caloriesText = "%,d kcal".format(calories),
            distanceText = String.format(Locale.KOREAN, "%.1f km", distance),
            targetStatusText = buildTargetStatusText(),
            comparisonText = buildComparisonText(previousDay),
            hasData = hasData,
            isAchieved = isAchieved,
            achievementFraction = if (hasData) (steps.toFloat() / targetSteps).coerceIn(0f, 1f) else 0f,
            comparisonSign = buildComparisonSign(previousDay),
            isPastDay = LocalDate.ofEpochDay(dateEpochDay).isBefore(LocalDate.now()),
        )
    }

    private fun CalendarItem.Day.buildTargetStatusText(): String = when {
        !hasData -> "이날은 걸음 기록이 없어요"
        isAchieved -> "목표 달성"
        else -> "목표까지 %,d보".format(targetSteps - steps)
    }

    private fun CalendarItem.Day.buildComparisonSign(previousDay: CalendarItem.Day?): Int? {
        if (!hasData || previousDay == null || !previousDay.hasData) return null
        val diff = steps - previousDay.steps
        return diff.sign
    }

    private fun CalendarItem.Day.buildComparisonText(previousDay: CalendarItem.Day?): String {
        if (!hasData) return "기록이 없어 전날 비교가 없어요"
        if (previousDay == null || !previousDay.hasData) return "전날 기록 없음"

        val diff = steps - previousDay.steps
        return when {
            diff > 0 -> "전날보다 +%,d보".format(diff)
            diff < 0 -> "전날보다 %,d보".format(diff)
            else -> "전날과 같아요"
        }
    }
}
