package com.river.walklog.core.datastore

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `preferencesDataStore` 위임 프로퍼티는 JVM/클래스로더 단위로 공유되는 싱글턴이라, Robolectric이
 * 테스트 메서드마다 새 샌드박스를 만들어주지 않으면 파일 상태가 테스트 간에 그대로 남는다.
 * [tearDown]에서 공개 API로 포인트/미션 지급일을 초기화해 각 테스트를 격리한다.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class UserPreferencesDataSourceTest {

    private lateinit var dataSource: UserPreferencesDataSource

    @Before
    fun setUp() {
        dataSource = UserPreferencesDataSource(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() = runTest {
        val current = dataSource.settings.first()
        if (current.totalPoints > 0) dataSource.trySpendPoints(current.totalPoints)
        dataSource.setLastDailyMissionAwardedDate("")
        dataSource.setLastRecoveryMissionAwardedDate("")
    }

    // trySpendPoints

    @Test
    fun `trySpendPoints returns false and does not deduct when balance is insufficient`() = runTest {
        dataSource.addPoints(50)

        val result = dataSource.trySpendPoints(100)

        assertFalse(result)
        assertEquals(50, dataSource.settings.first().totalPoints)
    }

    @Test
    fun `trySpendPoints returns true and deducts when balance is sufficient`() = runTest {
        dataSource.addPoints(100)

        val result = dataSource.trySpendPoints(60)

        assertTrue(result)
        assertEquals(40, dataSource.settings.first().totalPoints)
    }

    @Test
    fun `trySpendPoints succeeds when amount exactly equals balance`() = runTest {
        dataSource.addPoints(100)

        val result = dataSource.trySpendPoints(100)

        assertTrue(result)
        assertEquals(0, dataSource.settings.first().totalPoints)
    }

    // tryMarkDailyMissionAwarded

    @Test
    fun `tryMarkDailyMissionAwarded returns true and marks the date on first call`() = runTest {
        val result = dataSource.tryMarkDailyMissionAwarded("2026-05-13")

        assertTrue(result)
        assertEquals("2026-05-13", dataSource.settings.first().lastDailyMissionAwardedDate)
    }

    @Test
    fun `tryMarkDailyMissionAwarded returns false when already awarded for that date`() = runTest {
        dataSource.tryMarkDailyMissionAwarded("2026-05-13")

        val result = dataSource.tryMarkDailyMissionAwarded("2026-05-13")

        assertFalse(result)
    }

    @Test
    fun `tryMarkDailyMissionAwarded returns true again for a new date`() = runTest {
        dataSource.tryMarkDailyMissionAwarded("2026-05-13")

        val result = dataSource.tryMarkDailyMissionAwarded("2026-05-14")

        assertTrue(result)
        assertEquals("2026-05-14", dataSource.settings.first().lastDailyMissionAwardedDate)
    }

    // tryMarkRecoveryMissionAwarded

    @Test
    fun `tryMarkRecoveryMissionAwarded returns true and marks the date on first call`() = runTest {
        val result = dataSource.tryMarkRecoveryMissionAwarded("2026-05-13")

        assertTrue(result)
        assertEquals("2026-05-13", dataSource.settings.first().lastRecoveryMissionAwardedDate)
    }

    @Test
    fun `tryMarkRecoveryMissionAwarded returns false when already awarded for that date`() = runTest {
        dataSource.tryMarkRecoveryMissionAwarded("2026-05-13")

        val result = dataSource.tryMarkRecoveryMissionAwarded("2026-05-13")

        assertFalse(result)
    }

    @Test
    fun `tryMarkRecoveryMissionAwarded returns true again for a new date`() = runTest {
        dataSource.tryMarkRecoveryMissionAwarded("2026-05-13")

        val result = dataSource.tryMarkRecoveryMissionAwarded("2026-05-14")

        assertTrue(result)
        assertEquals("2026-05-14", dataSource.settings.first().lastRecoveryMissionAwardedDate)
    }
}
