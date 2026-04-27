package com.river.walklog.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyReportArchiveUseCase
import com.river.walklog.feature.report.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WeeklyReportArchiveViewModel @Inject constructor(
    private val getWeeklyReportArchive: GetWeeklyReportArchiveUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyReportArchiveState())
    val state: StateFlow<WeeklyReportArchiveState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.WEEKLY_REPORT)
        collectReportArchive()
    }

    fun handleIntent(intent: WeeklyReportArchiveIntent) {
        when (intent) {
            WeeklyReportArchiveIntent.OnClickBack -> Unit
            is WeeklyReportArchiveIntent.OnClickReport -> Unit
        }
    }

    private fun collectReportArchive() {
        getWeeklyReportArchive()
            .onEach { entries ->
                _state.update {
                    it.copy(
                        archiveItems = entries
                            .filter { entry -> entry.isLocked || entry.summary.totalSteps > 0 }
                            .map { entry -> entry.toUiModel() },
                        isLoading = false,
                        isError = false,
                    )
                }
            }
            .catch { e ->
                crashReporter.recordException(e)
                _state.update { it.copy(isLoading = false, isError = true) }
            }
            .launchIn(viewModelScope)
    }
}
