package com.river.walklog.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.model.DailyStepCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.sign

private const val CALORIES_PER_STEP = 0.04f
private const val KILOMETERS_PER_STEP = 0.00075f

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getMonthlyRecap: GetMonthlyRecapUseCase,
    private val stepRepository: StepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private var currentYearMonth: YearMonth = YearMonth.now()
    private var collectJob: Job? = null
    private var selectedDayTimelineJob: Job? = null

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
                selectedDaySummary = selectedDay.toSelectedDaySummary(
                    previousDay = previousDay,
                    monthDays = dayItems,
                ),
            )
        }
        loadSelectedDayTimeline(selectedDay)
    }

    private fun loadMonth(yearMonth: YearMonth) {
        val today = YearMonth.now()
        collectJob?.cancel()
        selectedDayTimelineJob?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                items = emptyList(),
                yearMonth = yearMonth,
                selectedDateEpochDay = null,
                selectedDaySummary = null,
                canNavigateForward = yearMonth < today,
            )
        }
        collectJob = getMonthlyRecap(yearMonth.year, yearMonth.monthValue)
            .combine(userSettingsRepository.settings) { recap, settings -> recap to settings.dailyStepGoal }
            .onEach { (recap, todayGoal) ->
                val dailyCounts = recap.dailyCounts
                val selectedDateEpochDay = _state.value.selectedDateEpochDay
                val calendarItems = buildCalendarItems(
                    yearMonth = yearMonth,
                    dailyCounts = dailyCounts,
                    selectedDateEpochDay = selectedDateEpochDay,
                    todayGoal = todayGoal,
                )
                val dayItems = calendarItems.filterIsInstance<CalendarItem.Day>()
                val selectedDay = selectedDateEpochDay?.let { selectedEpochDay ->
                    dayItems.firstOrNull { it.dateEpochDay == selectedEpochDay }
                }
                val previousDay = selectedDay?.let { selected ->
                    dayItems.firstOrNull { it.dateEpochDay == selected.dateEpochDay - 1 }
                }
                val todayEpochDay = LocalDate.now().toEpochDay()
                val achievedDays = dailyCounts.count { count ->
                    if (count.dateEpochDay == todayEpochDay) count.steps >= todayGoal else count.steps >= count.targetSteps
                }
                val totalDays = recap.totalDays
                val achievedPct = if (totalDays > 0) (achievedDays * 100 / totalDays) else 0
                val totalSteps = recap.totalSteps
                _state.update { current ->
                    val preservedTimeline = current.selectedDaySummary
                        ?.takeIf { it.dateEpochDay == selectedDay?.dateEpochDay }
                        ?.timelineSegments
                        ?: emptyList()
                    current.copy(
                        items = calendarItems,
                        selectedDaySummary = selectedDay?.toSelectedDaySummary(
                            previousDay = previousDay,
                            monthDays = dayItems,
                        )?.copy(timelineSegments = preservedTimeline),
                        totalSteps = totalSteps,
                        achievementRatePct = achievedPct,
                        isLoading = false,
                        isEmpty = totalSteps == 0,
                    )
                }
                selectedDay?.let { loadSelectedDayTimeline(it) }
            }
            .catch { e ->
                crashReporter.log("History month load failed: ${e.message}")
                crashReporter.recordException(e)
            }
            .launchIn(viewModelScope)
    }

    private fun buildCalendarItems(
        yearMonth: YearMonth,
        dailyCounts: List<DailyStepCount>,
        selectedDateEpochDay: Long?,
        todayGoal: Int,
    ): List<CalendarItem> {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val firstDay = yearMonth.atDay(1)
        // 월요일=1, 일요일=7 → 월요일 기준 오프셋 (0~6)
        val startOffset = (firstDay.dayOfWeek.value - 1)
        val stepMap = dailyCounts.associateBy { it.dateEpochDay }

        val items = mutableListOf<CalendarItem>()

        // 요일 헤더
        (1..7).forEach { items.add(CalendarItem.DayLabel(it)) }

        // 앞쪽 빈 셀
        repeat(startOffset) { index -> items.add(CalendarItem.Empty(index)) }

        // 날짜 셀
        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            val epochDay = date.toEpochDay()
            val stepCount = stepMap[epochDay]
            val dayTargetSteps = if (epochDay == todayEpochDay) {
                todayGoal
            } else {
                stepCount?.targetSteps ?: DailyStepCount.DEFAULT_TARGET_STEPS
            }
            items.add(
                CalendarItem.Day(
                    dateEpochDay = epochDay,
                    dayNumber = day,
                    steps = stepCount?.steps ?: 0,
                    targetSteps = dayTargetSteps,
                    isAchieved = (stepCount?.steps ?: 0) >= dayTargetSteps,
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
        monthDays: List<CalendarItem.Day>,
    ): SelectedDaySummary {
        val calories = (steps * CALORIES_PER_STEP).toInt()
        val distance = steps * KILOMETERS_PER_STEP
        val activeDays = monthDays.filter { it.hasData && it.steps > 0 }
        val rank = if (hasData && activeDays.isNotEmpty()) {
            activeDays.count { it.steps > steps } + 1
        } else {
            null
        }
        return SelectedDaySummary(
            dateEpochDay = dateEpochDay,
            steps = steps,
            calories = calories,
            distanceKm = distance,
            remainingSteps = (targetSteps - steps).coerceAtLeast(0),
            comparisonDiff = buildComparisonDiff(previousDay),
            hasData = hasData,
            isAchieved = isAchieved,
            achievementFraction = if (hasData) (steps.toFloat() / targetSteps).coerceIn(0f, 1f) else 0f,
            comparisonSign = buildComparisonSign(previousDay),
            isPastDay = LocalDate.ofEpochDay(dateEpochDay).isBefore(LocalDate.now()),
            monthRank = rank,
            activeDaysInMonth = activeDays.size,
        )
    }

    private fun loadSelectedDayTimeline(selectedDay: CalendarItem.Day) {
        selectedDayTimelineJob?.cancel()
        if (!selectedDay.hasData) return

        selectedDayTimelineJob = viewModelScope.launch {
            runCatching {
                stepRepository.getHourlyStepsForRange(
                    fromEpochDay = selectedDay.dateEpochDay,
                    toEpochDay = selectedDay.dateEpochDay,
                )
            }.onSuccess { hourlySteps ->
                val timelineSegments = hourlySteps.toTimelineSegments()
                _state.update { state ->
                    if (state.selectedDateEpochDay != selectedDay.dateEpochDay) {
                        state
                    } else {
                        state.copy(
                            selectedDaySummary = state.selectedDaySummary?.copy(
                                timelineSegments = timelineSegments,
                            ),
                        )
                    }
                }
            }.onFailure { e ->
                crashReporter.log("History timeline load failed: ${e.message}")
                crashReporter.recordException(e)
            }
        }
    }

    private fun FloatArray.toTimelineSegments(): List<SelectedDayTimelineSegment> {
        if (size < 24 || all { it <= 0f }) return emptyList()

        val morningSteps = sliceArray(0..11).sum().toInt()
        val afternoonSteps = sliceArray(12..17).sum().toInt()
        val eveningSteps = sliceArray(18..23).sum().toInt()
        val segments = listOf(
            TimelineSegmentType.Morning to morningSteps,
            TimelineSegmentType.Afternoon to afternoonSteps,
            TimelineSegmentType.Evening to eveningSteps,
        )
        val maxSteps = segments.maxOf { it.second }.coerceAtLeast(1)

        return segments.map { (labelRes, steps) ->
            SelectedDayTimelineSegment(
                type = labelRes,
                steps = steps,
                fraction = steps.toFloat() / maxSteps,
            )
        }
    }

    private fun CalendarItem.Day.buildComparisonSign(previousDay: CalendarItem.Day?): Int? {
        if (!hasData || previousDay == null || !previousDay.hasData) return null
        val diff = steps - previousDay.steps
        return diff.sign
    }

    private fun CalendarItem.Day.buildComparisonDiff(previousDay: CalendarItem.Day?): Int? =
        if (hasData && previousDay?.hasData == true) steps - previousDay.steps else null
}
