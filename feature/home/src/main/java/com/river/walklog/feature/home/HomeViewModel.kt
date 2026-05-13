package com.river.walklog.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.domain.usecase.AwardMissionPointsUseCase
import com.river.walklog.core.domain.usecase.GetCurrentStreakUseCase
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.domain.usecase.GetWalkingInsightsUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyHomeStatsUseCase
import com.river.walklog.core.domain.usecase.ObserveActivityStateUseCase
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.feature.home.notification.WalkingReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stepRepository: StepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val getWeeklyHomeStats: GetWeeklyHomeStatsUseCase,
    private val getMonthlyRecap: GetMonthlyRecapUseCase,
    private val getStreak: GetCurrentStreakUseCase,
    private val getWalkingInsights: GetWalkingInsightsUseCase,
    private val observeActivityState: ObserveActivityStateUseCase,
    private val awardMissionPoints: AwardMissionPointsUseCase,
    private val crashReporter: CrashReporter,
    private val walkingReminderScheduler: WalkingReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var liveStepsJob: Job? = null
    private var activityJob: Job? = null
    private var weatherJob: Job? = null
    private var recapPreviewJob: Job? = null
    private var streakJob: Job? = null

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.HOME)
        crashReporter.setKey(CrashKeys.SENSOR_STATUS, CrashKeys.SensorValues.LOADING)
        initDateText()
        initSensorStatus()
        observeUserSettings()
        collectWeeklyStats()
        loadRecapPreview()
        loadCurrentStreak()
        loadWalkingInsights()
        loadWeather()
        scheduleMidnightRefresh()
    }

    private fun initDateText() {
        _state.update { it.copy(todayDate = LocalDate.now()) }
    }

    private fun initSensorStatus() {
        if (!stepRepository.isHealthConnectAvailable) {
            crashReporter.setKey(CrashKeys.SENSOR_STATUS, CrashKeys.SensorValues.UNAVAILABLE)
            crashReporter.log("Health Connect unavailable on this device")
            _state.update { it.copy(sensorStatus = SensorStatus.Unavailable) }
        }
    }

    private fun observeUserSettings() {
        userSettingsRepository.settings
            .catch { throwable ->
                crashReporter.log("User settings query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { settings ->
                val alreadyCompletedToday = settings.lastDailyMissionAwardedDate == LocalDate.now().toString()
                _state.update { state ->
                    state.copy(
                        userName = settings.nickname,
                        targetSteps = settings.dailyStepGoal,
                        missionIsCompleted = alreadyCompletedToday || state.missionIsCompleted,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updatePermissionResult(granted: Boolean) {
        if (_state.value.sensorStatus == SensorStatus.Unavailable) return

        crashReporter.log("Health Connect READ_STEPS permission result: granted=$granted")

        if (granted) {
            crashReporter.setKey(CrashKeys.SENSOR_STATUS, CrashKeys.SensorValues.AVAILABLE)
            _state.update { it.copy(sensorStatus = SensorStatus.Available) }
            startLiveSteps()
            startObservingActivity()
        } else {
            crashReporter.setKey(CrashKeys.SENSOR_STATUS, CrashKeys.SensorValues.PERMISSION_DENIED)
            _state.update { it.copy(sensorStatus = SensorStatus.PermissionRequired) }
            liveStepsJob?.cancel()
            liveStepsJob = null
            activityJob?.cancel()
            activityJob = null
        }
    }

    private fun startLiveSteps() {
        crashReporter.log("Health Connect step polling started")
        liveStepsJob?.cancel()
        liveStepsJob = stepRepository.observeCurrentSteps()
            .catch { throwable ->
                crashReporter.log("Live step sensor error: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { steps ->
                crashReporter.setKey(CrashKeys.CURRENT_STEPS, steps)
                val prev = _state.value
                val justAchieved = steps >= prev.targetSteps && !prev.missionIsCompleted
                _state.update { state ->
                    state.copy(
                        currentSteps = steps,
                        missionIsCompleted = steps >= state.targetSteps || state.missionIsCompleted,
                    )
                }
                if (justAchieved) awardDailyMission()
            }
            .launchIn(viewModelScope)
    }

    private fun startObservingActivity() {
        activityJob?.cancel()
        activityJob = observeActivityState()
            .catch { throwable ->
                crashReporter.log("Activity classifier error: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { activityState ->
                _state.update { it.copy(activityState = activityState) }
            }
            .launchIn(viewModelScope)
    }

    private fun schedulePeakHourAlarm(peakHour: Int) {
        viewModelScope.launch {
            val notificationsEnabled = runCatching {
                userSettingsRepository.settings.first().notificationsEnabled
            }.getOrDefault(true)
            if (!notificationsEnabled) {
                walkingReminderScheduler.cancel()
                return@launch
            }
            walkingReminderScheduler.schedule(peakHour)
            crashReporter.log("Peak-hour alarm scheduled: $peakHour:00")
        }
    }

    private fun collectWeeklyStats() {
        getWeeklyHomeStats()
            .catch { throwable ->
                crashReporter.log("Weekly summary query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { stats ->
                _state.update { state ->
                    state.copy(
                        weeklyTotalSteps = stats.totalSteps,
                        weeklyAchievementRateText = "${stats.achievementPct}%",
                        bestDayEpochDay = stats.bestDayEpochDay,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWalkingInsights() {
        viewModelScope.launch {
            runCatching {
                val insights = getWalkingInsights(
                    targetStepsPerDay = _state.value.targetSteps,
                    currentHour = LocalTime.now().hour,
                ) ?: return@runCatching
                _state.update { state ->
                    state.copy(
                        streakRiskLevel = StreakRiskLevel.from(insights.streakRisk),
                        forecastAverageStepsAtPeakHour = insights.averageStepsAtPeakHour,
                        forecastTotalDays = insights.totalDays,
                        forecastActiveDays = insights.activeDays,
                        forecastHourlyAverages = insights.hourlyAverages,
                        forecastPeakHour = insights.peakHour,
                    )
                }
                if (insights.peakHour in 6..22) {
                    schedulePeakHourAlarm(insights.peakHour)
                }
            }.onFailure { e ->
                crashReporter.log("Walking insights load failed: ${e.message}")
                crashReporter.recordException(e)
            }
        }
    }

    private fun loadRecapPreview() {
        val today = LocalDate.now()
        val recapMonth = YearMonth.from(today).minusMonths(1)
        recapPreviewJob?.cancel()
        _state.update {
            it.copy(
                isRecapPreviewLoading = true,
                recapYearMonth = recapMonth,
            )
        }
        recapPreviewJob = getMonthlyRecap(recapMonth.year, recapMonth.monthValue)
            .catch { throwable ->
                crashReporter.log("Monthly recap query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
                _state.update { it.copy(isRecapPreviewLoading = false) }
            }
            .onEach { recap ->
                _state.update { state ->
                    state.copy(
                        isRecapPreviewLoading = false,
                        recapYearMonth = YearMonth.of(recap.year, recap.month),
                        recapTotalSteps = recap.totalSteps,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentStreak() {
        streakJob?.cancel()
        streakJob = getStreak()
            .catch { throwable ->
                crashReporter.log("Current streak query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
                _state.update { it.copy(streakDays = 0) }
            }
            .onEach { streak -> _state.update { it.copy(streakDays = streak) } }
            .launchIn(viewModelScope)
    }

    /** 자정마다 날짜 텍스트·걷기 예보 갱신. */
    private fun scheduleMidnightRefresh() {
        viewModelScope.launch {
            while (isActive) {
                val now = LocalTime.now()
                val secondsUntilMidnight =
                    (23 - now.hour) * 3600L + (59 - now.minute) * 60L + (60 - now.second)
                delay(secondsUntilMidnight * 1000L)
                initDateText()
                collectWeeklyStats()
                loadRecapPreview()
                loadCurrentStreak()
                loadWalkingInsights()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            crashReporter.log("Manual refresh triggered")
            _state.update { it.copy(isLoading = true) }
            collectWeeklyStats()
            loadRecapPreview()
            loadCurrentStreak()
            loadWalkingInsights()
            loadWeather(forceRefresh = true)
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun refreshWeather() {
        loadWeather(forceRefresh = true)
    }

    private fun loadWeather(forceRefresh: Boolean = false) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _state.update { it.copy(isWeatherLoading = true) }
            val weather = loadWeatherWithRetry(forceRefresh = forceRefresh)
            _state.update { state -> state.copy(weather = weather, isWeatherLoading = false) }
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
                    crashReporter.log("Weather load failed: ${e.message}")
                    crashReporter.recordException(e)
                }

            if (attempt < WEATHER_LOAD_MAX_ATTEMPTS - 1) {
                delay(WEATHER_RETRY_DELAY_MS)
            }
        }
        return fallback
    }

    private fun awardDailyMission() {
        viewModelScope.launch {
            runCatching {
                val awarded = awardMissionPoints(MissionType.DAILY, DAILY_MISSION_POINTS)
                if (awarded) {
                    crashReporter.log("Daily mission points awarded: +$DAILY_MISSION_POINTS")
                }
            }.onFailure { e ->
                crashReporter.recordException(e)
            }
        }
    }

    companion object {
        private const val WEATHER_LOAD_MAX_ATTEMPTS = 3
        private const val WEATHER_RETRY_DELAY_MS = 1_500L
        private const val DAILY_MISSION_POINTS = 20
    }
}
