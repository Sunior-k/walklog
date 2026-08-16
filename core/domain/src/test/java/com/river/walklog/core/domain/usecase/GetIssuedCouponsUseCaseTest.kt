package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeCouponRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetIssuedCouponsUseCaseTest {

    private val couponRepository = FakeCouponRepository()
    private val authRepository = FakeAuthRepository()
    private val useCase = GetIssuedCouponsUseCase(couponRepository, authRepository)

    @Test
    fun `returns empty list when no user is signed in`() = runTest {
        authRepository.setCurrentUser(null)

        useCase().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `returns issued coupons when signed in`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser(uid = "user-1"))
        couponRepository.issueCoupon("user-1", RewardCatalogIds.COFFEE_COUPON, 500)

        useCase().test {
            assertEquals(1, awaitItem().size)
        }
    }
}
