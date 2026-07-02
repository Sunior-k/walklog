package com.river.walklog.core.domain.usecase

import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignInWithGoogleUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var useCase: SignInWithGoogleUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        userSettingsRepository = FakeUserSettingsRepository()
        useCase = SignInWithGoogleUseCase(authRepository, userSettingsRepository)
    }

    // 성공

    @Test
    fun `returns success with AuthUser on sign in`() = runTest {
        val result = useCase("id-token")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returned AuthUser uid matches auth repository result`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(uid = "user-123"))
        val result = useCase("id-token")
        assertEquals("user-123", result.getOrThrow().uid)
    }

    @Test
    fun `saves userId to UserSettingsRepository on success`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(uid = "user-abc"))
        useCase("id-token")
        assertEquals("user-abc", userSettingsRepository.settings.value.userId)
    }

    @Test
    fun `isNewUser flag is forwarded from auth repository`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = true))
        val result = useCase("id-token")
        assertTrue(result.getOrThrow().isNewUser)
    }

    // 실패

    @Test
    fun `returns failure when auth repository fails`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("auth error"))
        val result = useCase("bad-token")
        assertTrue(result.isFailure)
    }

    @Test
    fun `does not update userId on auth failure`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(userId = "existing-id"))
        authRepository.signInResult = Result.failure(RuntimeException("auth error"))
        useCase("bad-token")
        assertEquals("existing-id", userSettingsRepository.settings.value.userId)
    }

    @Test
    fun `failure result contains the original exception`() = runTest {
        val error = RuntimeException("network timeout")
        authRepository.signInResult = Result.failure(error)
        val result = useCase("bad-token")
        assertFalse(result.isSuccess)
        assertEquals(error, result.exceptionOrNull())
    }
}
