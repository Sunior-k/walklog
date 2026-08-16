package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.MissionType
import java.time.LocalDate
import javax.inject.Inject

/** 미션 달성 시 포인트 지급. 당일 이미 지급된 미션 유형은 차감 없이 false. */
class AwardMissionPointsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
) {
    suspend operator fun invoke(type: MissionType, points: Int): Boolean {
        val today = LocalDate.now().toString()

        // 지급 여부 마킹을 먼저 원자적으로 확정한 뒤 포인트를 지급 — 순서를 반대로 하면
        // 동시 호출 시 마킹 전에 둘 다 통과해 하루 중복 지급이 될 수 있다.
        val awarded = when (type) {
            MissionType.DAILY -> userSettingsRepository.tryMarkDailyMissionAwarded(today)
            MissionType.RECOVERY -> userSettingsRepository.tryMarkRecoveryMissionAwarded(today)
        }
        if (!awarded) return false

        userSettingsRepository.addPoints(points)
        pointsLedgerRepository.record(points, type.name)
        return true
    }
}
