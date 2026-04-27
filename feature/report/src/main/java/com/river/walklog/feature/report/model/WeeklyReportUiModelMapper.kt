package com.river.walklog.feature.report.model

import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyReportArchiveEntry
import com.river.walklog.core.model.WeeklyStepSummary
import com.river.walklog.feature.report.WeeklyReportDetailState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun WeeklyReportArchiveEntry.toUiModel(): WeeklyReportArchiveItemUiModel {
    val weekStart = LocalDate.ofEpochDay(weekStartEpochDay)
    val weekEnd = weekStart.plusDays(6)
    val rangeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.KOREAN)
    val monthFormatter = DateTimeFormatter.ofPattern("M월", Locale.KOREAN)
    val weekOfMonth = (weekStart.dayOfMonth - 1) / 7 + 1
    val achievedDays = summary.dailyCounts.count { it.isAchieved }
    val totalDays = summary.dailyCounts.size.coerceAtLeast(7)
    val achievementPct = if (summary.dailyCounts.isEmpty()) {
        0
    } else {
        achievedDays * 100 / totalDays
    }

    return WeeklyReportArchiveItemUiModel(
        weekStartEpochDay = weekStartEpochDay,
        weekRangeText = "${weekStart.format(monthFormatter)} ${weekOfMonth}주차",
        dateRangeText = "${weekStart.format(rangeFormatter)}~${weekEnd.format(rangeFormatter)}",
        totalStepsText = "%,d보".format(summary.totalSteps),
        achievementRateText = "$achievementPct%",
        achievementRate = achievementPct / 100f,
        isLocked = isLocked,
        unlockMessage = "${
        unlockDate.format(
            DateTimeFormatter.ofPattern(
                "M월 d일",
                Locale.KOREAN,
            ),
        )
        } 00:00부터 볼 수 있어요",
    )
}

internal fun WeeklyReportDetailState.applyWeeklySummary(
    summary: WeeklyStepSummary,
    bestHour: Int?,
): WeeklyReportDetailState {
    val weekStart = LocalDate.ofEpochDay(summary.weekStartEpochDay)
    val weekEnd = weekStart.plusDays(6)
    val rangeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.KOREAN)
    val monthFormatter = DateTimeFormatter.ofPattern("M월", Locale.KOREAN)
    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
    val weekOfMonth = (weekStart.dayOfMonth - 1) / 7 + 1

    val stepMap = summary.dailyCounts.associateBy { it.dateEpochDay }
    val weekCounts = (0L..6L).map { offset ->
        val epochDay = summary.weekStartEpochDay + offset
        stepMap[epochDay] ?: DailyStepCount(dateEpochDay = epochDay, steps = 0)
    }

    val baseState = copy(
        weekRangeText = "${weekStart.format(monthFormatter)} ${weekOfMonth}주차 · ${
        weekStart.format(
            rangeFormatter,
        )
        }~${weekEnd.format(rangeFormatter)}",
        dateRangeSubtitle = "${weekStart.format(dateFormatter)} — ${weekEnd.format(dateFormatter)}",
        isLoading = false,
        isError = false,
    )

    if (weekCounts.none { it.steps > 0 }) {
        return baseState.copy(isEmpty = true)
    }

    val achievedDays = weekCounts.count { it.isAchieved }
    val achievementRate = achievedDays / 7f
    val achievementPct = (achievementRate * 100).toInt()
    val bestDayName = summary.bestDay?.let { best ->
        LocalDate.ofEpochDay(best.dateEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE", Locale.KOREAN))
    } ?: "-"

    return baseState.copy(
        totalStepsText = "%,d보".format(summary.totalSteps),
        achievementRateText = "$achievementPct%",
        achievedDays = achievedDays,
        totalDays = 7,
        achievementRate = achievementRate,
        bestDayText = bestDayName,
        bestTimeText = formatBestHour(bestHour),
        bestStreakText = if (summary.longestAchievedStreak > 0) "${summary.longestAchievedStreak}일" else "-",
        dailyCounts = weekCounts,
        summaryMessage = when {
            achievementPct >= 100 -> "이번 주 목표를 모두 달성했어요!"
            achievementPct >= 70 -> "훌륭해요! 목표에 가까워지고 있어요"
            else -> "꾸준히 걷고 있어요, 파이팅!"
        },
        isEmpty = false,
    )
}

private fun formatBestHour(hour: Int?): String {
    hour ?: return "-"
    val amPm = if (hour < 12) "오전" else "오후"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$amPm ${displayHour}시"
}
