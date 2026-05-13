package com.river.walklog.core.model

import java.time.LocalDate

data class WeeklyArchiveSummary(
    val weekStartEpochDay: Long,
    val isLocked: Boolean,
    val unlockDate: LocalDate,
    val totalSteps: Int,
    val achievedDays: Int,
    val totalDays: Int,
    val achievementPct: Int,
    val achievementRate: Float,
)
