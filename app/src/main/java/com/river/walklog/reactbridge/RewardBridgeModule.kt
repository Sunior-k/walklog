package com.river.walklog.reactbridge

import android.content.res.Configuration
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.river.walklog.MainActivity
import com.river.walklog.core.domain.usecase.RedeemResult
import com.river.walklog.core.model.PromoCodeRedeemResult
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 리워드 스토어(React Native) ↔ WalkLog 네이티브 포인트 도메인 브릿지.
 * JS 쪽 인터페이스: reward-store-rn/src/nativeModules/RewardBridge.ts
 */
class RewardBridgeModule(
    reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {

    private val entryPoint = EntryPointAccessors.fromApplication(
        reactContext.applicationContext,
        RewardBridgeEntryPoint::class.java,
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getName() = NAME

    private fun Promise.rejectAndReport(errorCode: String, methodName: String, error: Throwable) {
        entryPoint.crashReporter().log("RewardBridgeModule.$methodName failed")
        entryPoint.crashReporter().recordException(error)
        reject(errorCode, error)
    }

    @ReactMethod
    fun getPointsBalance(promise: Promise) {
        scope.launch {
            runCatching { entryPoint.getPointsBalanceUseCase()().first() }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.rejectAndReport(ERR_POINTS_BALANCE, "getPointsBalance", it) }
        }
    }

    @ReactMethod
    fun redeemReward(itemId: String, cost: Double, promise: Promise) {
        scope.launch {
            val roundedCost = cost.roundToInt()
            runCatching {
                require(roundedCost.toDouble() == cost) { "cost must be an integer: $cost" }
                entryPoint.redeemRewardUseCase()(itemId, roundedCost)
            }
                .onSuccess { result ->
                    val status = when (result) {
                        RedeemResult.Success -> "SUCCESS"
                        RedeemResult.InsufficientBalance -> "INSUFFICIENT_BALANCE"
                        RedeemResult.SignInRequired -> "SIGN_IN_REQUIRED"
                        RedeemResult.AlreadyOwned -> "ALREADY_OWNED"
                        RedeemResult.RedemptionFailed -> "REDEMPTION_FAILED"
                    }
                    promise.resolve(status)
                }
                .onFailure { promise.rejectAndReport(ERR_REDEEM_REWARD, "redeemReward", it) }
        }
    }

    @ReactMethod
    fun isSignedIn(promise: Promise) {
        scope.launch {
            runCatching { entryPoint.authRepository().currentUserIdOrNull != null }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.rejectAndReport(ERR_AUTH_STATE, "isSignedIn", it) }
        }
    }

    @ReactMethod
    fun navigateToSignIn(promise: Promise) {
        val activity = reactApplicationContext.currentActivity as? MainActivity
        if (activity == null) {
            val error = IllegalStateException("MainActivity not available")
            entryPoint.crashReporter().log("RewardBridgeModule.navigateToSignIn failed")
            entryPoint.crashReporter().recordException(error)
            promise.reject(ERR_NAVIGATE, error)
            return
        }
        activity.runOnUiThread {
            activity.navigateToLogin()
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun getIssuedCoupons(promise: Promise) {
        scope.launch {
            runCatching { entryPoint.getIssuedCouponsUseCase()().first() }
                .onSuccess { coupons ->
                    val result = Arguments.createArray()
                    coupons.forEach { coupon ->
                        val map = Arguments.createMap()
                        map.putString("code", coupon.code)
                        map.putString("status", coupon.status.name)
                        map.putDouble("createdAtEpochMillis", coupon.createdAtEpochMillis.toDouble())
                        result.pushMap(map)
                    }
                    promise.resolve(result)
                }
                .onFailure { promise.rejectAndReport(ERR_GET_COUPONS, "getIssuedCoupons", it) }
        }
    }

    @ReactMethod
    fun markCouponUsed(code: String, promise: Promise) {
        scope.launch {
            runCatching { entryPoint.markCouponUsedUseCase()(code) }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.rejectAndReport(ERR_MARK_COUPON_USED, "markCouponUsed", it) }
        }
    }

    @ReactMethod
    fun getRewardCatalog(promise: Promise) {
        scope.launch {
            runCatching { entryPoint.getRewardCatalogUseCase()().first() }
                .onSuccess { items ->
                    val result = Arguments.createArray()
                    items.forEach { item ->
                        val map = Arguments.createMap()
                        map.putString("id", item.id)
                        map.putString("emoji", item.emoji)
                        map.putString("title", item.title)
                        map.putString("description", item.description)
                        map.putDouble("cost", item.cost.toDouble())
                        result.pushMap(map)
                    }
                    promise.resolve(result)
                }
                .onFailure { promise.rejectAndReport(ERR_GET_CATALOG, "getRewardCatalog", it) }
        }
    }

    @ReactMethod
    fun getOwnedRewardIds(promise: Promise) {
        scope.launch {
            runCatching { entryPoint.getOwnedRewardIdsUseCase()().first() }
                .onSuccess { ids ->
                    val result = Arguments.createArray()
                    ids.forEach { result.pushString(it) }
                    promise.resolve(result)
                }
                .onFailure { promise.rejectAndReport(ERR_GET_OWNED_REWARDS, "getOwnedRewardIds", it) }
        }
    }

    @ReactMethod
    fun redeemPromoCode(code: String, promise: Promise) {
        scope.launch {
            runCatching { entryPoint.redeemPromoCodeUseCase()(code) }
                .onSuccess { result ->
                    val map = Arguments.createMap()
                    when (result) {
                        is PromoCodeRedeemResult.Success -> {
                            map.putString("status", "SUCCESS")
                            map.putDouble("pointsAwarded", result.pointsAwarded.toDouble())
                        }
                        PromoCodeRedeemResult.AlreadyRedeemed -> map.putString("status", "ALREADY_REDEEMED")
                        PromoCodeRedeemResult.InvalidCode -> map.putString("status", "INVALID_CODE")
                        PromoCodeRedeemResult.SignInRequired -> map.putString("status", "SIGN_IN_REQUIRED")
                        PromoCodeRedeemResult.UnknownError -> map.putString("status", "UNKNOWN_ERROR")
                    }
                    promise.resolve(map)
                }
                .onFailure { promise.rejectAndReport(ERR_REDEEM_PROMO_CODE, "redeemPromoCode", it) }
        }
    }

    @ReactMethod
    fun getThemeState(promise: Promise) {
        scope.launch {
            runCatching {
                val isPremiumActive = entryPoint.getActiveThemeUseCase()().first()
                val nightModeFlags = reactApplicationContext.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
                val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                val map = Arguments.createMap()
                map.putBoolean("isDarkMode", isDarkMode)
                map.putBoolean("isPremiumActive", isPremiumActive)
                map
            }
                .onSuccess { promise.resolve(it) }
                .onFailure { promise.rejectAndReport(ERR_THEME_STATE, "getThemeState", it) }
        }
    }

    override fun invalidate() {
        super.invalidate()
        scope.cancel()
    }

    companion object {
        const val NAME = "WalkLogRewardBridge"
        private const val ERR_POINTS_BALANCE = "ERR_POINTS_BALANCE"
        private const val ERR_REDEEM_REWARD = "ERR_REDEEM_REWARD"
        private const val ERR_GET_COUPONS = "ERR_GET_COUPONS"
        private const val ERR_GET_CATALOG = "ERR_GET_CATALOG"
        private const val ERR_GET_OWNED_REWARDS = "ERR_GET_OWNED_REWARDS"
        private const val ERR_MARK_COUPON_USED = "ERR_MARK_COUPON_USED"
        private const val ERR_REDEEM_PROMO_CODE = "ERR_REDEEM_PROMO_CODE"
        private const val ERR_THEME_STATE = "ERR_THEME_STATE"
        private const val ERR_AUTH_STATE = "ERR_AUTH_STATE"
        private const val ERR_NAVIGATE = "ERR_NAVIGATE"
    }
}
