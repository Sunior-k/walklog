package com.river.walklog.core.data.repository

import com.river.walklog.core.model.Coupon
import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    fun observeCoupons(userId: String): Flow<List<Coupon>>
    suspend fun issueCoupon(userId: String, rewardId: String, pointsCost: Int): Coupon
    suspend fun markUsed(code: String, userId: String): Boolean
}
