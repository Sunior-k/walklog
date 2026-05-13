package com.river.walklog.core.model

data class WeeklyHomeStats(
    val totalSteps: Int,
    val achievementPct: Int,
    val bestDayEpochDay: Long?,
)
