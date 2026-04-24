package com.river.walklog.core.model

data class WeeklyStepSummary(
    val weekStartEpochDay: Long,
    val dailyCounts: List<DailyStepCount>,
) {
    val totalSteps: Int
        get() = dailyCounts.sumOf { it.steps }

    val averageSteps: Int
        get() = if (dailyCounts.isEmpty()) 0 else totalSteps / dailyCounts.size

    val achievementRate: Float
        get() = if (dailyCounts.isEmpty()) {
            0f
        } else {
            dailyCounts.count { it.isAchieved }.toFloat() / dailyCounts.size
        }

    val bestDay: DailyStepCount?
        get() = dailyCounts.maxByOrNull { it.steps }

    companion object {
        fun empty(weekStartEpochDay: Long) = WeeklyStepSummary(
            weekStartEpochDay = weekStartEpochDay,
            dailyCounts = emptyList(),
        )
    }
}
