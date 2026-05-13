package com.river.walklog.core.model

data class WeeklyReportDetail(
    val weekStartEpochDay: Long,
    val weekCounts: List<DailyStepCount>,
    val totalSteps: Int,
    val achievedDays: Int,
    val totalDays: Int,
    val achievementRate: Float,
    val bestDayEpochDay: Long?,
    val bestHour: Int?,
    val longestAchievedStreak: Int,
    val isEmpty: Boolean,
) {
    companion object {
        fun empty(weekStartEpochDay: Long, weekCounts: List<DailyStepCount> = emptyList()) =
            WeeklyReportDetail(
                weekStartEpochDay = weekStartEpochDay,
                weekCounts = weekCounts,
                totalSteps = 0,
                achievedDays = 0,
                totalDays = 7,
                achievementRate = 0f,
                bestDayEpochDay = null,
                bestHour = null,
                longestAchievedStreak = 0,
                isEmpty = true,
            )
    }
}
