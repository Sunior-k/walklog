package com.river.walklog.core.data.repository

import com.river.walklog.core.model.PromoCodeRedeemResult

interface PromoCodeRepository {
    suspend fun redeem(userId: String, code: String): PromoCodeRedeemResult
}
