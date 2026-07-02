package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        userSettingsRepository.setUserId("")
    }
}
