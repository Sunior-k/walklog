package com.river.walklog.core.domain.usecase

import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var useCase: SignOutUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings(userId = "test-uid"))
        useCase = SignOutUseCase(authRepository, userSettingsRepository)
    }

    @Test
    fun `calls signOut on auth repository`() = runTest {
        useCase()
        assertTrue(authRepository.signedOut)
    }

    @Test
    fun `clears userId in UserSettingsRepository`() = runTest {
        useCase()
        assertEquals("", userSettingsRepository.settings.value.userId)
    }

    @Test
    fun `userId is cleared even when it had a value`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "user-xyz"))
        useCase()
        assertEquals("", userSettingsRepository.settings.value.userId)
    }

    @Test
    fun `sign out clears currentUser in auth repository`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser())
        useCase()
        assertNull(authRepository.currentUser.first())
    }
}
