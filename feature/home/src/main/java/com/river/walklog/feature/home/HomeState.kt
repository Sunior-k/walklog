package com.river.walklog.feature.home

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.ActivityState
import com.river.walklog.core.model.WeatherSummary
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class HomeState(
    val userName: String = "",
    val todayDate: LocalDate = LocalDate.now(),
    val sensorStatus: SensorStatus = SensorStatus.Loading,
    val currentSteps: Int = 0,
    val targetSteps: Int = 10_000,
    val streakDays: Int? = null,
    val forecastHourlyAverages: List<Float> = emptyList(),
    val forecastPeakHour: Int = DEFAULT_FORECAST_PEAK_HOUR,
    val forecastAverageStepsAtPeakHour: Int? = null,
    val forecastTotalDays: Int = 0,
    val forecastActiveDays: Int = 0,
    val weather: WeatherSummary = WeatherSummary.unavailable(),
    val isWeatherLoading: Boolean = true,
    val missionRewardPoints: Int = 20,
    val missionIsRecovery: Boolean = false,
    val missionIsCompleted: Boolean = false,
    val weeklyTotalSteps: Int = 0,
    val weeklyAchievementRateText: String = "-",
    val bestDayEpochDay: Long? = null,
    val isLoading: Boolean = false,
    val isRecapPreviewLoading: Boolean = true,
    val recapYearMonth: YearMonth? = null,
    val recapTotalSteps: Int = 0,
    val streakRiskLevel: StreakRiskLevel = StreakRiskLevel.LOW,
    val activityState: ActivityState = ActivityState.UNKNOWN,
) {
    companion object {
        const val DEFAULT_FORECAST_PEAK_HOUR = 15
    }
}

enum class StreakRiskLevel {
    LOW, MEDIUM, HIGH;

    companion object {
        fun from(streakRisk: Float): StreakRiskLevel = when {
            streakRisk < 0.33f -> LOW
            streakRisk < 0.67f -> MEDIUM
            else -> HIGH
        }
    }
}
