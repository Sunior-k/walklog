package com.river.walklog.core.ui

import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NumberFormatExtTest {

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `0 formats to 0`() {
        assertEquals("0", 0.withComma())
    }

    @Test
    fun `three digit number has no comma`() {
        assertEquals("999", 999.withComma())
    }

    @Test
    fun `1000 formats with comma`() {
        assertEquals("1,000", 1_000.withComma())
    }

    @Test
    fun `10000 formats with comma`() {
        assertEquals("10,000", 10_000.withComma())
    }

    @Test
    fun `1000000 formats with two commas`() {
        assertEquals("1,000,000", 1_000_000.withComma())
    }

    @Test
    fun `withComma result differs from plain toString for 4-digit number`() {
        assertNotEquals(1_000.toString(), 1_000.withComma())
    }

    @Test
    fun `daily step goal 6000 formats correctly`() {
        assertEquals("6,000", 6_000.withComma())
    }
}
