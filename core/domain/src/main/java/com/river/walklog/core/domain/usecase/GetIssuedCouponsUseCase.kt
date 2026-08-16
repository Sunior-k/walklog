package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.CouponRepository
import com.river.walklog.core.model.Coupon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetIssuedCouponsUseCase @Inject constructor(
    private val couponRepository: CouponRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<List<Coupon>> {
        val userId = authRepository.currentUserIdOrNull
        return if (userId.isNullOrBlank()) flowOf(emptyList()) else couponRepository.observeCoupons(userId)
    }
}
