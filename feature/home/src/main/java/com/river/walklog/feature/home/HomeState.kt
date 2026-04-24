package com.river.walklog.feature.home

import androidx.compose.runtime.Immutable
import com.river.walklog.core.engine.ActivityState
import com.river.walklog.feature.home.model.MissionCardUiModel

@Immutable
data class HomeState(
    val userName: String = "익명",
    val todayDateText: String = "",
    val sensorStatus: SensorStatus = SensorStatus.Loading,
    val currentSteps: Int = 0,
    val targetSteps: Int = 10_000,
    val streakDays: Int = 0,
    val forecastTitle: String = "걷기 예보",
    val forecastDescription: String = "오늘 오후 3시는 평소 가장 많이 걷는 시간이에요",
    val weatherLocationText: String = "서울 기준",
    val weatherTemperatureText: String = "-",
    val weatherConditionText: String = "정보 없음",
    val weatherAdviceText: String = "날씨 정보를 불러오는 중이에요",
    val weatherSupportingText: String = "",
    val mission: MissionCardUiModel = MissionCardUiModel(
        title = "오늘 목표 달성까지 조금만 더 걸어보세요",
        currentSteps = 0,
        targetSteps = 10_000,
        rewardText = "+20 캐시",
    ),
    val weeklyTotalStepsText: String = "-",
    val weeklyAchievementRateText: String = "-",
    val bestDayText: String = "-",
    val bestTimeText: String = "-",
    val bestStreakText: String = "-",
    val isLoading: Boolean = false,
    val recapMonthLabel: String = "",
    val recapTotalStepsText: String = "",
    val streakRiskLevel: StreakRiskLevel = StreakRiskLevel.LOW,
    val activityState: ActivityState = ActivityState.UNKNOWN,
)

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

sealed interface HomeIntent {
    data object OnClickTodayMission : HomeIntent
    data object OnClickWeeklyReport : HomeIntent
    data object OnClickForecast : HomeIntent
    data object OnRefresh : HomeIntent
    data object OnRefreshWeather : HomeIntent
    data class OnPermissionResult(val granted: Boolean) : HomeIntent
}
