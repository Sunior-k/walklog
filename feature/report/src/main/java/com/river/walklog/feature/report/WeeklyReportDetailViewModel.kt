package com.river.walklog.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyReportDetailUseCase
import com.river.walklog.core.model.WeeklyReportDetail
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
    private val getWeeklyReportDetail: GetWeeklyReportDetailUseCase,
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
            getWeeklyReportDetail(weekStartEpochDay)
                .onEach { detail -> _state.update { it.applyDetail(detail) } }
                .catch { e ->
                    crashReporter.log("Weekly report detail load failed: ${e.message}")
                    crashReporter.recordException(e)
                    _state.update { it.copy(isLoading = false, isError = true) }
                }
                .collect {}
        }
    }
}

private fun WeeklyReportDetailState.applyDetail(detail: WeeklyReportDetail): WeeklyReportDetailState {
    if (detail.isEmpty) {
        return copy(
            weekStartEpochDay = detail.weekStartEpochDay,
            isLoading = false,
            isError = false,
            isEmpty = true,
        )
    }
    val achievementPct = (detail.achievementRate * 100).toInt()
    return copy(
        weekStartEpochDay = detail.weekStartEpochDay,
        isLoading = false,
        isError = false,
        isEmpty = false,
        totalSteps = detail.totalSteps,
        achievementPct = achievementPct,
        achievedDays = detail.achievedDays,
        totalDays = detail.totalDays,
        achievementRate = detail.achievementRate,
        bestDayEpochDay = detail.bestDayEpochDay,
        bestHour = detail.bestHour,
        longestAchievedStreak = detail.longestAchievedStreak,
        dailyCounts = detail.weekCounts,
        summaryMessageType = when {
            achievementPct >= 100 -> WeeklyReportSummaryMessageType.AllAchieved
            achievementPct >= 70 -> WeeklyReportSummaryMessageType.GoodProgress
            else -> WeeklyReportSummaryMessageType.KeepGoing
        },
    )
}
