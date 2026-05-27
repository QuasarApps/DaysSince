package com.quasarapps.dayssince.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class EnglishDateFormatTest {

    // ── ordinalDay ────────────────────────────────────────────────────────────

    @Test fun ordinalDay_1_isSt() = assertEquals("1st", EnglishDateFormat.ordinalDay(1))
    @Test fun ordinalDay_2_isNd() = assertEquals("2nd", EnglishDateFormat.ordinalDay(2))
    @Test fun ordinalDay_3_isRd() = assertEquals("3rd", EnglishDateFormat.ordinalDay(3))
    @Test fun ordinalDay_4_isTh() = assertEquals("4th", EnglishDateFormat.ordinalDay(4))
    @Test fun ordinalDay_10_isTh() = assertEquals("10th", EnglishDateFormat.ordinalDay(10))

    // 11, 12, 13 are the classic exceptions (not 11st / 12nd / 13rd)
    @Test fun ordinalDay_11_isTh() = assertEquals("11th", EnglishDateFormat.ordinalDay(11))
    @Test fun ordinalDay_12_isTh() = assertEquals("12th", EnglishDateFormat.ordinalDay(12))
    @Test fun ordinalDay_13_isTh() = assertEquals("13th", EnglishDateFormat.ordinalDay(13))

    @Test fun ordinalDay_21_isSt() = assertEquals("21st", EnglishDateFormat.ordinalDay(21))
    @Test fun ordinalDay_22_isNd() = assertEquals("22nd", EnglishDateFormat.ordinalDay(22))
    @Test fun ordinalDay_23_isRd() = assertEquals("23rd", EnglishDateFormat.ordinalDay(23))
    @Test fun ordinalDay_24_isTh() = assertEquals("24th", EnglishDateFormat.ordinalDay(24))
    @Test fun ordinalDay_31_isSt() = assertEquals("31st", EnglishDateFormat.ordinalDay(31))

    // ── formatOrdinalDate ─────────────────────────────────────────────────────

    @Test
    fun formatOrdinalDate_jan1_formatsCorrectly() {
        val date = LocalDate.of(2026, 1, 1)
        assertEquals("1st of January 2026", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_feb11_formatsCorrectly() {
        val date = LocalDate.of(2026, 2, 11)
        // 11 is the "th" exception
        assertEquals("11th of February 2026", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_mar22_formatsCorrectly() {
        val date = LocalDate.of(2025, 3, 22)
        assertEquals("22nd of March 2025", EnglishDateFormat.formatOrdinalDate(date))
    }

    @Test
    fun formatOrdinalDate_dec31_formatsCorrectly() {
        val date = LocalDate.of(2024, 12, 31)
        assertEquals("31st of December 2024", EnglishDateFormat.formatOrdinalDate(date))
    }
}
