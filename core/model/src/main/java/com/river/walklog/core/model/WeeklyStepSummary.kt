package com.river.walklog.core.model

data class WeeklyStepSummary(
    val weekStartEpochDay: Long,
    val dailyCounts: List<DailyStepCount>,
)
