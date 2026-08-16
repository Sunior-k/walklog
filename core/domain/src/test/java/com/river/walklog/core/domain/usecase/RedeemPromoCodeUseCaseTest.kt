package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.PromoCodeRedeemResult
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import com.river.walklog.core.testing.repository.FakePromoCodeRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RedeemPromoCodeUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var promoCodeRepository: FakePromoCodeRepository
    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var pointsLedgerRepository: FakePointsLedgerRepository
    private lateinit var useCase: RedeemPromoCodeUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository().apply { setCurrentUser(defaultAuthUser()) }
        promoCodeRepository = FakePromoCodeRepository()
        userSettingsRepository = mockk()
        pointsLedgerRepository = FakePointsLedgerRepository()
        useCase = RedeemPromoCodeUseCase(
            authRepository = authRepository,
            promoCodeRepository = promoCodeRepository,
            userSettingsRepository = userSettingsRepository,
            pointsLedgerRepository = pointsLedgerRepository,
        )
        coJustRun { userSettingsRepository.addPoints(any()) }
    }

    @Test
    fun `guest without sign-in returns SignInRequired`() = runTest {
        authRepository.setCurrentUser(null)

        assertEquals(PromoCodeRedeemResult.SignInRequired, useCase("WALKLOG100"))
    }

    @Test
    fun `guest without sign-in does not call repository`() = runTest {
        authRepository.setCurrentUser(null)

        useCase("WALKLOG100")

        assertNull(promoCodeRepository.lastCode)
    }

    @Test
    fun `signed-in user delegates to repository with uid and code`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.Success(pointsAwarded = 100)

        useCase("WALKLOG100")

        assertEquals("test-uid", promoCodeRepository.lastUserId)
        assertEquals("WALKLOG100", promoCodeRepository.lastCode)
    }

    @Test
    fun `success awards points to user settings`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.Success(pointsAwarded = 100)

        useCase("WALKLOG100")

        coVerify(exactly = 1) { userSettingsRepository.addPoints(100) }
    }

    @Test
    fun `success records a points ledger entry with promo code reason`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.Success(pointsAwarded = 100)

        useCase("walklog100")

        val entry = pointsLedgerRepository.entries.value.single()
        assertEquals(100, entry.deltaPoints)
        assertEquals("PROMO_CODE_WALKLOG100", entry.reason)
    }

    @Test
    fun `success returns the result unchanged`() = runTest {
        val success = PromoCodeRedeemResult.Success(pointsAwarded = 100)
        promoCodeRepository.result = success

        assertEquals(success, useCase("WALKLOG100"))
    }

    @Test
    fun `already redeemed does not award points again`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.AlreadyRedeemed

        assertEquals(PromoCodeRedeemResult.AlreadyRedeemed, useCase("WALKLOG100"))
        coVerify(exactly = 0) { userSettingsRepository.addPoints(any()) }
        assertTrue(pointsLedgerRepository.entries.value.isEmpty())
    }

    @Test
    fun `invalid code does not award points`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.InvalidCode

        assertEquals(PromoCodeRedeemResult.InvalidCode, useCase("BOGUS"))
        coVerify(exactly = 0) { userSettingsRepository.addPoints(any()) }
    }

    @Test
    fun `unknown error does not award points`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.UnknownError

        assertEquals(PromoCodeRedeemResult.UnknownError, useCase("WALKLOG100"))
        coVerify(exactly = 0) { userSettingsRepository.addPoints(any()) }
    }

    @Test
    fun `local apply failure retries three times then throws`() = runTest {
        promoCodeRepository.result = PromoCodeRedeemResult.Success(pointsAwarded = 100)
        coEvery { userSettingsRepository.addPoints(any()) } throws RuntimeException("db error")

        assertFailsWith<RuntimeException> { useCase("WALKLOG100") }

        coVerify(exactly = 3) { userSettingsRepository.addPoints(any()) }
    }
}
