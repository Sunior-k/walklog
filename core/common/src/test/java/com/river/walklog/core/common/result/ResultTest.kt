package com.river.walklog.core.common.result

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResultTest {

    // onSuccess tests

    @Test
    fun `onSuccess invokes action for Success`() {
        var invoked = false
        Result.Success("data").onSuccess { invoked = true }
        assertTrue(invoked)
    }

    @Test
    fun `onSuccess passes correct data to action`() {
        var received: String? = null
        Result.Success("hello").onSuccess { received = it }
        assertEquals("hello", received)
    }

    @Test
    fun `onSuccess does not invoke action for Error`() {
        var invoked = false
        Result.Error(RuntimeException()).onSuccess { invoked = true }
        assertFalse(invoked)
    }

    @Test
    fun `onSuccess does not invoke action for Loading`() {
        var invoked = false
        Result.Loading.onSuccess<Unit> { invoked = true }
        assertFalse(invoked)
    }

    @Test
    fun `onSuccess returns the same instance`() {
        val result: Result<String> = Result.Success("data")
        val returned = result.onSuccess { }
        assertEquals(result, returned)
    }

    // onError tests

    @Test
    fun `onError invokes action for Error`() {
        var invoked = false
        Result.Error(RuntimeException()).onError { invoked = true }
        assertTrue(invoked)
    }

    @Test
    fun `onError passes correct exception to action`() {
        val exception = IllegalStateException("oops")
        var received: Throwable? = null
        Result.Error(exception).onError { received = it }
        assertEquals(exception, received)
    }

    @Test
    fun `onError does not invoke action for Success`() {
        var invoked = false
        Result.Success("data").onError { invoked = true }
        assertFalse(invoked)
    }

    @Test
    fun `onError does not invoke action for Loading`() {
        var invoked = false
        Result.Loading.onError { invoked = true }
        assertFalse(invoked)
    }

    @Test
    fun `onError returns the same instance`() {
        val result: Result<String> = Result.Error(RuntimeException())
        val returned = result.onError { }
        assertEquals(result, returned)
    }

    // chaining tests

    @Test
    fun `onSuccess and onError can be chained`() {
        var successInvoked = false
        var errorInvoked = false
        Result.Success("ok")
            .onSuccess { successInvoked = true }
            .onError { errorInvoked = true }
        assertTrue(successInvoked)
        assertFalse(errorInvoked)
    }
}
