package com.river.walklog.feature.history

sealed class CalendarItem {
    data class DayLabel(val label: String) : CalendarItem()
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
