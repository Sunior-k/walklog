package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestorePointsLedgerDataSource
import com.river.walklog.core.data.datasource.RemotePointsLedgerEntry
import com.river.walklog.core.database.dao.PointsLedgerDao
import com.river.walklog.core.database.entity.PointsLedgerEntity
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

class RoomPointsLedgerRepositoryTest {

    private lateinit var dao: PointsLedgerDao
    private lateinit var firestoreDataSource: FirestorePointsLedgerDataSource
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var repository: RoomPointsLedgerRepository

    @Before
    fun setUp() {
        dao = mockk()
        firestoreDataSource = mockk(relaxed = true)
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings())
        every { dao.observeAll() } returns flowOf(emptyList())
        coJustRun { dao.setRemoteId(any(), any()) }
        // relaxed mock은 Result<String>의 성공값을 안전하게 만들어내지 못해 ClassCastException이
        // 나므로 기본 성공 응답을 명시적으로 스텁 — 개별 테스트는 필요하면 더 구체적으로 재정의.
        coEvery { firestoreDataSource.upload(any(), any(), any(), any()) } returns Result.success("remote-default")
        repository = RoomPointsLedgerRepository(dao, firestoreDataSource, userSettingsRepository)
    }

    @Test
    fun `record inserts locally regardless of sign-in state`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        repository.record(20, "DAILY")

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `record skips Firestore upload for guests`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = ""))
        coEvery { dao.insert(any()) } returns 1L

        repository.record(20, "DAILY")

        coVerify(exactly = 0) { firestoreDataSource.upload(any(), any(), any(), any()) }
    }

    @Test
    fun `record uploads to Firestore and stores remoteId when signed in`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        coEvery { dao.insert(any()) } returns 1L
        coEvery { firestoreDataSource.upload("user-1", 20, "DAILY", any()) } returns Result.success("remote-1")
        coEvery { dao.setRemoteId(1L, "remote-1") } returns Unit

        repository.record(20, "DAILY")

        coVerify(exactly = 1) { dao.setRemoteId(1L, "remote-1") }
    }

    @Test
    fun `sync returns true without network calls for guests`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = ""))

        assertTrue(repository.sync())

        coVerify(exactly = 0) { firestoreDataSource.fetchAll(any()) }
    }

    @Test
    fun `sync inserts remote-only entries missing locally`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        coEvery { dao.getUnsyncedOnce() } returns emptyList()
        coEvery { dao.getAllOnce() } returns emptyList()
        coEvery { firestoreDataSource.fetchAll("user-1") } returns listOf(
            RemotePointsLedgerEntry(
                remoteId = "remote-only",
                deltaPoints = -500,
                reason = "REDEEM_coffee-coupon",
                createdAtEpochMillis = 111L,
            ),
        )
        coEvery { dao.insert(any()) } returns 3L

        assertTrue(repository.sync())

        coVerify(exactly = 1) {
            dao.insert(match { it.reason == "REDEEM_coffee-coupon" && it.remoteId == "remote-only" })
        }
    }

    @Test
    fun `sync does not reinsert entries already present locally by remoteId`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-1"))
        val existing = PointsLedgerEntity(
            id = 1L,
            deltaPoints = 20,
            reason = "DAILY",
            createdAtEpochMillis = 222L,
            remoteId = "already-synced",
        )
        coEvery { dao.getUnsyncedOnce() } returns emptyList()
        coEvery { dao.getAllOnce() } returns listOf(existing)
        coEvery { firestoreDataSource.fetchAll("user-1") } returns listOf(
            RemotePointsLedgerEntry(
                remoteId = "already-synced",
                deltaPoints = 20,
                reason = "DAILY",
                createdAtEpochMillis = 222L,
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
        val unsynced = PointsLedgerEntity(
            id = 5L,
            deltaPoints = 20,
            reason = "DAILY",
            createdAtEpochMillis = 123L,
            remoteId = null,
        )
        coEvery { dao.getUnsyncedOnce() } returns listOf(unsynced)
        coEvery {
            firestoreDataSource.upload("user-1", 20, "DAILY", 123L)
        } returns Result.failure(RuntimeException("network error"))

        assertFalse(repository.sync())
    }
}
