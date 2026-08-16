package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.PremiumVisualMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPremiumVisualModeUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<PremiumVisualMode> = userSettingsRepository.settings.map { it.premiumVisualMode }
}
