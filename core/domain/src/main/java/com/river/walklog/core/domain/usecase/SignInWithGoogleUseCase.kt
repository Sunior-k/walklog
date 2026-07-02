package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.AuthUser
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> =
        authRepository.signInWithGoogle(idToken).onSuccess { user ->
            userSettingsRepository.setUserId(user.uid)
        }
}
