package com.river.walklog.core.model

data class WalkingInsightsAnalysis(
    val peakHour: Int,
    val weeklyTrend: Float,
    val recoveryDifficulty: Float,
    val streakRisk: Float,
)
