package com.river.walklog.feature.report

import androidx.compose.runtime.Immutable
import com.river.walklog.feature.report.model.WeeklyReportArchiveItemUiModel

@Immutable
data class WeeklyReportArchiveState(
    val archiveItems: List<WeeklyReportArchiveItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)

sealed interface WeeklyReportArchiveIntent {
    data object OnClickBack : WeeklyReportArchiveIntent
    data class OnClickReport(val weekStartEpochDay: Long) : WeeklyReportArchiveIntent
}
