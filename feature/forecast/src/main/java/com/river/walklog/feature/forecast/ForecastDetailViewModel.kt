package com.river.walklog.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.engine.WalkingInsightsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ForecastDetailViewModel @Inject constructor(
    private val walkingInsightsEngine: WalkingInsightsEngine,
    private val stepRepository: StepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(ForecastDetailState())
    val state: StateFlow<ForecastDetailState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.FORECAST)
        loadInsights()
    }

    fun handleIntent(intent: ForecastDetailIntent) {
        when (intent) {
            ForecastDetailIntent.OnDismiss -> Unit
            ForecastDetailIntent.OnClickStartWalking -> Unit
        }
    }

    // ─── Private ───────────────────────────────────────────────────────────

    private fun loadInsights() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val today = LocalDate.now()
                val toEpochDay = today.toEpochDay()
                val fromEpochDay = toEpochDay - 6
                val targetSteps = userSettingsRepository.settings.first().dailyStepGoal
                val hourlySteps = stepRepository.getHourlyStepsForRange(fromEpochDay, toEpochDay)

                if (hourlySteps.any { it > 0f }) {
                    val result = walkingInsightsEngine.analyze(
                        hourlySteps = hourlySteps,
                        targetStepsPerDay = targetSteps,
                        currentHour = LocalTime.now().hour,
                    )
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            recommendedTimeText = "오늘 ${peakHourToText(result.peakHour)}",
                            averageStepsAtThisTimeText = avgStepsAtPeakHour(hourlySteps, result.peakHour),
                            activeDaysText = activeDaysText(hourlySteps),
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }.onFailure { e ->
                crashReporter.recordException(e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun peakHourToText(hour: Int): String {
        val period = if (hour < 12) "오전" else "오후"
        val displayHour = when {
            hour == 0 -> 12
            hour <= 12 -> hour
            else -> hour - 12
        }
        return "$period ${displayHour}시"
    }

    private fun avgStepsAtPeakHour(hourlySteps: FloatArray, peakHour: Int): String {
        val days = hourlySteps.size / 24
        var total = 0f
        for (d in 0 until days) total += hourlySteps[d * 24 + peakHour]
        return "평균 %,d보".format((total / days).toInt())
    }

    private fun activeDaysText(hourlySteps: FloatArray): String {
        val days = hourlySteps.size / 24
        val activeDays = (0 until days).count { d ->
            (0 until 24).any { h -> hourlySteps[d * 24 + h] > 0f }
        }
        return "최근 ${days}일 중 ${activeDays}일"
    }
}
