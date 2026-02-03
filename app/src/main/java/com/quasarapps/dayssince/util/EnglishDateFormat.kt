package com.quasarapps.dayssince.util

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Small, composable-independent date formatting helpers.
 */
object EnglishDateFormat {

    /**
     * Formats a date in a simple, standard English style.
     *
     * Example: `1st of January 2026`
     */
    fun formatOrdinalDate(date: LocalDate, locale: Locale = Locale.ENGLISH): String {
        val monthName = date.month.getDisplayName(TextStyle.FULL, locale)
        return "${ordinalDay(date.dayOfMonth)} of $monthName ${date.year}"
    }

    /**
     * Returns an ordinal day number suffix (1st, 2nd, 3rd, 4th, ..., 11th, 12th, 13th, ...).
     */
    fun ordinalDay(day: Int): String {
        val mod100 = day % 100
        val suffix = if (mod100 in 11..13) {
            "th"
        } else {
            when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
        return "$day$suffix"
    }
}

