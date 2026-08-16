package com.river.walklog.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.database.dao.RewardRedemptionDao
import com.river.walklog.core.database.entity.RewardRedemptionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class RewardRedemptionDaoTest {

    private lateinit var db: WalkLogDatabase
    private lateinit var dao: RewardRedemptionDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WalkLogDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.rewardRedemptionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeAll returns empty list when no redemptions`() = runTest {
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun `insert then observeAll returns the redemption`() = runTest {
        dao.insert(entity(rewardId = "walk-badge-gold", cost = 200))

        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("walk-badge-gold", result.single().rewardId)
    }

    @Test
    fun `observeAll orders by redeemedAtEpochMillis descending`() = runTest {
        dao.insert(entity(rewardId = "coffee-coupon", cost = 500, redeemedAt = 100L))
        dao.insert(entity(rewardId = "theme-pack", cost = 800, redeemedAt = 300L))
        dao.insert(entity(rewardId = "donation-500", cost = 500, redeemedAt = 200L))

        val result = dao.observeAll().first()
        assertEquals(listOf("theme-pack", "donation-500", "coffee-coupon"), result.map { it.rewardId })
    }

    @Test
    fun `observeByRewardId filters to matching rewardId only`() = runTest {
        dao.insert(entity(rewardId = "donation-500", cost = 500))
        dao.insert(entity(rewardId = "donation-500", cost = 500))
        dao.insert(entity(rewardId = "walk-badge-gold", cost = 200))

        val result = dao.observeByRewardId("donation-500").first()
        assertEquals(2, result.size)
    }

    @Test
    fun `observeByRewardId returns empty list when rewardId never redeemed`() = runTest {
        dao.insert(entity(rewardId = "walk-badge-gold", cost = 200))

        assertTrue(dao.observeByRewardId("theme-pack").first().isEmpty())
    }

    @Test
    fun `couponCode is null when not provided`() = runTest {
        dao.insert(entity(rewardId = "walk-badge-gold", cost = 200, couponCode = null))

        assertNull(dao.observeAll().first().single().couponCode)
    }

    @Test
    fun `couponCode persists when provided`() = runTest {
        dao.insert(entity(rewardId = "coffee-coupon", cost = 500, couponCode = "WALK-ABC123"))

        assertEquals("WALK-ABC123", dao.observeAll().first().single().couponCode)
    }

    private fun entity(
        rewardId: String,
        cost: Int,
        redeemedAt: Long = 0L,
        couponCode: String? = null,
    ) = RewardRedemptionEntity(
        rewardId = rewardId,
        pointsCost = cost,
        redeemedAtEpochMillis = redeemedAt,
        couponCode = couponCode,
    )
}
