package com.quasarapps.dayssince

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

/**
 * Shared logic for reading the user-selected start date/time.
 *
 * Single source of truth for:
 * - preference keys
 * - parsing
 * - safe fallbacks
 */
object SelectedStartDateTime {

    const val PREF_SELECTED_DATE = "selected_date"
    const val PREF_SELECTED_TIME = "selected_time"

    data class Value(
        val date: LocalDate,
        val time: LocalTime
    )

    /**
     * Loads the selected date/time from [Prefs].
     *
     * Fallbacks:
     * - date: today
     * - time: midnight
     */
    fun load(context: Context): Value {
        val prefs = Prefs.get(context)

        val date = prefs.getString(PREF_SELECTED_DATE, null)
            ?.runCatching(LocalDate::parse)
            ?.getOrNull() ?: LocalDate.now()

        val time = prefs.getString(PREF_SELECTED_TIME, null)
            ?.runCatching(LocalTime::parse)
            ?.getOrNull() ?: LocalTime.MIDNIGHT

        return Value(date = date, time = time)
    }

    /** True only if the legacy single-counter prefs were actually saved. Gates one-time migration. */
    fun hasStored(context: Context): Boolean {
        val prefs = Prefs.get(context)
        return prefs.contains(PREF_SELECTED_DATE) || prefs.contains(PREF_SELECTED_TIME)
    }

    fun persistDate(context: Context, date: LocalDate) {
        Prefs.get(context).edit().putString(PREF_SELECTED_DATE, date.toString()).apply()
    }

    fun persistTime(context: Context, time: LocalTime) {
        Prefs.get(context).edit().putString(PREF_SELECTED_TIME, time.toString()).apply()
    }
}

