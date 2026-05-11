package com.river.walklog.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyBestHourUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyStepSummaryUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeeklyReportDetailViewModel @Inject constructor(
    private val getWeeklyStepSummary: GetWeeklyStepSummaryUseCase,
    private val getWeeklyBestHour: GetWeeklyBestHourUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyReportDetailState())
    val state: StateFlow<WeeklyReportDetailState> = _state.asStateFlow()
    private var detailJob: Job? = null

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.WEEKLY_REPORT)
    }

    fun startSharing() {
        _state.update { it.copy(isSharing = true) }
    }

    fun completeSharing() {
        _state.update { it.copy(isSharing = false) }
    }

    fun failSharing() {
        _state.update {
            it.copy(
                isSharing = false,
                userMessage = WeeklyReportUserMessage.ShareFailed,
            )
        }
    }

    fun clearUserMessage() {
        _state.update { it.copy(userMessage = null) }
    }

    fun loadReport(weekStartEpochDay: Long) {
        detailJob?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                isError = false,
                isEmpty = false,
                isSharing = false,
            )
        }
        detailJob = viewModelScope.launch {
            getWeeklyStepSummary(weekStartEpochDay)
                .onEach { summary ->
                    val bestHour = getWeeklyBestHour(summary)
                    _state.update { it.applyWeeklySummary(summary, bestHour) }
                }
                .catch { e ->
                    crashReporter.log("Weekly report detail load failed: ${e.message}")
                    crashReporter.recordException(e)
                    _state.update { it.copy(isLoading = false, isError = true) }
                }
                .collect {}
        }
    }
}

private fun WeeklyReportDetailState.applyWeeklySummary(
    summary: WeeklyStepSummary,
    bestHour: Int?,
): WeeklyReportDetailState {
    val stepMap = summary.dailyCounts.associateBy { it.dateEpochDay }
    val weekCounts = (0L..6L).map { offset ->
        val epochDay = summary.weekStartEpochDay + offset
        stepMap[epochDay] ?: DailyStepCount(dateEpochDay = epochDay, steps = 0)
    }

    val baseState = copy(
        weekStartEpochDay = summary.weekStartEpochDay,
        isLoading = false,
        isError = false,
    )

    if (weekCounts.none { it.steps > 0 }) {
        return baseState.copy(isEmpty = true)
    }

    val achievedDays = weekCounts.count { it.isAchieved }
    val achievementRate = achievedDays / 7f
    val achievementPct = (achievementRate * 100).toInt()

    return baseState.copy(
        totalSteps = summary.totalSteps,
        achievementPct = achievementPct,
        achievedDays = achievedDays,
        totalDays = 7,
        achievementRate = achievementRate,
        bestDayEpochDay = summary.bestDay?.dateEpochDay,
        bestHour = bestHour,
        longestAchievedStreak = summary.longestAchievedStreak,
        dailyCounts = weekCounts,
        summaryMessageType = when {
            achievementPct >= 100 -> WeeklyReportSummaryMessageType.AllAchieved
            achievementPct >= 70 -> WeeklyReportSummaryMessageType.GoodProgress
            else -> WeeklyReportSummaryMessageType.KeepGoing
        },
        isEmpty = false,
    )
}
