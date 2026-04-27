package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyStepSummary
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * 주간 걸음 수 집계 (Flow를 반환)
 * weekStartEpochDay가 null이면 마지막으로 완료된 주의 월요일 기준으로 조회.
 */
class GetWeeklyStepSummaryUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(weekStartEpochDay: Long? = null): Flow<WeeklyStepSummary> {
        val startDay = weekStartEpochDay
            ?: LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .toEpochDay()
        return stepRepository.getWeeklyStepSummary(startDay)
    }
}
