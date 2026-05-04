package com.river.walklog.feature.history

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sign

object HistoryDummyData {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
    private val selectedDateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
    private val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    private val sampleSteps = listOf(
        8_420,
        10_180,
        5_760,
        12_340,
        6_480,
        3_920,
        9_860,
        11_520,
        7_140,
        4_430,
        13_280,
        9_240,
        6_050,
        10_740,
        2_680,
        8_910,
        12_060,
        7_830,
        5_420,
        14_100,
        9_660,
        11_880,
        6_710,
        4_950,
        10_320,
        8_050,
        12_790,
        7_520,
        5_180,
        9_370,
        11_240,
    )

    fun monthState(
        monthLabel: String,
        canNavigateForward: Boolean,
        targetSteps: Int = 6_000,
    ): HistoryState {
        val yearMonth = parseYearMonth(monthLabel) ?: YearMonth.now()
        val today = LocalDate.now()
        val selectedDate = when {
            yearMonth == YearMonth.from(today) -> today
            yearMonth.isBefore(YearMonth.from(today)) -> yearMonth.atEndOfMonth()
            else -> yearMonth.atDay(1)
        }
        val dailySteps = (1..yearMonth.lengthOfMonth()).map { day ->
            val date = yearMonth.atDay(day)
            val steps = when {
                date.isAfter(today) -> 0
                else -> sampleSteps[(day - 1) % sampleSteps.size]
            }
            date to steps
        }
        val calendarItems = buildCalendarItems(
            yearMonth = yearMonth,
            targetSteps = targetSteps,
            dailySteps = dailySteps,
            selectedDate = selectedDate,
        )
        val dayItems = calendarItems.filterIsInstance<CalendarItem.Day>()
        val selectedDay = dayItems.firstOrNull { it.dateEpochDay == selectedDate.toEpochDay() }
            ?: dayItems.lastOrNull { it.hasData }
        val previousDay = selectedDay?.let { selected ->
            dayItems.firstOrNull { it.dateEpochDay == selected.dateEpochDay - 1 }
        }
        val activeSteps = dailySteps.filter { !it.first.isAfter(today) }.map { it.second }
        val totalSteps = activeSteps.sum()
        val achievedDays = activeSteps.count { it >= targetSteps }
        val totalDays = activeSteps.size.coerceAtLeast(1)

        return HistoryState(
            monthLabel = yearMonth.format(monthFormatter),
            totalStepsText = "%,d 보".format(totalSteps),
            achievementRateText = "${achievedDays * 100 / totalDays}%",
            selectedDateEpochDay = selectedDay?.dateEpochDay,
            selectedDaySummary = selectedDay?.toSelectedDaySummary(previousDay),
            canNavigateBack = true,
            canNavigateForward = canNavigateForward,
            items = calendarItems,
            isLoading = false,
            isEmpty = false,
        )
    }

    private fun parseYearMonth(monthLabel: String): YearMonth? {
        val match = Regex("""(\d{4})년\s*(\d{1,2})월""").find(monthLabel) ?: return null
        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        return runCatching { YearMonth.of(year, month) }.getOrNull()
    }

    private fun buildCalendarItems(
        yearMonth: YearMonth,
        targetSteps: Int,
        dailySteps: List<Pair<LocalDate, Int>>,
        selectedDate: LocalDate,
    ): List<CalendarItem> {
        val firstDay = yearMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value - 1
        val stepMap = dailySteps.associate { it.first.toEpochDay() to it.second }
        val items = mutableListOf<CalendarItem>()

        dayLabels.forEach { items.add(CalendarItem.DayLabel(it)) }
        repeat(startOffset) { items.add(CalendarItem.Empty(it)) }

        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            val steps = stepMap[date.toEpochDay()] ?: 0
            items.add(
                CalendarItem.Day(
                    dateEpochDay = date.toEpochDay(),
                    dayNumber = day,
                    steps = steps,
                    targetSteps = targetSteps,
                    isAchieved = steps >= targetSteps,
                    isToday = date == LocalDate.now(),
                    hasData = steps > 0,
                    isSelected = date == selectedDate,
                ),
            )
        }

        return items
    }

    private fun CalendarItem.Day.toSelectedDaySummary(
        previousDay: CalendarItem.Day?,
    ): SelectedDaySummary {
        val calories = (steps * 0.04f).toInt()
        val distance = steps * 0.00075f
        val diff = if (hasData && previousDay?.hasData == true) steps - previousDay.steps else null
        val timelineSteps = buildTimelineSteps(steps, dayNumber)
        val maxSegmentSteps = timelineSteps.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        return SelectedDaySummary(
            dateText = LocalDate.ofEpochDay(dateEpochDay).format(selectedDateFormatter),
            stepsText = "%,d".format(steps),
            caloriesText = "%,d kcal".format(calories),
            distanceText = String.format(Locale.KOREAN, "%.1f km", distance),
            targetStatusText = when {
                !hasData -> "이날은 걸음 기록이 없어요"
                isAchieved -> "목표 달성"
                else -> "목표까지 %,d보".format(targetSteps - steps)
            },
            comparisonText = when {
                diff == null -> "전날 기록 없음"
                diff > 0 -> "전날보다 +%,d보".format(diff)
                diff < 0 -> "전날보다 %,d보".format(diff)
                else -> "전날과 같아요"
            },
            hasData = hasData,
            isAchieved = isAchieved,
            achievementFraction = if (hasData) (steps.toFloat() / targetSteps).coerceIn(0f, 1f) else 0f,
            comparisonSign = diff?.sign,
            isPastDay = LocalDate.ofEpochDay(dateEpochDay).isBefore(LocalDate.now()),
            insightText = when {
                !hasData -> "이날은 분석할 걸음 기록이 없어요"
                isAchieved && diff != null && diff > 0 -> "목표를 달성했고 전날보다 %,d보 더 걸었어요".format(diff)
                isAchieved -> "목표를 달성한 날이에요"
                diff != null && diff > 0 -> "전날보다 %,d보 더 걸었어요".format(diff)
                diff != null && diff < 0 -> "전날보다 %,d보 적게 걸었어요".format(-diff)
                else -> "목표까지 %,d보 남았어요".format((targetSteps - steps).coerceAtLeast(0))
            },
            monthRankText = if (hasData) "이번 달 ${((dayNumber - 1) % 9) + 1}위 / 31일" else "이번 달 순위 없음",
            timelineSegments = if (hasData) {
                timelineSteps.map { (label, segmentSteps) ->
                    SelectedDayTimelineSegment(
                        label = label,
                        stepsText = "%,d보".format(segmentSteps),
                        fraction = segmentSteps.toFloat() / maxSegmentSteps,
                    )
                }
            } else {
                emptyList()
            },
        )
    }

    private fun buildTimelineSteps(totalSteps: Int, dayNumber: Int): List<Pair<String, Int>> {
        if (totalSteps <= 0) return emptyList()
        val morningRatio = 0.24f + ((dayNumber % 3) * 0.04f)
        val afternoonRatio = 0.42f + ((dayNumber % 4) * 0.03f)
        val morningSteps = (totalSteps * morningRatio).toInt()
        val afternoonSteps = (totalSteps * afternoonRatio).toInt()
        val eveningSteps = (totalSteps - morningSteps - afternoonSteps).coerceAtLeast(0)
        return listOf(
            "오전" to morningSteps,
            "오후" to afternoonSteps,
            "저녁" to eveningSteps,
        )
    }
}
