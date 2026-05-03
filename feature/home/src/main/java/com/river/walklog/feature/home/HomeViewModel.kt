package com.river.walklog.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.domain.usecase.AwardMissionPointsUseCase
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyStepSummaryUseCase
import com.river.walklog.core.engine.ActivityClassifier
import com.river.walklog.core.engine.WalkingInsightsEngine
import com.river.walklog.core.engine.WalkingInsightsResult
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.model.WeeklyStepSummary
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stepRepository: StepRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val getWeeklyStepSummary: GetWeeklyStepSummaryUseCase,
    private val getMonthlyRecap: GetMonthlyRecapUseCase,
    private val walkingInsightsEngine: WalkingInsightsEngine,
    private val activityClassifier: ActivityClassifier,
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
    private var latestWeeklySummary: WeeklyStepSummary? = null

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.HOME)
        crashReporter.setKey(CrashKeys.SENSOR_STATUS, CrashKeys.SensorValues.LOADING)
        initDateText()
        initSensorStatus()
        observeUserSettings()
        collectWeeklySummary()
        loadRecapPreview()
        loadWalkingInsights()
        loadWeather()
        scheduleMidnightRefresh()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OnClickForecast -> {
                crashReporter.log("Navigate → forecast bottom sheet")
            }
            HomeIntent.OnClickTodayMission -> {
                crashReporter.log("Navigate → mission detail")
                crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.MISSION_DETAIL)
            }
            HomeIntent.OnClickWeeklyReport -> {
                crashReporter.log("Navigate → weekly report")
                crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.WEEKLY_REPORT)
            }
            HomeIntent.OnRefresh -> refresh()
            HomeIntent.OnRefreshWeather -> loadWeather(forceRefresh = true)
            is HomeIntent.OnPermissionResult -> handlePermissionResult(intent.granted)
        }
    }

    // ─── Private ───────────────────────────────────────────────────────────

    private fun initDateText() {
        val formatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
        _state.update { it.copy(todayDateText = LocalDate.now().format(formatter)) }
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
                val today = LocalDate.now().toString()
                val alreadyCompletedToday = settings.lastDailyMissionAwardedDate == today
                _state.update { state ->
                    val updatedState = state.copy(
                        userName = settings.nickname.ifBlank { "익명" },
                        targetSteps = settings.dailyStepGoal,
                        mission = state.mission.copy(
                            targetSteps = settings.dailyStepGoal,
                            // 앱 재시작 후에도 당일 달성 상태 유지
                            isCompleted = alreadyCompletedToday || state.mission.isCompleted,
                        ),
                    )
                    latestWeeklySummary?.let { summary ->
                        updatedState.applyWeeklySummary(summary, settings.dailyStepGoal)
                    } ?: updatedState
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handlePermissionResult(granted: Boolean) {
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
                val justAchieved = steps >= prev.targetSteps && !prev.mission.isCompleted
                _state.update { state ->
                    state.copy(
                        currentSteps = steps,
                        mission = state.mission.copy(
                            currentSteps = steps,
                            isCompleted = steps >= state.targetSteps || state.mission.isCompleted,
                        ),
                    )
                }
                if (justAchieved) awardDailyMission()
            }
            .launchIn(viewModelScope)
    }

    private fun startObservingActivity() {
        if (!activityClassifier.isModelAvailable) return
        activityJob?.cancel()
        activityJob = activityClassifier.observeActivityState()
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

    /** 최근 7일간의 시간대별 걸음 수 데이터를 불러와 걷기 예보를 갱신. */
    private fun loadWalkingInsights() {
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now()
                val toEpochDay = today.toEpochDay()
                val fromEpochDay = toEpochDay - 6
                val hourlySteps = stepRepository.getHourlyStepsForRange(fromEpochDay, toEpochDay)

                val daysWithData = (0..6).count { dayOffset ->
                    val start = dayOffset * 24
                    (start until start + 24).any { hourlySteps[it] > 0f }
                }

                if (daysWithData >= 3) {
                    val result = walkingInsightsEngine.analyze(
                        hourlySteps = hourlySteps,
                        targetStepsPerDay = _state.value.targetSteps,
                        currentHour = LocalTime.now().hour,
                    )
                    _state.update { state ->
                        state.copy(
                            forecastDescription = if (result.peakHour in 6..22) {
                                buildForecastDescription(result)
                            } else {
                                state.forecastDescription
                            },
                            streakRiskLevel = StreakRiskLevel.from(result.streakRisk),
                            forecastRecommendedTimeText = "오늘 ${peakHourToText(result.peakHour)}",
                            forecastAverageStepsText = avgStepsAtPeakHour(hourlySteps, result.peakHour),
                            forecastActiveDaysText = activeDaysText(hourlySteps),
                            forecastHourlyAverages = computeHourlyAverages(hourlySteps),
                            forecastPeakHour = result.peakHour,
                        )
                    }
                    if (result.peakHour in 6..22) {
                        schedulePeakHourAlarm(result.peakHour)
                    }
                }
            }.onFailure { e ->
                crashReporter.log("Walking insights load failed: ${e.message}")
                crashReporter.recordException(e)
            }
        }
    }

    private fun buildForecastDescription(result: WalkingInsightsResult): String {
        val period = if (result.peakHour < 12) "오전" else "오후"
        val displayHour = when {
            result.peakHour == 0 -> 12
            result.peakHour <= 12 -> result.peakHour
            else -> result.peakHour - 12
        }
        return "오늘 $period ${displayHour}시는 평소 가장 많이 걷는 시간이에요"
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

    private fun computeHourlyAverages(hourlySteps: FloatArray): List<Float> {
        val days = hourlySteps.size / 24
        return (0 until 24).map { hour ->
            var total = 0f
            for (d in 0 until days) total += hourlySteps[d * 24 + hour]
            total / days
        }
    }

    private fun activeDaysText(hourlySteps: FloatArray): String {
        val days = hourlySteps.size / 24
        val activeDays = (0 until days).count { d ->
            (0 until 24).any { h -> hourlySteps[d * 24 + h] > 0f }
        }
        return "최근 ${days}일 중 ${activeDays}일"
    }

    private fun collectWeeklySummary() {
        getWeeklyStepSummary()
            .catch { throwable ->
                crashReporter.log("Weekly summary query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { summary ->
                latestWeeklySummary = summary
                _state.update { state -> state.applyWeeklySummary(summary, state.targetSteps) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadRecapPreview() {
        val today = LocalDate.now()
        recapPreviewJob?.cancel()
        recapPreviewJob = getMonthlyRecap(today.year, today.monthValue)
            .catch { throwable ->
                crashReporter.log("Monthly recap query failed: ${throwable.message}")
                crashReporter.recordException(throwable)
            }
            .onEach { recap ->
                _state.update { state ->
                    state.copy(
                        recapMonthLabel = recap.monthLabel,
                        recapTotalStepsText = "%,d보".format(recap.totalSteps),
                        streakDays = computeCurrentStreak(
                            dailyCounts = recap.dailyCounts,
                            today = today,
                        ),
                    )
                }
            }
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
                collectWeeklySummary()
                loadRecapPreview()
                loadWalkingInsights()
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            crashReporter.log("Manual refresh triggered")
            _state.update { it.copy(isLoading = true) }
            collectWeeklySummary()
            loadRecapPreview()
            loadWalkingInsights()
            loadWeather(forceRefresh = true)
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadWeather(forceRefresh: Boolean = false) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            val weather = loadWeatherWithRetry(forceRefresh = forceRefresh)
            _state.update { state -> state.applyWeather(weather) }
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

    // ─── Mapper ────────────────────────────────────────────────────────────

    private fun HomeState.applyWeeklySummary(
        summary: WeeklyStepSummary,
        targetSteps: Int,
    ): HomeState {
        val bestDayName = summary.bestDay?.let { best ->
            LocalDate.ofEpochDay(best.dateEpochDay)
                .format(DateTimeFormatter.ofPattern("EEEE", Locale.KOREAN))
        } ?: "-"
        val achievementPct = if (summary.dailyCounts.isEmpty() || targetSteps <= 0) {
            0
        } else {
            summary.dailyCounts.count { it.steps >= targetSteps } * 100 / summary.dailyCounts.size
        }

        return copy(
            weeklyTotalStepsText = "%,d보".format(summary.totalSteps),
            weeklyAchievementRateText = "$achievementPct%",
            bestDayText = bestDayName,
        )
    }

    private fun HomeState.applyWeather(weather: WeatherSummary): HomeState = copy(
        weatherLocationText = "${weather.locationName} 기준",
        weatherTemperatureText = weather.temperatureText,
        weatherConditionText = weather.conditionText,
        weatherAdviceText = weather.walkingAdvice,
        weatherSupportingText = buildWeatherSupportingText(weather),
    )

    private fun buildWeatherSupportingText(weather: WeatherSummary): String = listOfNotNull(
        weather.precipitationProbability?.let { "강수 $it%" },
        weather.humidity?.let { "습도 $it%" },
        weather.windSpeedMetersPerSecond?.let { "풍속 ${String.format(Locale.KOREAN, "%.1f", it)}m/s" },
    ).joinToString(" · ")

    private fun computeCurrentStreak(
        dailyCounts: List<DailyStepCount>,
        today: LocalDate,
    ): Int {
        val todayEpochDay = today.toEpochDay()
        val countsByDay = dailyCounts.associateBy { it.dateEpochDay }
        var day = todayEpochDay
        if (countsByDay[day]?.isAchieved != true) {
            day--
        }

        var streak = 0
        while (countsByDay[day]?.isAchieved == true) {
            streak++
            day--
        }
        return streak
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
