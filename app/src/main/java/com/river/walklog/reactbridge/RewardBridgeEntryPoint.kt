package com.river.walklog.reactbridge

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.domain.usecase.GetActiveThemeUseCase
import com.river.walklog.core.domain.usecase.GetIssuedCouponsUseCase
import com.river.walklog.core.domain.usecase.GetOwnedRewardIdsUseCase
import com.river.walklog.core.domain.usecase.GetPointsBalanceUseCase
import com.river.walklog.core.domain.usecase.GetRewardCatalogUseCase
import com.river.walklog.core.domain.usecase.MarkCouponUsedUseCase
import com.river.walklog.core.domain.usecase.RedeemPromoCodeUseCase
import com.river.walklog.core.domain.usecase.RedeemRewardUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * RewardBridgeModule은 React Native가 직접 생성하는 NativeModule이라 Hilt 그래프에 편입될 수 없다.
 * Application Context를 통해 UseCase를 꺼내오기 위한 진입점.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RewardBridgeEntryPoint {
    fun getPointsBalanceUseCase(): GetPointsBalanceUseCase
    fun redeemRewardUseCase(): RedeemRewardUseCase
    fun getIssuedCouponsUseCase(): GetIssuedCouponsUseCase
    fun markCouponUsedUseCase(): MarkCouponUsedUseCase
    fun getActiveThemeUseCase(): GetActiveThemeUseCase
    fun getRewardCatalogUseCase(): GetRewardCatalogUseCase
    fun getOwnedRewardIdsUseCase(): GetOwnedRewardIdsUseCase
    fun redeemPromoCodeUseCase(): RedeemPromoCodeUseCase
    fun authRepository(): AuthRepository
    fun crashReporter(): CrashReporter
}
