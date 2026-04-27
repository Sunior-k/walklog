package com.river.walklog.feature.report.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeeklyReportArchiveItemUiModel(
    val weekStartEpochDay: Long,
    val weekRangeText: String,
    val dateRangeText: String,
    val totalStepsText: String,
    val achievementRateText: String,
    val achievementRate: Float,
    val isLocked: Boolean,
    val unlockMessage: String,
)
