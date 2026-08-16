package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.CouponRepository
import com.river.walklog.core.model.Coupon
import javax.inject.Inject

class IssueCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(userId: String, rewardId: String, pointsCost: Int): Coupon =
        couponRepository.issueCoupon(userId, rewardId, pointsCost)
}
