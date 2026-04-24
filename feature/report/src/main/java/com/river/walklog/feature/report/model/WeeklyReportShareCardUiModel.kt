package com.river.walklog.feature.report.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeeklyReportShareCardUiModel(
    val weekRangeText: String,
    val headline: String,
    val totalStepsText: String,
    val achievementRateText: String,
    val bestDayText: String,
    val bestTimeText: String,
    val streakText: String,
    val footerText: String = "WalkLog와 함께 만든 나의 주간 걷기 리포트",
)
