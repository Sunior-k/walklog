package com.river.walklog.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.database.dao.PointsLedgerDao
import com.river.walklog.core.database.entity.PointsLedgerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PointsLedgerDaoTest {

    private lateinit var db: WalkLogDatabase
    private lateinit var dao: PointsLedgerDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WalkLogDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.pointsLedgerDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeAll returns empty list when no entries`() = runTest {
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun `insert then observeAll returns the entry`() = runTest {
        dao.insert(entity(delta = 20, reason = "DAILY"))

        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals(20, result.single().deltaPoints)
        assertEquals("DAILY", result.single().reason)
    }

    @Test
    fun `observeAll orders by createdAtEpochMillis descending`() = runTest {
        dao.insert(entity(delta = 20, reason = "DAILY", createdAt = 100L))
        dao.insert(entity(delta = -200, reason = "REDEEM_walk-badge-gold", createdAt = 300L))
        dao.insert(entity(delta = 10, reason = "RECOVERY", createdAt = 200L))

        val result = dao.observeAll().first()
        assertEquals(listOf(-200, 10, 20), result.map { it.deltaPoints })
    }

    @Test
    fun `negative delta persists correctly`() = runTest {
        dao.insert(entity(delta = -500, reason = "REDEEM_donation-500"))

        assertEquals(-500, dao.observeAll().first().single().deltaPoints)
    }

    private fun entity(delta: Int, reason: String, createdAt: Long = 0L) = PointsLedgerEntity(
        deltaPoints = delta,
        reason = reason,
        createdAtEpochMillis = createdAt,
    )
}
