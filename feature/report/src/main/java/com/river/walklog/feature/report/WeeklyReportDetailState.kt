package com.river.walklog.feature.report

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.DailyStepCount
import java.time.LocalDate

@Immutable
data class WeeklyReportDetailState(
    val weekStartEpochDay: Long? = null,
    val totalSteps: Int = 0,
    val achievementPct: Int = 0,
    val achievedDays: Int = 0,
    val totalDays: Int = 7,
    val achievementRate: Float = 0f,
    val bestDayEpochDay: Long? = null,
    val bestHour: Int? = null,
    val longestAchievedStreak: Int = 0,
    val summaryMessageType: WeeklyReportSummaryMessageType = WeeklyReportSummaryMessageType.KeepGoing,
    val dailyCounts: List<DailyStepCount> = emptyList(),
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val isEmpty: Boolean = false,
    val isError: Boolean = false,
    val userMessage: WeeklyReportUserMessage? = null,
) {
    val weekStart: LocalDate?
        get() = weekStartEpochDay?.let(LocalDate::ofEpochDay)

    val weekEnd: LocalDate?
        get() = weekStart?.plusDays(6)

    val weekOfMonth: Int?
        get() = weekStart?.let { (it.dayOfMonth - 1) / 7 + 1 }

    val achievementRateText: String
        get() = "$achievementPct%"
}

enum class WeeklyReportSummaryMessageType {
    AllAchieved,
    GoodProgress,
    KeepGoing,
}

enum class WeeklyReportUserMessage {
    ShareFailed,
}
