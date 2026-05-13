package com.river.walklog.feature.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.domain.usecase.GetWalkingInsightsUseCase
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
    private val getWalkingInsights: GetWalkingInsightsUseCase,
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
                val settings = userSettingsRepository.settings.first()
                val currentSteps = stepRepository.getStepsForDay(today.toEpochDay()).first().steps
                val insights = getWalkingInsights(
                    targetStepsPerDay = settings.dailyStepGoal,
                    currentHour = LocalTime.now().hour,
                )
                _state.update { state ->
                    state.copy(
                        currentSteps = currentSteps,
                        targetSteps = settings.dailyStepGoal,
                        recommendedPeakHour = insights?.peakHour ?: state.recommendedPeakHour,
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
