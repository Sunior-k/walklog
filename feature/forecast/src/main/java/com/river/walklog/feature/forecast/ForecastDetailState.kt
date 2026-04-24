package com.river.walklog.feature.forecast

import androidx.compose.runtime.Immutable

@Immutable
data class ForecastDetailState(
    val title: String = "걷기 예보",
    val recommendedTimeText: String = "오늘 오후 3시",
    val description: String = "평소 이 시간대에 가장 많이 걷고 있어요. 지금 움직이면 목표 달성 확률이 높아요.",
    val averageStepsAtThisTimeText: String = "평균 1,240보",
    val activeDaysText: String = "최근 7일 중 5일",
    val bestPatternText: String = "평일 오후 시간대",
    val isLoading: Boolean = false,
)

sealed interface ForecastDetailIntent {
    data object OnDismiss : ForecastDetailIntent
    data object OnClickStartWalking : ForecastDetailIntent
}
