package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.CouponRepository
import javax.inject.Inject

class MarkCouponUsedUseCase @Inject constructor(
    private val couponRepository: CouponRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(code: String): Boolean {
        val userId = authRepository.currentUserIdOrNull ?: return false
        return couponRepository.markUsed(code, userId)
    }
}
