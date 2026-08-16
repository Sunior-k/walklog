package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestorePromoCodeDataSource
import com.river.walklog.core.model.PromoCodeRedeemResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePromoCodeRepository @Inject constructor(
    private val dataSource: FirestorePromoCodeDataSource,
) : PromoCodeRepository {

    override suspend fun redeem(userId: String, code: String): PromoCodeRedeemResult =
        dataSource.redeem(userId, code)
}
