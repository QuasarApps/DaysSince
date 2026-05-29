package com.quasarapps.dayssince.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

/**
 * Unit tests for [EnglishDateFormat]. The interesting logic is the ordinal-suffix calculation —
 * specifically the 11/12/13 exception, which is a classic off-by-one trap.
 */
class EnglishDateFormatTest {

    // ---- ordinalDay ----

    @Test
    fun ordinalDay_singleDigits() {
        assertEquals("1st", EnglishDateFormat.ordinalDay(1))
        assertEquals("2nd", EnglishDateFormat.ordinalDay(2))
        assertEquals("3rd", EnglishDateFormat.ordinalDay(3))
        assertEquals("4th", EnglishDateFormat.ordinalDay(4))
        assertEquals("5th", EnglishDateFormat.ordinalDay(5))
        assertEquals("6th", EnglishDateFormat.ordinalDay(6))
        assertEquals("7th", EnglishDateFormat.ordinalDay(7))
        assertEquals("8th", EnglishDateFormat.ordinalDay(8))
        assertEquals("9th", EnglishDateFormat.ordinalDay(9))
    }

    @Test
    fun ordinalDay_teensAreAllTh_includingTheExceptions() {
        assertEquals("10th", EnglishDateFormat.ordinalDay(10))
        // The 11/12/13 exception is the whole point of the mod-100 branch.
        assertEquals("11th", EnglishDateFormat.ordinalDay(11))
        assertEquals("12th", EnglishDateFormat.ordinalDay(12))
        assertEquals("13th", EnglishDateFormat.ordinalDay(13))
        assertEquals("14th", EnglishDateFormat.ordinalDay(14))
        assertEquals("15th", EnglishDateFormat.ordinalDay(15))
        assertEquals("19th", EnglishDateFormat.ordinalDay(19))
    }

    @Test
    fun ordinalDay_twentiesUseTheNormalRule() {
        assertEquals("20th", EnglishDateFormat.ordinalDay(20))
        assertEquals("21st", EnglishDateFormat.ordinalDay(21))
        assertEquals("22nd", EnglishDateFormat.ordinalDay(22))
        assertEquals("23rd", EnglishDateFormat.ordinalDay(23))
        assertEquals("24th", EnglishDateFormat.ordinalDay(24))
    }

    @Test
    fun ordinalDay_thirtiesAndThirtyFirst() {
        assertEquals("30th", EnglishDateFormat.ordinalDay(30))
        assertEquals("31st", EnglishDateFormat.ordinalDay(31))
    }

    // ---- formatOrdinalDate ----

    @Test
    fun formatOrdinalDate_firstOfMonth() {
        val date = LocalDate.of(2026, 1, 1)
        assertEquals("1st of January 2026", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_secondOfMonth() {
        val date = LocalDate.of(2026, 2, 2)
        assertEquals("2nd of February 2026", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_thirdOfMonth() {
        val date = LocalDate.of(2026, 3, 3)
        assertEquals("3rd of March 2026", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_elevenTwelveThirteenAreTh() {
        assertEquals(
            "11th of November 2026",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2026, 11, 11)),
        )
        assertEquals(
            "12th of December 2026",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2026, 12, 12)),
        )
        assertEquals(
            "13th of December 2026",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2026, 12, 13)),
        )
    }

    @Test
    fun formatOrdinalDate_endOfMonth() {
        assertEquals(
            "31st of January 2026",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2026, 1, 31)),
        )
    }

    @Test
    fun formatOrdinalDate_leapDay() {
        assertEquals(
            "29th of February 2024",
            EnglishDateFormat.formatOrdinalDate(LocalDate.of(2024, 2, 29)),
        )
    }

    @Test
    fun formatOrdinalDate_respectsLocaleForMonthName_butNotForOrdinalSuffix() {
        // The day-suffix is English regardless (the method name says so), but the month name
        // comes from the supplied locale. Using lowercase for the comparison so the test is
        // stable across JDK locale-data versions that capitalize French month names differently.
        val date = LocalDate.of(2026, 1, 1)
        val french = EnglishDateFormat.formatOrdinalDate(date, Locale.FRENCH)
        assertEquals("1st of janvier 2026", french.lowercase(Locale.ROOT))
    }
}
