package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.PremiumVisualMode
import javax.inject.Inject

class SetPremiumVisualModeUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(mode: PremiumVisualMode) = userSettingsRepository.setPremiumVisualMode(mode)
}
