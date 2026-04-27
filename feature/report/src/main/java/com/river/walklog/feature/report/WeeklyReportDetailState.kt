package com.river.walklog.feature.report

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.DailyStepCount

@Immutable
data class WeeklyReportDetailState(
    val weekRangeText: String = "",
    val dateRangeSubtitle: String = "",
    val totalStepsText: String = "-",
    val achievementRateText: String = "-",
    val achievedDays: Int = 0,
    val totalDays: Int = 7,
    val achievementRate: Float = 0f,
    val bestDayText: String = "-",
    val bestTimeText: String = "-",
    val bestStreakText: String = "-",
    val summaryMessage: String = "",
    val dailyCounts: List<DailyStepCount> = emptyList(),
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val isEmpty: Boolean = false,
    val isError: Boolean = false,
)

sealed interface WeeklyReportDetailIntent {
    data object OnClickBack : WeeklyReportDetailIntent
    data object OnClickShare : WeeklyReportDetailIntent
}
