package com.river.walklog.core.engine

/**
 * Output of [WalkingInsightsEngine.analyze].
 *
 * @property peakHour            Most active hour of day (0–23).
 * @property weeklyTrend         0.0 (declining) … 1.0 (improving).
 * @property recoveryDifficulty  0.0 (easy) … 1.0 (hard). Clamped to [0.2, 0.8].
 * @property streakRisk          0.0 (safe) … 1.0 (at risk).
 */
data class WalkingInsightsResult(
    val peakHour: Int,
    val weeklyTrend: Float,
    val recoveryDifficulty: Float,
    val streakRisk: Float,
)
