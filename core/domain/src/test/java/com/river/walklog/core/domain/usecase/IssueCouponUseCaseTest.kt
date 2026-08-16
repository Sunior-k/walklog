package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.CouponStatus
import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeCouponRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class IssueCouponUseCaseTest {

    private val couponRepository = FakeCouponRepository()
    private val useCase = IssueCouponUseCase(couponRepository)

    @Test
    fun `issues a coupon with ISSUED status for the given reward`() = runTest {
        val coupon = useCase("user-1", RewardCatalogIds.COFFEE_COUPON, 500)

        assertEquals(RewardCatalogIds.COFFEE_COUPON, coupon.rewardId)
        assertEquals(500, coupon.pointsCost)
        assertEquals(CouponStatus.ISSUED, coupon.status)
    }

    @Test
    fun `issued coupon is persisted in the repository`() = runTest {
        useCase("user-1", RewardCatalogIds.COFFEE_COUPON, 500)

        assertEquals(1, couponRepository.coupons.value.size)
    }
}
