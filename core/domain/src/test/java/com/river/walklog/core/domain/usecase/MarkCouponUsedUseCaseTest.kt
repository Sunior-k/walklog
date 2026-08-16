package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeCouponRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkCouponUsedUseCaseTest {

    private val couponRepository = FakeCouponRepository()
    private val authRepository = FakeAuthRepository()
    private val useCase = MarkCouponUsedUseCase(couponRepository, authRepository)

    @Test
    fun `returns false when no user is signed in`() = runTest {
        authRepository.setCurrentUser(null)

        assertFalse(useCase("WALK-TEST01"))
    }

    @Test
    fun `marks an issued coupon as used when signed in`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser(uid = "user-1"))
        couponRepository.issueCoupon("user-1", RewardCatalogIds.COFFEE_COUPON, 500)
        val code = couponRepository.coupons.value.single().code

        assertTrue(useCase(code))
    }

    @Test
    fun `returns false for an unknown coupon code`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser(uid = "user-1"))

        assertFalse(useCase("WALK-UNKNOWN"))
    }
}
