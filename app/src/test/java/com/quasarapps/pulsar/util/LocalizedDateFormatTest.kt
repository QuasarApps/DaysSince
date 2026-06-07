package com.quasarapps.pulsar.util

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

/**
 * Unit tests for [LocalizedDateFormat]. Exact punctuation and word order come from the JDK's CLDR
 * data and shift between releases, so these assert the locale-sensitive *content* (month name, year)
 * and that different locales actually render differently — not a brittle exact string.
 */
class LocalizedDateFormatTest {

    @Test
    fun formatLongDate_english_includesMonthNameAndYear() {
        val date = LocalDate.of(2026, 1, 1)
        val text = LocalizedDateFormat.formatLongDate(date, Locale.ENGLISH)
        assertTrue("expected English month name in \"$text\"", text.contains("January"))
        assertTrue("expected year in \"$text\"", text.contains("2026"))
    }

    @Test
    fun formatLongDate_french_usesFrenchMonthName() {
        val date = LocalDate.of(2026, 1, 1)
        val text = LocalizedDateFormat.formatLongDate(date, Locale.FRENCH).lowercase(Locale.ROOT)
        assertTrue("expected French month name in \"$text\"", text.contains("janvier"))
        assertTrue("expected year in \"$text\"", text.contains("2026"))
    }

    @Test
    fun formatLongDate_differsAcrossLocales() {
        val date = LocalDate.of(2025, 6, 15)
        val english = LocalizedDateFormat.formatLongDate(date, Locale.ENGLISH)
        val french = LocalizedDateFormat.formatLongDate(date, Locale.FRENCH)
        assertNotEquals(english, french)
    }
}
