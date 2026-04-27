package com.river.walklog.core.model

/**
 * 한 주 동안의 걸음 수 요약 정보를 나타내는 데이터 클래스.
 *
 * @property weekStartEpochDay 해당 주의 시작 날짜를 에포크 일수로 표현한 값.
 * @property dailyCounts 해당 주의 각 날짜별 걸음 수 정보 리스트.
 */
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

    val longestAchievedStreak: Int
        get() = dailyCounts.fold(0 to 0) { (longest, current), day ->
            if (day.isAchieved) {
                val next = current + 1
                maxOf(longest, next) to next
            } else {
                longest to 0
            }
        }.first

    companion object {
        fun empty(weekStartEpochDay: Long) = WeeklyStepSummary(
            weekStartEpochDay = weekStartEpochDay,
            dailyCounts = emptyList(),
        )
    }
}
