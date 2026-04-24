package com.river.walklog.feature.history

data class HistoryState(
    val monthLabel: String = "",
    val totalStepsText: String = "-",
    val achievementRateText: String = "-",
    val selectedDateEpochDay: Long? = null,
    val selectedDaySummary: SelectedDaySummary? = null,
    val canNavigateBack: Boolean = true,
    val canNavigateForward: Boolean = false,
    val items: List<CalendarItem> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
)

data class SelectedDaySummary(
    val dateText: String,
    val stepsText: String,
    val caloriesText: String,
    val distanceText: String,
    val targetStatusText: String,
    val comparisonText: String,
    val hasData: Boolean,
)
