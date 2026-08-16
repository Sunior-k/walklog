package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPointsBalanceUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<Int> = userSettingsRepository.settings.map { it.totalPoints }
}
