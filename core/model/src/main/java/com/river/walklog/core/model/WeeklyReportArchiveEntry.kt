package com.river.walklog.core.model

import java.time.LocalDate

/**
 * 주간 보고서 아카이브 항목을 나타내는 데이터 클래스.
 *
 * @property summary 해당 주의 걸음 수 요약 정보.
 * @property isLocked 해당 주간 보고서가 잠겨 있는지 여부. 현재 주는 잠김 상태로 간주.
 * @property unlockDate 해당 주간 보고서가 잠금 해제되는 날짜. 일반적으로 다음 주 월요일이 됨.
 */
data class WeeklyReportArchiveEntry(
    val summary: WeeklyStepSummary,
    val isLocked: Boolean,
    val unlockDate: LocalDate,
) {
    val weekStartEpochDay: Long
        get() = summary.weekStartEpochDay
}
