package com.river.walklog.feature.history

import androidx.compose.runtime.Immutable
import java.time.YearMonth

@Immutable
data class HistoryState(
    val yearMonth: YearMonth = YearMonth.now(),
    val totalSteps: Int = 0,
    val achievementRatePct: Int = 0,
    val selectedDateEpochDay: Long? = null,
    val selectedDaySummary: SelectedDaySummary? = null,
    val canNavigateBack: Boolean = true,
    val canNavigateForward: Boolean = false,
    val items: List<CalendarItem> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
) {
    val achievementRateText: String
        get() = "$achievementRatePct%"
}

@Immutable
data class SelectedDaySummary(
    val dateEpochDay: Long,
    val steps: Int,
    val calories: Int,
    val distanceKm: Float,
    val remainingSteps: Int,
    val comparisonDiff: Int?,
    val hasData: Boolean,
    val isAchieved: Boolean,
    val achievementFraction: Float,
    val comparisonSign: Int?, // 1=증가, 0=동일, -1=감소, null=비교불가
    val isPastDay: Boolean,
    val monthRank: Int?,
    val activeDaysInMonth: Int,
    val timelineSegments: List<SelectedDayTimelineSegment> = emptyList(),
)

@Immutable
data class SelectedDayTimelineSegment(
    val type: TimelineSegmentType,
    val steps: Int,
    val fraction: Float,
)

enum class TimelineSegmentType {
    Morning,
    Afternoon,
    Evening,
}

@Immutable
sealed class CalendarItem {
    data class DayLabel(val dayOfWeek: Int) : CalendarItem()
    data class Empty(val index: Int) : CalendarItem()
    data class Day(
        val dateEpochDay: Long,
        val dayNumber: Int,
        val steps: Int,
        val targetSteps: Int,
        val isAchieved: Boolean,
        val isToday: Boolean,
        val hasData: Boolean,
        val isSelected: Boolean,
    ) : CalendarItem()
}
