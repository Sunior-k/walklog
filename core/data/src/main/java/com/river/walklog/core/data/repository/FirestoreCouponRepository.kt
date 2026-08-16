package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestoreCouponDataSource
import com.river.walklog.core.model.Coupon
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCouponRepository @Inject constructor(
    private val dataSource: FirestoreCouponDataSource,
) : CouponRepository {

    override fun observeCoupons(userId: String): Flow<List<Coupon>> =
        dataSource.observeCoupons(userId)

    override suspend fun issueCoupon(userId: String, rewardId: String, pointsCost: Int): Coupon =
        dataSource.issueCoupon(userId, rewardId, pointsCost)

    override suspend fun markUsed(code: String, userId: String): Boolean =
        dataSource.markUsed(code, userId)
}
