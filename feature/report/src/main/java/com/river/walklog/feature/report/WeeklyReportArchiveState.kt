package com.river.walklog.feature.report

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class WeeklyReportArchiveState(
    val archiveItems: List<WeeklyReportArchiveItemUiState> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)

@Immutable
data class WeeklyReportArchiveItemUiState(
    val weekStartEpochDay: Long,
    val unlockDate: LocalDate,
    val totalSteps: Int,
    val achievementPct: Int,
    val achievementRate: Float,
    val isLocked: Boolean,
) {
    val weekStart: LocalDate
        get() = LocalDate.ofEpochDay(weekStartEpochDay)

    val weekEnd: LocalDate
        get() = weekStart.plusDays(6)

    val weekOfMonth: Int
        get() = (weekStart.dayOfMonth - 1) / 7 + 1

    val achievementRateText: String
        get() = "$achievementPct%"
}
