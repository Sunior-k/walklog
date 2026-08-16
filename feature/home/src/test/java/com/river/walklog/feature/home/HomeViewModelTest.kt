package com.river.walklog.feature.home

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.AwardMissionPointsUseCase
import com.river.walklog.core.domain.usecase.GetCurrentStreakUseCase
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.domain.usecase.GetWalkingInsightsUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyHomeStatsUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyStepSummaryUseCase
import com.river.walklog.core.domain.usecase.ObserveActivityStateUseCase
import com.river.walklog.core.model.ActivityState
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeActivityStateRepository
import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import com.river.walklog.core.testing.repository.FakeStepRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.FakeWalkingInsightsRepository
import com.river.walklog.core.testing.repository.FakeWeatherRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import com.river.walklog.feature.home.notification.WalkingReminderScheduler
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var stepRepository: FakeStepRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var weatherRepository: FakeWeatherRepository
    private lateinit var activityStateRepository: FakeActivityStateRepository
    private lateinit var walkingInsightsRepository: FakeWalkingInsightsRepository
    private lateinit var getWeeklyHomeStats: GetWeeklyHomeStatsUseCase
    private lateinit var getMonthlyRecap: GetMonthlyRecapUseCase
    private lateinit var getStreak: GetCurrentStreakUseCase
    private lateinit var getWalkingInsights: GetWalkingInsightsUseCase
    private lateinit var observeActivityState: ObserveActivityStateUseCase
    private lateinit var awardMissionPoints: AwardMissionPointsUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var walkingReminderScheduler: WalkingReminderScheduler

    private val createdViewModels = mutableListOf<HomeViewModel>()

    @Before
    fun setUp() {
        stepRepository = FakeStepRepository()
        userSettingsRepository = FakeUserSettingsRepository(defaultSettings())
        weatherRepository = FakeWeatherRepository()
        activityStateRepository = FakeActivityStateRepository().apply {
            isModelAvailable = false
        }
        walkingInsightsRepository = FakeWalkingInsightsRepository()
        getWeeklyHomeStats = GetWeeklyHomeStatsUseCase(
            getWeeklyStepSummary = GetWeeklyStepSummaryUseCase(stepRepository),
            userSettingsRepository = userSettingsRepository,
        )
        getMonthlyRecap = GetMonthlyRecapUseCase(stepRepository)
        getStreak = GetCurrentStreakUseCase(stepRepository)
        getWalkingInsights = GetWalkingInsightsUseCase(stepRepository, walkingInsightsRepository)
        observeActivityState = ObserveActivityStateUseCase(activityStateRepository)
        awardMissionPoints = AwardMissionPointsUseCase(userSettingsRepository, FakePointsLedgerRepository())
        crashReporter = mockk(relaxed = true)
        walkingReminderScheduler = mockk(relaxed = true)
        defaultSetup()
    }

    @After
    fun tearDown() {
        createdViewModels.forEach { vm ->
            HomeViewModel::class.java.superclass
                ?.getDeclaredMethod("onCleared")
                ?.also { it.isAccessible = true }
                ?.invoke(vm)
        }
        createdViewModels.clear()
    }

    // SensorStatus — Health Connect 가용성

    @Test
    fun `sensorStatus is Loading when HC is available and no permission result yet`() {
        stepRepository.isHealthConnectAvailable = true
        assertEquals(SensorStatus.Loading, createViewModel().state.value.sensorStatus)
    }

    @Test
    fun `sensorStatus is Unavailable when HC is not available on this device`() {
        stepRepository.isHealthConnectAvailable = false
        assertEquals(SensorStatus.Unavailable, createViewModel().state.value.sensorStatus)
    }

    // updatePermissionResult

    @Test
    fun `updatePermissionResult granted=true sets sensorStatus to Available`() {
        val viewModel = createViewModel()

        viewModel.updatePermissionResult(granted = true)

        assertEquals(SensorStatus.Available, viewModel.state.value.sensorStatus)
    }

    @Test
    fun `updatePermissionResult granted=false sets sensorStatus to PermissionRequired`() {
        val viewModel = createViewModel()

        viewModel.updatePermissionResult(granted = false)

        assertEquals(SensorStatus.PermissionRequired, viewModel.state.value.sensorStatus)
    }

    @Test
    fun `updatePermissionResult is no-op when sensorStatus is already Unavailable`() {
        stepRepository.isHealthConnectAvailable = false
        val viewModel = createViewModel()

        viewModel.updatePermissionResult(granted = true)

        assertEquals(SensorStatus.Unavailable, viewModel.state.value.sensorStatus)
    }

    // settings 관찰

    @Test
    fun `userName is populated from user settings`() {
        userSettingsRepository.setSettings(defaultSettings(nickname = "리버"))
        assertEquals("리버", createViewModel().state.value.userName)
    }

    @Test
    fun `targetSteps is populated from dailyStepGoal in settings`() {
        userSettingsRepository.setSettings(defaultSettings(dailyStepGoal = 8_000))
        assertEquals(8_000, createViewModel().state.value.targetSteps)
    }

    @Test
    fun `missionIsCompleted is true when lastDailyMissionAwardedDate is today`() {
        val today = LocalDate.now().toString()
        userSettingsRepository.setSettings(defaultSettings(lastDailyMissionAwardedDate = today))
        assertTrue(createViewModel().state.value.missionIsCompleted)
    }

    @Test
    fun `missionIsCompleted is false when lastDailyMissionAwardedDate is not today`() {
        userSettingsRepository.setSettings(defaultSettings(lastDailyMissionAwardedDate = "2000-01-01"))
        assertFalse(createViewModel().state.value.missionIsCompleted)
    }

    // 주간 통계

    @Test
    fun `weeklyTotalSteps is populated from GetWeeklyHomeStatsUseCase`() {
        setWeeklyStepCounts(10_000, 10_000, 10_000, 10_000, 10_000, 10_000, 10_000)
        assertEquals(70_000, createViewModel().state.value.weeklyTotalSteps)
    }

    @Test
    fun `weeklyAchievementRateText is formatted as percentage string`() {
        setWeeklyStepCounts(10_000, 10_000, 10_000, 10_000, 0, 0, 0)
        assertEquals("57%", createViewModel().state.value.weeklyAchievementRateText)
    }

    @Test
    fun `bestDayEpochDay is populated from weekly stats`() {
        val weekStart = previousCompletedWeekStart()
        setWeeklyStepCounts(1, 5, 3, 2, 1, 0, 0)
        assertEquals(weekStart + 1, createViewModel().state.value.bestDayEpochDay)
    }

    @Test
    fun `bestDayEpochDay is null when weekly stats has no best day`() {
        assertNull(createViewModel().state.value.bestDayEpochDay)
    }

    // 현재 스트릭

    @Test
    fun `streakDays is populated from GetCurrentStreakUseCase`() {
        val today = LocalDate.now()
        stepRepository.setDailyStepCounts(
            listOf(
                DailyStepCount(today.minusDays(1).toEpochDay(), steps = 10_000, targetSteps = 10_000),
                DailyStepCount(today.toEpochDay(), steps = 10_000, targetSteps = 10_000),
            ),
        )
        assertEquals(2, createViewModel().state.value.streakDays)
    }

    @Test
    fun `streakDays is 0 when streak use case emits 0`() {
        assertEquals(0, createViewModel().state.value.streakDays)
    }

    // 리캡 미리보기

    @Test
    fun `isRecapPreviewLoading becomes false after recap emission`() {
        assertFalse(createViewModel().state.value.isRecapPreviewLoading)
    }

    @Test
    fun `recapTotalSteps is populated from recap preview`() {
        setPreviousMonthStepCount(300_000)
        assertEquals(300_000, createViewModel().state.value.recapTotalSteps)
    }

    @Test
    fun `recapYearMonth is set to the previous month`() {
        val expectedMonth = YearMonth.now().minusMonths(1)
        assertEquals(expectedMonth, createViewModel().state.value.recapYearMonth)
    }

    // todayDate

    @Test
    fun `todayDate is set to LocalDate_now on init`() {
        assertEquals(LocalDate.now(), createViewModel().state.value.todayDate)
    }

    // live steps — updatePermissionResult(true) 이후 걸음 수 관찰

    @Test
    fun `currentSteps is updated when live step stream emits`() {
        stepRepository.setCurrentSteps(5_000)
        val viewModel = createViewModel()

        viewModel.updatePermissionResult(granted = true)

        assertEquals(5_000, viewModel.state.value.currentSteps)
    }

    // activityState 관찰

    @Test
    fun `activityState is updated from ObserveActivityStateUseCase`() {
        activityStateRepository.isModelAvailable = true
        activityStateRepository.setActivityState(ActivityState.WALKING)
        val viewModel = createViewModel()

        viewModel.updatePermissionResult(granted = true)

        assertEquals(ActivityState.WALKING, viewModel.state.value.activityState)
    }

    // StreakRiskLevel

    @Test
    fun `StreakRiskLevel is LOW when streakRisk is below 0_33`() {
        assertEquals(StreakRiskLevel.LOW, StreakRiskLevel.from(0.0f))
        assertEquals(StreakRiskLevel.LOW, StreakRiskLevel.from(0.32f))
    }

    @Test
    fun `StreakRiskLevel is MEDIUM when streakRisk is 0_33 or above but below 0_67`() {
        assertEquals(StreakRiskLevel.MEDIUM, StreakRiskLevel.from(0.33f))
        assertEquals(StreakRiskLevel.MEDIUM, StreakRiskLevel.from(0.50f))
        assertEquals(StreakRiskLevel.MEDIUM, StreakRiskLevel.from(0.66f))
    }

    @Test
    fun `StreakRiskLevel is HIGH when streakRisk is 0_67 or above`() {
        assertEquals(StreakRiskLevel.HIGH, StreakRiskLevel.from(0.67f))
        assertEquals(StreakRiskLevel.HIGH, StreakRiskLevel.from(1.0f))
    }

    // 날씨

    @Test
    fun `isWeatherLoading becomes false after weather load completes`() {
        val viewModel = createViewModel()

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(3_000)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.state.value.isWeatherLoading)
    }

    @Test
    fun `weather is populated from repository on successful load`() {
        val availableWeather = weatherSummary(isAvailable = true)
        weatherRepository.setWeather(availableWeather)
        val viewModel = createViewModel()

        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        assertEquals(availableWeather, viewModel.state.value.weather)
    }

    // helpers

    private fun defaultSetup() {
        stepRepository.isHealthConnectAvailable = true
        stepRepository.setDailyStepCounts(emptyList())
        stepRepository.setCurrentSteps(0)
        stepRepository.setHourlySteps(FloatArray(24 * 7))
        userSettingsRepository.setSettings(defaultSettings())
        weatherRepository.setWeather(WeatherSummary.unavailable())
    }

    private fun createViewModel() = HomeViewModel(
        stepRepository = stepRepository,
        userSettingsRepository = userSettingsRepository,
        weatherRepository = weatherRepository,
        getWeeklyHomeStats = getWeeklyHomeStats,
        getMonthlyRecap = getMonthlyRecap,
        getStreak = getStreak,
        getWalkingInsights = getWalkingInsights,
        observeActivityState = observeActivityState,
        awardMissionPoints = awardMissionPoints,
        crashReporter = crashReporter,
        walkingReminderScheduler = walkingReminderScheduler,
    ).also { createdViewModels.add(it) }

    private fun defaultSettings(
        nickname: String = "",
        dailyStepGoal: Int = 10_000,
        lastDailyMissionAwardedDate: String = "",
    ) = defaultUserSettings(
        nickname = nickname,
        dailyStepGoal = dailyStepGoal,
        lastDailyMissionAwardedDate = lastDailyMissionAwardedDate,
    )

    private fun setWeeklyStepCounts(vararg steps: Int) {
        val weekStart = previousCompletedWeekStart()
        stepRepository.setDailyStepCounts(
            steps.mapIndexed { index, stepCount ->
                DailyStepCount(
                    dateEpochDay = weekStart + index,
                    steps = stepCount,
                    targetSteps = 10_000,
                )
            },
        )
    }

    private fun previousCompletedWeekStart(): Long =
        LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .minusWeeks(1)
            .toEpochDay()

    private fun setPreviousMonthStepCount(steps: Int) {
        val recapMonth = YearMonth.now().minusMonths(1)
        stepRepository.setDailyStepCount(
            DailyStepCount(
                dateEpochDay = recapMonth.atDay(1).toEpochDay(),
                steps = steps,
            ),
        )
    }

    private fun weatherSummary(isAvailable: Boolean) = WeatherSummary(
        locationName = "서울",
        temperatureCelsius = 20,
        condition = com.river.walklog.core.model.WeatherCondition.CLEAR,
        precipitationProbability = 10,
        humidity = 50,
        windSpeedMetersPerSecond = 2f,
        isAvailable = isAvailable,
    )
}
