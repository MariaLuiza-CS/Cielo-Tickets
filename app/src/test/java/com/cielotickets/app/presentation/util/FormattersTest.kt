package com.cielotickets.app.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `toBrazilianCurrency with Int should format correctly`() {
        assertEquals("R$ 120,00", 12000.toBrazilianCurrency())
        assertEquals("R$ 0,00", 0.toBrazilianCurrency())
        assertEquals("R$ 1,50", 150.toBrazilianCurrency())
    }

    @Test
    fun `toBrazilianCurrency with Double should format correctly`() {
        assertEquals("R$ 120,00", 12000.0.toBrazilianCurrency())
        assertEquals("R$ 0,00", 0.0.toBrazilianCurrency())
    }

    @Test
    fun `toBrazilianDateTime should format timestamp correctly`() {
        val timestamp = 1787490000000L
        val result = timestamp.toBrazilianDateTime()
        assertEquals("23/08/2026 10:00", result)
    }

    @Test
    fun `toFormattedEventDate should format ISO string correctly`() {
        val isoDate = "2026-10-15T22:00:00"
        val expected = "15 de outubro de 2026, às 22h"
        assertEquals(expected, isoDate.toFormattedEventDate())
    }

    @Test
    fun `toFormattedEventDate should return original string if malformed`() {
        val invalidDate = "invalid-date"
        assertEquals(invalidDate, invalidDate.toFormattedEventDate())
    }
}
