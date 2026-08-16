package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.CouponRepository
import com.river.walklog.core.model.Coupon
import com.river.walklog.core.model.CouponStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCouponRepository : CouponRepository {

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: MutableStateFlow<List<Coupon>> = _coupons

    var nextCode: String = "WALK-TEST01"
    var shouldThrowOnIssue: Boolean = false

    override fun observeCoupons(userId: String): Flow<List<Coupon>> = _coupons

    override suspend fun issueCoupon(userId: String, rewardId: String, pointsCost: Int): Coupon {
        if (shouldThrowOnIssue) error("simulated coupon issue failure")
        val coupon = Coupon(
            code = nextCode,
            rewardId = rewardId,
            pointsCost = pointsCost,
            status = CouponStatus.ISSUED,
            createdAtEpochMillis = System.currentTimeMillis(),
            usedAtEpochMillis = null,
        )
        _coupons.value = _coupons.value + coupon
        return coupon
    }

    override suspend fun markUsed(code: String, userId: String): Boolean {
        val target = _coupons.value.firstOrNull { it.code == code && it.status == CouponStatus.ISSUED }
            ?: return false
        _coupons.value = _coupons.value.map {
            if (it.code == code) it.copy(status = CouponStatus.USED, usedAtEpochMillis = System.currentTimeMillis()) else it
        }
        return true
    }
}
