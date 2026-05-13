package com.river.walklog.feature.mission

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWalkingInsightsUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeStepRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.FakeWalkingInsightsRepository
import com.river.walklog.core.testing.repository.FakeWeatherRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import com.river.walklog.core.testing.repository.defaultWalkingInsightsAnalysis
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MissionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getWalkingInsights: GetWalkingInsightsUseCase
    private lateinit var stepRepository: FakeStepRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var weatherRepository: FakeWeatherRepository
    private lateinit var walkingInsightsRepository: FakeWalkingInsightsRepository
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        stepRepository = FakeStepRepository()
        userSettingsRepository = FakeUserSettingsRepository(defaultSettings())
        weatherRepository = FakeWeatherRepository()
        walkingInsightsRepository = FakeWalkingInsightsRepository()
        getWalkingInsights = GetWalkingInsightsUseCase(stepRepository, walkingInsightsRepository)
        crashReporter = mockk(relaxed = true)
        defaultSetup()
    }

    // 초기 상태

    @Test
    fun `initial missionType is DAILY`() {
        assertEquals(MissionType.DAILY, createViewModel().state.value.missionType)
    }

    @Test
    fun `initial rewardPoints is 20`() {
        assertEquals(20, createViewModel().state.value.rewardPoints)
    }

    @Test
    fun `initial isCompleted is false`() {
        assertTrue(!createViewModel().state.value.isCompleted)
    }

    // loadMissionData — settings에서 targetSteps 로드

    @Test
    fun `targetSteps is loaded from user settings dailyStepGoal`() {
        userSettingsRepository.setSettings(defaultSettings(dailyStepGoal = 8_000))
        assertEquals(8_000, createViewModel().state.value.targetSteps)
    }

    @Test
    fun `targetSteps uses default 6000 when settings emit default goal`() {
        userSettingsRepository.setSettings(defaultSettings(dailyStepGoal = 6_000))
        assertEquals(6_000, createViewModel().state.value.targetSteps)
    }

    // loadMissionData — stepRepository에서 currentSteps 로드

    @Test
    fun `currentSteps is populated from step repository`() {
        stepRepository.setDailyStepCount(todaySteps(3_500))
        assertEquals(3_500, createViewModel().state.value.currentSteps)
    }

    @Test
    fun `currentSteps is 0 when repository returns 0 steps`() {
        stepRepository.setDailyStepCount(todaySteps(0))
        assertEquals(0, createViewModel().state.value.currentSteps)
    }

    // loadMissionData — getWalkingInsights로 recommendedPeakHour 로드

    @Test
    fun `recommendedPeakHour is set when insights return a peak hour`() {
        setHourlyStepsWithData()
        walkingInsightsRepository.setAnalysis(defaultWalkingInsightsAnalysis(peakHour = 9))
        assertEquals(9, createViewModel().state.value.recommendedPeakHour)
    }

    @Test
    fun `recommendedPeakHour stays null when insights are null`() {
        assertNull(createViewModel().state.value.recommendedPeakHour)
    }

    @Test
    fun `recommendedPeakHour uses insights peakHour when available`() {
        setHourlyStepsWithData()
        walkingInsightsRepository.setAnalysis(defaultWalkingInsightsAnalysis(peakHour = 14))
        assertEquals(14, createViewModel().state.value.recommendedPeakHour)
    }

    // 날씨

    @Test
    fun `weather is populated from repository on init`() {
        val available = availableWeather()
        weatherRepository.setWeather(available)
        assertEquals(available, createViewModel().state.value.weather)
    }

    @Test
    fun `weather is unavailable when repository returns unavailable`() {
        weatherRepository.setWeather(WeatherSummary.unavailable())
        assertTrue(!createViewModel().state.value.weather.isAvailable)
    }

    // refreshWeather

    @Test
    fun `refreshWeather updates weather state`() {
        weatherRepository.setWeather(WeatherSummary.unavailable())
        val viewModel = createViewModel()

        val refreshed = availableWeather()
        weatherRepository.setWeather(refreshed)

        viewModel.refreshWeather()

        assertEquals(refreshed, viewModel.state.value.weather)
    }

    // helpers

    private fun defaultSetup() {
        userSettingsRepository.setSettings(defaultSettings())
        stepRepository.setDailyStepCounts(listOf(todaySteps(0)))
        stepRepository.setHourlySteps(FloatArray(24 * 7))
        walkingInsightsRepository.setAnalysis(defaultWalkingInsightsAnalysis())
        weatherRepository.setWeather(WeatherSummary.unavailable())
    }

    private fun createViewModel() = MissionDetailViewModel(
        getWalkingInsights = getWalkingInsights,
        stepRepository = stepRepository,
        userSettingsRepository = userSettingsRepository,
        weatherRepository = weatherRepository,
        crashReporter = crashReporter,
    )

    private fun defaultSettings(dailyStepGoal: Int = 6_000) = defaultUserSettings(
        dailyStepGoal = dailyStepGoal,
    )

    private fun todaySteps(steps: Int) = DailyStepCount(
        dateEpochDay = LocalDate.now().toEpochDay(),
        steps = steps,
    )

    private fun setHourlyStepsWithData() {
        val hourlySteps = FloatArray(24 * 7)
        listOf(0, 1, 2).forEach { day ->
            hourlySteps[day * 24 + 9] = 500f
        }
        stepRepository.setHourlySteps(hourlySteps)
    }

    private fun availableWeather() = WeatherSummary(
        locationName = "서울",
        temperatureCelsius = 22,
        condition = WeatherCondition.CLEAR,
        precipitationProbability = 10,
        humidity = 50,
        windSpeedMetersPerSecond = 2f,
        isAvailable = true,
    )
}
