package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.data.repository.ActivityStateRepository
import com.river.walklog.core.model.ActivityState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ObserveActivityStateUseCaseTest {

    private lateinit var repository: ActivityStateRepository
    private lateinit var useCase: ObserveActivityStateUseCase

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.isStationary } returns MutableStateFlow(false)
        useCase = ObserveActivityStateUseCase(repository)
    }

    @Test
    fun `invoke returns empty flow when model is unavailable`() = runTest {
        every { repository.isModelAvailable } returns false

        useCase().test {
            awaitComplete()
        }
    }

    @Test
    fun `invoke delegates to repository when model is available`() = runTest {
        every { repository.isModelAvailable } returns true
        every { repository.observeActivityState() } returns flowOf(ActivityState.WALKING)

        useCase().test {
            assertEquals(ActivityState.WALKING, awaitItem())
            awaitComplete()
        }
    }
}
