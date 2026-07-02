package com.river.walklog.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.entity.DailyStepEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class DailyStepDaoTest {

    private lateinit var db: WalkLogDatabase
    private lateinit var dao: DailyStepDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WalkLogDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.dailyStepDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // getForDay

    @Test
    fun `getForDay returns null when no entity exists`() = runTest {
        assertNull(dao.getForDay(19_000L))
    }

    @Test
    fun `getForDay returns entity after upsert`() = runTest {
        dao.upsert(entity(19_000L, 5_000))
        assertEquals(5_000, dao.getForDay(19_000L)?.totalSteps)
    }

    // upsert

    @Test
    fun `upsert replaces existing entity on conflict`() = runTest {
        dao.upsert(entity(19_000L, 1_000))
        dao.upsert(entity(19_000L, 8_000))
        assertEquals(8_000, dao.getForDay(19_000L)?.totalSteps)
    }

    @Test
    fun `upsert persists all fields`() = runTest {
        val e = DailyStepEntity(19_000L, 5_000, 6_000, 999L)
        dao.upsert(e)
        assertEquals(e, dao.getForDay(19_000L))
    }

    // insertIfNotExists

    @Test
    fun `insertIfNotExists inserts when no row exists`() = runTest {
        dao.insertIfNotExists(entity(19_000L, 4_000))
        assertEquals(4_000, dao.getForDay(19_000L)?.totalSteps)
    }

    @Test
    fun `insertIfNotExists does not overwrite existing entity`() = runTest {
        dao.upsert(entity(19_000L, 1_000))
        dao.insertIfNotExists(entity(19_000L, 9_999))
        assertEquals(1_000, dao.getForDay(19_000L)?.totalSteps)
    }

    // updateStepsOnly

    @Test
    fun `updateStepsOnly updates totalSteps and lastUpdatedAt`() = runTest {
        dao.upsert(entity(19_000L, 0))
        dao.updateStepsOnly(19_000L, 5_000, 100L)
        val result = dao.getForDay(19_000L)!!
        assertEquals(5_000, result.totalSteps)
        assertEquals(100L, result.lastUpdatedAt)
    }

    @Test
    fun `updateStepsOnly returns 0 when row does not exist`() = runTest {
        assertEquals(0, dao.updateStepsOnly(99_999L, 5_000, 0L))
    }

    @Test
    fun `updateStepsOnly returns 1 when row updated successfully`() = runTest {
        dao.upsert(entity(19_000L, 0))
        assertEquals(1, dao.updateStepsOnly(19_000L, 5_000, 0L))
    }

    @Test
    fun `updateStepsOnly does not change targetSteps`() = runTest {
        dao.upsert(entity(19_000L, 0, targetSteps = 8_000))
        dao.updateStepsOnly(19_000L, 5_000, 0L)
        assertEquals(8_000, dao.getForDay(19_000L)?.targetSteps)
    }

    // observeForRange

    @Test
    fun `observeForRange returns empty list when no data`() = runTest {
        assertEquals(0, dao.observeForRange(19_000L, 19_006L).first().size)
    }

    @Test
    fun `observeForRange returns only entities within range`() = runTest {
        dao.upsert(entity(18_999L, 100)) // before
        dao.upsert(entity(19_000L, 1_000))
        dao.upsert(entity(19_003L, 2_000))
        dao.upsert(entity(19_006L, 3_000))
        dao.upsert(entity(19_007L, 500)) // after

        val result = dao.observeForRange(19_000L, 19_006L).first()
        assertEquals(3, result.size)
        assertEquals(listOf(19_000L, 19_003L, 19_006L), result.map { it.dateEpochDay })
    }

    @Test
    fun `observeForRange returns entities ordered by dateEpochDay ascending`() = runTest {
        dao.upsert(entity(19_006L, 3_000))
        dao.upsert(entity(19_000L, 1_000))
        dao.upsert(entity(19_003L, 2_000))

        val result = dao.observeForRange(19_000L, 19_006L).first()
        assertEquals(listOf(19_000L, 19_003L, 19_006L), result.map { it.dateEpochDay })
    }

    @Test
    fun `observeForRange includes boundary dates`() = runTest {
        dao.upsert(entity(19_000L, 1_000))
        dao.upsert(entity(19_006L, 2_000))

        val result = dao.observeForRange(19_000L, 19_006L).first()
        assertEquals(2, result.size)
    }

    // observeForDay

    @Test
    fun `observeForDay emits null when no entity`() = runTest {
        assertNull(dao.observeForDay(19_000L).first())
    }

    @Test
    fun `observeForDay emits entity after upsert`() = runTest {
        dao.upsert(entity(19_000L, 7_000))
        assertEquals(7_000, dao.observeForDay(19_000L).first()?.totalSteps)
    }

    private fun entity(epochDay: Long, steps: Int, targetSteps: Int = 6_000) = DailyStepEntity(
        dateEpochDay = epochDay,
        totalSteps = steps,
        targetSteps = targetSteps,
        lastUpdatedAt = 0L,
    )
}
