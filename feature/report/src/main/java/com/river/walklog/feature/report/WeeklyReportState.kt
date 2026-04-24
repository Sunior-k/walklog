package com.river.walklog.feature.report

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.DailyStepCount

@Immutable
data class WeeklyReportState(
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
    val detailDescription: String = "한 주 동안 꾸준히 걸으며 목표에 가까워졌어요.",
    val dailyCounts: List<DailyStepCount> = emptyList(),
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val isEmpty: Boolean = false,
    val isError: Boolean = false,
)

sealed interface WeeklyReportIntent {
    data object OnClickBack : WeeklyReportIntent
    data object OnClickShare : WeeklyReportIntent
}
