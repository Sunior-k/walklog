package com.river.walklog.core.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyRecap(
    val year: Int,
    val month: Int,
    val totalSteps: Int,
    val averageStepsPerDay: Int,
    val bestDay: DailyStepCount?,
    val achievedDays: Int,
    val totalDays: Int,
    val longestStreak: Int,
    val activeDays: Int,
    val estimatedCalories: Int,
    val dailyCounts: List<DailyStepCount>,
) {
    val achievementRate: Float
        get() = if (totalDays == 0) 0f else achievedDays.toFloat() / totalDays

    val walkerPersona: String
        get() = when {
            averageStepsPerDay >= 10_000 -> "완벽한 워커"
            averageStepsPerDay >= 7_000 -> "꾸준한 달성자"
            averageStepsPerDay >= 5_000 -> "성실한 도전자"
            averageStepsPerDay >= 3_000 -> "가능성 있는 시작자"
            else -> "걷기 입문자"
        }

    val walkerPersonaDescription: String
        get() = when {
            averageStepsPerDay >= 10_000 -> "매일 완벽한 목표를 달성했어요.\n정말 대단해요!"
            averageStepsPerDay >= 7_000 -> "꾸준히 목표를 향해 나아갔어요.\n이 기세 유지해요!"
            averageStepsPerDay >= 5_000 -> "성실하게 걸음을 쌓아갔어요.\n조금만 더 올려볼까요?"
            averageStepsPerDay >= 3_000 -> "시작이 반! 내달엔 더 나아갈 수 있어요."
            else -> "첫 걸음을 내딛었어요.\n함께 더 걸어봐요!"
        }

    val caloriesFoodComparison: String
        get() = when {
            estimatedCalories >= 500 -> "햄버거 ${estimatedCalories / 500}개 분량"
            estimatedCalories >= 200 -> "아이스크림 ${estimatedCalories / 200}개 분량"
            else -> "${estimatedCalories}kcal"
        }

    val distanceKm: Int
        get() = (totalSteps * 0.00075f).toInt()

    val monthLabel: String
        get() = "${month}월"

    val bestDayDateText: String
        get() = bestDay?.let {
            LocalDate.ofEpochDay(it.dateEpochDay)
                .format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
        } ?: "-"
}
