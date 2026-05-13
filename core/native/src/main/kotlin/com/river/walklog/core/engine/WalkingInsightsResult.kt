package com.river.walklog.core.engine

data class WalkingInsightsResult(
    val peakHour: Int,
    val weeklyTrend: Float,
    val recoveryDifficulty: Float,
    val streakRisk: Float,
)
