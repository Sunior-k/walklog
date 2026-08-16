package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestoreRewardRedemptionDataSource
import com.river.walklog.core.data.datasource.RemoteRewardRedemption
import com.river.walklog.core.database.dao.RewardRedemptionDao
import com.river.walklog.core.database.entity.RewardRedemptionEntity
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomRewardRedemptionRepositoryTest {

    private lateinit var dao: RewardRedemptionDao
    private lateinit var firestoreDataSource: FirestoreRewardRedemptionDataSource
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var repository: RoomRewardRedemptionRepository

    @Before
    fun setUp() {
        dao = mockk()
        firestoreDataSource = mockk(relaxed = true)
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings())
        every { dao.observeAll() } returns flowOf(emptyList())
        every { dao.observeByRewardId(any()) } returns flowOf(emptyList())
        coJustRun { dao.setRemoteId(any(), any()) }
        // relaxed mock은 Result<String>의 성공값을 안전하게 만들어내지 못해 ClassCastException이
        // 나므로 기본 성공 응답을 명시적으로 스텁 — 개별 테스트는 필요하면 더 구체적으로 재정의.
        coEvery {
            firestoreDataSource.upload(any(), any(), any(), any(), any())
        } returns Result.success("remote-default")
        repository = RoomRewardRedemptionRepository(dao, firestoreDataSource, userSettingsRepository)
    }

    @Test
    fun `recordRedemption inserts locally regardless of sign-in state`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        repository.recordRedemption("walk-badge-gold", 200, null)

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `recordRedemption skips Firestore upload for guests`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = ""))
        coEvery { dao.insert(any()) } returns 1L

        repository.recordRedemption("walk-badge-gold", 200, null)

        coVerify(exactly = 0) { firestoreDataSource.upload(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `recordRedemption uploads to Firestore and stores remoteId when signed in`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        coEvery { dao.insert(any()) } returns 1L
        coEvery { dao.setRemoteId(1L, "remote-1") } returns Unit
        coEvery {
            firestoreDataSource.upload("user-1", "walk-badge-gold", 200, any(), null)
        } returns Result.success("remote-1")

        repository.recordRedemption("walk-badge-gold", 200, null)

        coVerify(exactly = 1) { dao.setRemoteId(1L, "remote-1") }
    }

    @Test
    fun `sync returns true without network calls for guests`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = ""))

        assertTrue(repository.sync())

        coVerify(exactly = 0) { firestoreDataSource.fetchAll(any()) }
    }

    @Test
    fun `sync pushes unsynced local entries to Firestore`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        val unsynced = RewardRedemptionEntity(
            id = 5L,
            rewardId = "theme-pack",
            pointsCost = 800,
            redeemedAtEpochMillis = 123L,
            couponCode = null,
            remoteId = null,
        )
        coEvery { dao.getUnsyncedOnce() } returns listOf(unsynced)
        coEvery { dao.getAllOnce() } returns listOf(unsynced)
        coEvery {
            firestoreDataSource.upload("user-1", "theme-pack", 800, 123L, null)
        } returns Result.success("remote-9")
        coEvery { dao.setRemoteId(5L, "remote-9") } returns Unit
        coEvery { firestoreDataSource.fetchAll("user-1") } returns emptyList()

        assertTrue(repository.sync())

        coVerify(exactly = 1) { dao.setRemoteId(5L, "remote-9") }
    }

    @Test
    fun `sync inserts remote-only entries missing locally`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        coEvery { dao.getUnsyncedOnce() } returns emptyList()
        coEvery { dao.getAllOnce() } returns emptyList()
        coEvery { firestoreDataSource.fetchAll("user-1") } returns listOf(
            RemoteRewardRedemption(
                remoteId = "remote-only",
                rewardId = "donation-500",
                pointsCost = 500,
                redeemedAtEpochMillis = 456L,
                couponCode = null,
            ),
        )
        coEvery { dao.insert(any()) } returns 2L

        assertTrue(repository.sync())

        coVerify(exactly = 1) {
            dao.insert(
                match {
                    it.rewardId == "donation-500" && it.remoteId == "remote-only"
                },
            )
        }
    }

    @Test
    fun `sync does not reinsert entries already present locally by remoteId`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        val existing = RewardRedemptionEntity(
            id = 1L,
            rewardId = "coffee-coupon",
            pointsCost = 500,
            redeemedAtEpochMillis = 789L,
            couponCode = "WALK-ABC123",
            remoteId = "already-synced",
        )
        coEvery { dao.getUnsyncedOnce() } returns emptyList()
        coEvery { dao.getAllOnce() } returns listOf(existing)
        coEvery { firestoreDataSource.fetchAll("user-1") } returns listOf(
            RemoteRewardRedemption(
                remoteId = "already-synced",
                rewardId = "coffee-coupon",
                pointsCost = 500,
                redeemedAtEpochMillis = 789L,
                couponCode = "WALK-ABC123",
            ),
        )

        assertTrue(repository.sync())

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `sync returns false when Firestore call throws`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        coEvery { dao.getUnsyncedOnce() } throws RuntimeException("boom")

        assertFalse(repository.sync())
    }

    @Test
    fun `sync returns false when an upload fails so WorkManager retries`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        val unsynced = RewardRedemptionEntity(
            id = 5L,
            rewardId = "theme-pack",
            pointsCost = 800,
            redeemedAtEpochMillis = 123L,
            couponCode = null,
            remoteId = null,
        )
        coEvery { dao.getUnsyncedOnce() } returns listOf(unsynced)
        coEvery {
            firestoreDataSource.upload("user-1", "theme-pack", 800, 123L, null)
        } returns Result.failure(RuntimeException("network error"))

        assertFalse(repository.sync())
    }
}
