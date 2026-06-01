package com.quasarapps.dayssince.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * On-device coverage for [EnglishDateFormat], the formatter shared by the cards, detail hero and
 * widget content descriptions. Ordinal suffixes have several edge cases (the 11/12/13 exception),
 * so they're pinned here against the device's locale data.
 */
@RunWith(AndroidJUnit4::class)
class EnglishDateFormatInstrumentedTest {

    @Test
    fun ordinalDay_standardSuffixes() {
        assertEquals("1st", EnglishDateFormat.ordinalDay(1))
        assertEquals("2nd", EnglishDateFormat.ordinalDay(2))
        assertEquals("3rd", EnglishDateFormat.ordinalDay(3))
        assertEquals("4th", EnglishDateFormat.ordinalDay(4))
        assertEquals("21st", EnglishDateFormat.ordinalDay(21))
        assertEquals("22nd", EnglishDateFormat.ordinalDay(22))
        assertEquals("23rd", EnglishDateFormat.ordinalDay(23))
        assertEquals("31st", EnglishDateFormat.ordinalDay(31))
    }

    @Test
    fun ordinalDay_teensAlwaysUseTh() {
        assertEquals("11th", EnglishDateFormat.ordinalDay(11))
        assertEquals("12th", EnglishDateFormat.ordinalDay(12))
        assertEquals("13th", EnglishDateFormat.ordinalDay(13))
    }

    @Test
    fun formatOrdinalDate_rendersDayMonthYear() {
        assertEquals(
            "1st of January 2026",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2026, 1, 1)),
        )
        assertEquals(
            "15th of June 2025",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2025, 6, 15)),
        )
        assertEquals(
            "23rd of December 2024",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2024, 12, 23)),
        )
    }
}
