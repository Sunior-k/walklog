package com.river.walklog.feature.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.engine.WalkingInsightsEngine
import com.river.walklog.core.model.WeatherSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class MissionDetailViewModel @Inject constructor(
    private val walkingInsightsEngine: WalkingInsightsEngine,
    private val stepRepository: StepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(MissionDetailState())
    val state: StateFlow<MissionDetailState> = _state.asStateFlow()

    private var weatherJob: Job? = null

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.MISSION_DETAIL)
        loadMissionData()
        loadWeather()
    }

    fun refreshWeather() {
        loadWeather(forceRefresh = true)
    }

    private fun loadMissionData() {
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now()
                val toEpochDay = today.toEpochDay()
                val fromEpochDay = toEpochDay - 6
                val settings = userSettingsRepository.settings.first()
                val targetSteps = settings.dailyStepGoal
                val currentSteps = stepRepository.getStepsForDay(toEpochDay).first().steps
                val hourlySteps = stepRepository.getHourlyStepsForRange(fromEpochDay, toEpochDay)

                val recommendedPeakHour = if (hourlySteps.any { it > 0f }) {
                    val result = walkingInsightsEngine.analyze(
                        hourlySteps = hourlySteps,
                        targetStepsPerDay = targetSteps,
                        currentHour = LocalTime.now().hour,
                    )
                    result.peakHour
                } else {
                    _state.value.recommendedPeakHour
                }

                _state.update { state ->
                    state.copy(
                        currentSteps = currentSteps,
                        targetSteps = targetSteps,
                        recommendedPeakHour = recommendedPeakHour,
                    )
                }
            }.onFailure { e ->
                crashReporter.log("Mission data load failed: ${e.message}")
                crashReporter.recordException(e)
            }
        }
    }

    private fun loadWeather(forceRefresh: Boolean = false) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            val weather = loadWeatherWithRetry(forceRefresh = forceRefresh)
            _state.update { state -> state.copy(weather = weather) }
        }
    }

    private suspend fun loadWeatherWithRetry(forceRefresh: Boolean): WeatherSummary {
        var fallback = WeatherSummary.unavailable()
        repeat(WEATHER_LOAD_MAX_ATTEMPTS) { attempt ->
            runCatching { weatherRepository.getCurrentWeather(forceRefresh = forceRefresh || attempt > 0) }
                .onSuccess { weather ->
                    if (weather.isAvailable) return weather
                    fallback = weather
                }
                .onFailure { e ->
                    crashReporter.log("Mission weather load failed: ${e.message}")
                    crashReporter.recordException(e)
                }

            if (attempt < WEATHER_LOAD_MAX_ATTEMPTS - 1) {
                delay(WEATHER_RETRY_DELAY_MS)
            }
        }
        return fallback
    }

    companion object {
        private const val WEATHER_LOAD_MAX_ATTEMPTS = 3
        private const val WEATHER_RETRY_DELAY_MS = 1_500L
    }
}
