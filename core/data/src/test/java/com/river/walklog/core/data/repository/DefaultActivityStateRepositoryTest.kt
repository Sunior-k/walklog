package com.river.walklog.core.data.repository

import app.cash.turbine.test
import com.river.walklog.core.engine.ActivityClassifier
import com.river.walklog.core.engine.EngineActivityState
import com.river.walklog.core.model.ActivityState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultActivityStateRepositoryTest {

    private lateinit var activityClassifier: ActivityClassifier
    private lateinit var repository: DefaultActivityStateRepository

    @Before
    fun setUp() {
        activityClassifier = mockk()
        repository = DefaultActivityStateRepository(activityClassifier)
    }

    // isModelAvailable

    @Test
    fun `isModelAvailable returns true when classifier is available`() {
        every { activityClassifier.isModelAvailable } returns true
        assertTrue(repository.isModelAvailable)
    }

    @Test
    fun `isModelAvailable returns false when classifier is unavailable`() {
        every { activityClassifier.isModelAvailable } returns false
        assertFalse(repository.isModelAvailable)
    }

    // isStationary

    @Test
    fun `isStationary reflects initial false state from classifier`() {
        every { activityClassifier.isStationary } returns MutableStateFlow(false)
        assertFalse(repository.isStationary.value)
    }

    @Test
    fun `isStationary reflects true state from classifier`() {
        every { activityClassifier.isStationary } returns MutableStateFlow(true)
        assertTrue(repository.isStationary.value)
    }

    // observeActivityState — state mapping

    @Test
    fun `observeActivityState maps EngineActivityState WALKING to ActivityState WALKING`() = runTest {
        every { activityClassifier.observeActivityState() } returns flowOf(EngineActivityState.WALKING)

        repository.observeActivityState().test {
            assertEquals(ActivityState.WALKING, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeActivityState maps EngineActivityState STATIONARY to ActivityState STATIONARY`() = runTest {
        every { activityClassifier.observeActivityState() } returns flowOf(EngineActivityState.STATIONARY)

        repository.observeActivityState().test {
            assertEquals(ActivityState.STATIONARY, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeActivityState maps EngineActivityState UNKNOWN to ActivityState UNKNOWN`() = runTest {
        every { activityClassifier.observeActivityState() } returns flowOf(EngineActivityState.UNKNOWN)

        repository.observeActivityState().test {
            assertEquals(ActivityState.UNKNOWN, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeActivityState emits all values in order from classifier`() = runTest {
        every { activityClassifier.observeActivityState() } returns flowOf(
            EngineActivityState.WALKING,
            EngineActivityState.STATIONARY,
            EngineActivityState.UNKNOWN,
        )

        repository.observeActivityState().test {
            assertEquals(ActivityState.WALKING, awaitItem())
            assertEquals(ActivityState.STATIONARY, awaitItem())
            assertEquals(ActivityState.UNKNOWN, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeActivityState covers all EngineActivityState values with 1-to-1 name mapping`() {
        EngineActivityState.entries.forEach { engineState ->
            val expectedName = engineState.name
            val matchingDomainState = ActivityState.entries.firstOrNull { it.name == expectedName }
            assertEquals(
                expectedName,
                matchingDomainState?.name,
                "EngineActivityState.$engineState has no matching ActivityState",
            )
        }
    }
}
