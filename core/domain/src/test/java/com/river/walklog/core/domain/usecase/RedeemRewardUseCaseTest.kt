package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeCouponRepository
import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import com.river.walklog.core.testing.repository.FakeRewardRedemptionRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RedeemRewardUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var pointsLedgerRepository: FakePointsLedgerRepository
    private lateinit var rewardRedemptionRepository: FakeRewardRedemptionRepository
    private lateinit var couponRepository: FakeCouponRepository
    private lateinit var useCase: RedeemRewardUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository().apply { setCurrentUser(defaultAuthUser()) }
        userSettingsRepository = FakeUserSettingsRepository()
        pointsLedgerRepository = FakePointsLedgerRepository()
        rewardRedemptionRepository = FakeRewardRedemptionRepository()
        couponRepository = FakeCouponRepository()
        useCase = RedeemRewardUseCase(
            authRepository = authRepository,
            userSettingsRepository = userSettingsRepository,
            pointsLedgerRepository = pointsLedgerRepository,
            rewardRedemptionRepository = rewardRedemptionRepository,
            issueCouponUseCase = IssueCouponUseCase(couponRepository),
        )
    }

    @Test
    fun `guest without sign-in returns SignInRequired`() = runTest {
        authRepository.setCurrentUser(null)
        stubBalance(1000)

        assertEquals(RedeemResult.SignInRequired, useCase(RewardCatalogIds.BADGE_GOLD, 500))
    }

    @Test
    fun `guest without sign-in does not deduct points`() = runTest {
        authRepository.setCurrentUser(null)
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 500)

        assertEquals(1000, currentBalance())
    }

    @Test
    fun `sufficient balance redeems and returns Success`() = runTest {
        stubBalance(1000)

        assertEquals(RedeemResult.Success, useCase(RewardCatalogIds.BADGE_GOLD, 500))
    }

    @Test
    fun `sufficient balance deducts exact cost`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 500)

        assertEquals(500, currentBalance())
    }

    @Test
    fun `balance equal to cost redeems successfully`() = runTest {
        stubBalance(500)

        assertEquals(RedeemResult.Success, useCase(RewardCatalogIds.BADGE_GOLD, 500))
    }

    @Test
    fun `insufficient balance returns InsufficientBalance`() = runTest {
        stubBalance(100)

        assertEquals(RedeemResult.InsufficientBalance, useCase(RewardCatalogIds.BADGE_GOLD, 500))
    }

    @Test
    fun `insufficient balance leaves balance untouched`() = runTest {
        stubBalance(100)

        useCase(RewardCatalogIds.BADGE_GOLD, 500)

        assertEquals(100, currentBalance())
    }

    @Test
    fun `insufficient balance records no redemption`() = runTest {
        stubBalance(100)

        useCase(RewardCatalogIds.BADGE_GOLD, 500)

        assertTrue(rewardRedemptionRepository.redemptions.value.isEmpty())
    }

    @Test
    fun `redeem records points ledger entry with negative delta and reason`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        val entry = pointsLedgerRepository.entries.value.single()
        assertEquals(-200, entry.deltaPoints)
        assertEquals("REDEEM_${RewardCatalogIds.BADGE_GOLD}", entry.reason)
    }

    @Test
    fun `redeem records reward redemption with matching rewardId and cost`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        val redemption = rewardRedemptionRepository.redemptions.value.single()
        assertEquals(RewardCatalogIds.BADGE_GOLD, redemption.rewardId)
        assertEquals(200, redemption.pointsCost)
    }

    @Test
    fun `redeeming coffee coupon issues a coupon via CouponRepository`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        val coupon = couponRepository.coupons.value.single()
        assertEquals(RewardCatalogIds.COFFEE_COUPON, coupon.rewardId)
    }

    @Test
    fun `redeeming coffee coupon does not persist coupon code in local redemption record`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        val redemption = rewardRedemptionRepository.redemptions.value.single()
        assertNull(redemption.couponCode)
    }

    @Test
    fun `redeeming non-coupon reward does not issue a coupon`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        assertTrue(couponRepository.coupons.value.isEmpty())
    }

    @Test
    fun `redeeming theme pack activates premium theme`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.THEME_PACK, 800)

        assertTrue(userSettingsRepository.settings.value.isPremiumThemeActive)
    }

    @Test
    fun `redeeming non theme-pack reward does not touch theme setting`() = runTest {
        stubBalance(1000)

        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        assertTrue(!userSettingsRepository.settings.value.isPremiumThemeActive)
    }

    @Test
    fun `redeeming an already owned badge returns AlreadyOwned`() = runTest {
        stubBalance(1000)
        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        assertEquals(RedeemResult.AlreadyOwned, useCase(RewardCatalogIds.BADGE_GOLD, 200))
    }

    @Test
    fun `redeeming an already owned theme pack does not deduct points again`() = runTest {
        stubBalance(1000)
        useCase(RewardCatalogIds.THEME_PACK, 800)

        useCase(RewardCatalogIds.THEME_PACK, 800)

        assertEquals(200, currentBalance())
    }

    @Test
    fun `redeeming an already owned reward records no additional redemption`() = runTest {
        stubBalance(1000)
        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        useCase(RewardCatalogIds.BADGE_GOLD, 200)

        assertEquals(1, rewardRedemptionRepository.redemptions.value.size)
    }

    @Test
    fun `redeeming coffee coupon repeatedly succeeds each time`() = runTest {
        stubBalance(1000)
        useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        assertEquals(RedeemResult.Success, useCase(RewardCatalogIds.COFFEE_COUPON, 500))
    }

    @Test
    fun `redeeming donation repeatedly succeeds each time`() = runTest {
        stubBalance(2000)
        useCase(RewardCatalogIds.DONATION, 500)

        assertEquals(RedeemResult.Success, useCase(RewardCatalogIds.DONATION, 500))
    }

    @Test
    fun `coupon issue failure refunds points and returns RedemptionFailed`() = runTest {
        stubBalance(1000)
        couponRepository.shouldThrowOnIssue = true

        val result = useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        assertEquals(RedeemResult.RedemptionFailed, result)
        assertEquals(1000, currentBalance())
    }

    @Test
    fun `coupon issue failure records no redemption`() = runTest {
        stubBalance(1000)
        couponRepository.shouldThrowOnIssue = true

        useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        assertTrue(rewardRedemptionRepository.redemptions.value.isEmpty())
    }

    @Test
    fun `coupon issue failure records offsetting ledger entries`() = runTest {
        stubBalance(1000)
        couponRepository.shouldThrowOnIssue = true

        useCase(RewardCatalogIds.COFFEE_COUPON, 500)

        // FakePointsLedgerRepository는 최신 항목을 맨 앞에 붙이므로 순서는 [환불, 차감].
        val entries = pointsLedgerRepository.entries.value
        assertEquals(500, entries[0].deltaPoints)
        assertEquals("REDEEM_REFUND_${RewardCatalogIds.COFFEE_COUPON}", entries[0].reason)
        assertEquals(-500, entries[1].deltaPoints)
    }

    private fun stubBalance(totalPoints: Int) {
        userSettingsRepository.setSettings(defaultUserSettings(totalPoints = totalPoints))
    }

    private fun currentBalance(): Int = userSettingsRepository.settings.value.totalPoints
}
