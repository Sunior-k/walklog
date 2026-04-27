package com.river.walklog.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyBestHourUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyStepSummaryUseCase
import com.river.walklog.feature.report.model.applyWeeklySummary
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

    fun handleIntent(intent: WeeklyReportDetailIntent) {
        when (intent) {
            WeeklyReportDetailIntent.OnClickBack -> Unit
            WeeklyReportDetailIntent.OnClickShare -> Unit
        }
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
                    crashReporter.recordException(e)
                    _state.update { it.copy(isLoading = false, isError = true) }
                }
                .collect {}
        }
    }

    fun setSharing(isSharing: Boolean) {
        _state.update { it.copy(isSharing = isSharing) }
    }
}
