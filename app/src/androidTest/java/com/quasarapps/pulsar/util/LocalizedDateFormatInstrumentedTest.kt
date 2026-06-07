package com.quasarapps.pulsar.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.Locale

/**
 * On-device coverage for [LocalizedDateFormat], the formatter shared by the cards, detail hero and
 * widget content descriptions. Pins the formatter against Android's own CLDR data (which differs
 * from the host JVM's), asserting locale-sensitive content rather than an exact string.
 */
@RunWith(AndroidJUnit4::class)
class LocalizedDateFormatInstrumentedTest {

    @Test
    fun formatLongDate_english_includesMonthNameAndYear() {
        val text = LocalizedDateFormat.formatLongDate(LocalDate.of(2026, 1, 1), Locale.ENGLISH)
        assertTrue("expected English month name in \"$text\"", text.contains("January"))
        assertTrue("expected year in \"$text\"", text.contains("2026"))
    }

    @Test
    fun formatLongDate_differsAcrossLocales() {
        val date = LocalDate.of(2025, 6, 15)
        val english = LocalizedDateFormat.formatLongDate(date, Locale.ENGLISH)
        val spanish = LocalizedDateFormat.formatLongDate(date, Locale("es"))
        assertNotEquals(english, spanish)
    }
}
