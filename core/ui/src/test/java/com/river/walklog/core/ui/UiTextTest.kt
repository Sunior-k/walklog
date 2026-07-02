package com.river.walklog.core.ui

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals

class UiTextTest {

    private val context: Context = mockk()

    // DynamicString

    @Test
    fun `DynamicString asString returns its value`() {
        assertEquals("hello", UiText.DynamicString("hello").asString(context))
    }

    @Test
    fun `DynamicString asString with empty string`() {
        assertEquals("", UiText.DynamicString("").asString(context))
    }

    @Test
    fun `DynamicString asString does not call context`() {
        UiText.DynamicString("hello").asString(context)
        verify(exactly = 0) { context.getString(any<Int>()) }
    }

    // StringRes

    @Test
    fun `StringRes without args delegates to context getString`() {
        every { context.getString(42) } returns "mocked"
        assertEquals("mocked", UiText.StringRes(42).asString(context))
        verify(exactly = 1) { context.getString(42) }
    }

    @Test
    fun `StringRes with args delegates to context getString with args`() {
        every { context.getString(42, "arg1") } returns "formatted"
        assertEquals("formatted", UiText.StringRes(42, listOf("arg1")).asString(context))
    }

    @Test
    fun `StringRes with multiple args passes all args`() {
        every { context.getString(7, "a", 3) } returns "a3"
        assertEquals("a3", UiText.StringRes(7, listOf("a", 3)).asString(context))
    }

    // PluralStringRes

    @Test
    fun `PluralStringRes without args calls getQuantityString`() {
        val resources: Resources = mockk()
        every { context.resources } returns resources
        every { resources.getQuantityString(42, 3) } returns "3 items"
        assertEquals("3 items", UiText.PluralStringRes(42, 3).asString(context))
    }

    @Test
    fun `PluralStringRes with args calls getQuantityString with args`() {
        val resources: Resources = mockk()
        every { context.resources } returns resources
        every { resources.getQuantityString(42, 2, 2) } returns "2 items"
        assertEquals("2 items", UiText.PluralStringRes(42, 2, listOf(2)).asString(context))
    }

    @Test
    fun `PluralStringRes count is forwarded correctly`() {
        val resources: Resources = mockk()
        every { context.resources } returns resources
        every { resources.getQuantityString(10, 1) } returns "singular"
        every { resources.getQuantityString(10, 5) } returns "plural"
        assertEquals("singular", UiText.PluralStringRes(10, 1).asString(context))
        assertEquals("plural", UiText.PluralStringRes(10, 5).asString(context))
    }
}
