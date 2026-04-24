package com.river.walklog.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyStepSummaryUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeeklyReportViewModel @Inject constructor(
    private val getWeeklyStepSummary: GetWeeklyStepSummaryUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyReportState())
    val state: StateFlow<WeeklyReportState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.WEEKLY_REPORT)
        collectWeeklySummary()
    }

    fun handleIntent(intent: WeeklyReportIntent) {
        when (intent) {
            WeeklyReportIntent.OnClickBack -> Unit
            WeeklyReportIntent.OnClickShare -> Unit
        }
    }

    fun setSharing(isSharing: Boolean) {
        _state.update { it.copy(isSharing = isSharing) }
    }

    private fun collectWeeklySummary() {
        getWeeklyStepSummary()
            .onEach { summary -> _state.update { it.applyWeeklySummary(summary) } }
            .catch { e ->
                crashReporter.recordException(e)
                _state.update { it.copy(isLoading = false, isError = true) }
            }
            .launchIn(viewModelScope)
    }

    private fun WeeklyReportState.applyWeeklySummary(summary: WeeklyStepSummary): WeeklyReportState {
        val weekStart = LocalDate.ofEpochDay(summary.weekStartEpochDay)
        val weekEnd = weekStart.plusDays(6)
        val rangeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.KOREAN)
        val monthFormatter = DateTimeFormatter.ofPattern("M월", Locale.KOREAN)
        val dateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        val weekOfMonth = (weekStart.dayOfMonth - 1) / 7 + 1

        val stepMap = summary.dailyCounts.associateBy { it.dateEpochDay }
        val weekCounts = (0L..6L).map { offset ->
            val epochDay = summary.weekStartEpochDay + offset
            stepMap[epochDay] ?: DailyStepCount(dateEpochDay = epochDay, steps = 0)
        }

        val baseState = copy(
            weekRangeText = "${weekStart.format(monthFormatter)} ${weekOfMonth}주차 · ${weekStart.format(rangeFormatter)}~${weekEnd.format(rangeFormatter)}",
            dateRangeSubtitle = "${weekStart.format(dateFormatter)} — ${weekEnd.format(dateFormatter)}",
            isLoading = false,
            isError = false,
        )

        if (weekCounts.none { it.steps > 0 }) {
            return baseState.copy(isEmpty = true)
        }

        val achievedDays = weekCounts.count { it.isAchieved }
        val achievementRate = achievedDays / 7f
        val achievementPct = (achievementRate * 100).toInt()
        val bestDayName = summary.bestDay?.let { best ->
            LocalDate.ofEpochDay(best.dateEpochDay)
                .format(DateTimeFormatter.ofPattern("EEEE", Locale.KOREAN))
        } ?: "-"

        return baseState.copy(
            totalStepsText = "%,d보".format(summary.totalSteps),
            achievementRateText = "$achievementPct%",
            achievedDays = achievedDays,
            totalDays = 7,
            achievementRate = achievementRate,
            bestDayText = bestDayName,
            dailyCounts = weekCounts,
            summaryMessage = when {
                achievementPct >= 100 -> "이번 주 목표를 모두 달성했어요!"
                achievementPct >= 70 -> "훌륭해요! 목표에 가까워지고 있어요"
                else -> "꾸준히 걷고 있어요, 파이팅!"
            },
            isEmpty = false,
        )
    }
}
