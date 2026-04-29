package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.MissionType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * 미션 달성 시 포인트 지급.
 * 날짜 기반 중복 방지: 당일 이미 지급된 미션 유형은 false 반환하고 건너뛰기.
 *
 * @return 실제로 포인트가 지급됐으면 true, 이미 당일 지급된 경우 false
 */
class AwardMissionPointsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(type: MissionType, points: Int): Boolean {
        val today = LocalDate.now().toString()
        val settings = userSettingsRepository.settings.first()

        val alreadyAwarded = when (type) {
            MissionType.DAILY -> settings.lastDailyMissionAwardedDate == today
            MissionType.RECOVERY -> settings.lastRecoveryMissionAwardedDate == today
        }
        if (alreadyAwarded) return false

        userSettingsRepository.addPoints(points)
        when (type) {
            MissionType.DAILY -> userSettingsRepository.setLastDailyMissionAwardedDate(today)
            MissionType.RECOVERY -> userSettingsRepository.setLastRecoveryMissionAwardedDate(today)
        }
        return true
    }
}
