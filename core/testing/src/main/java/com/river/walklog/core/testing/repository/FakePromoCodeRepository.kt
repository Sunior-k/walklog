package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.PromoCodeRepository
import com.river.walklog.core.model.PromoCodeRedeemResult

class FakePromoCodeRepository : PromoCodeRepository {

    var result: PromoCodeRedeemResult = PromoCodeRedeemResult.InvalidCode
    var lastUserId: String? = null
    var lastCode: String? = null

    override suspend fun redeem(userId: String, code: String): PromoCodeRedeemResult {
        lastUserId = userId
        lastCode = code
        return result
    }
}
