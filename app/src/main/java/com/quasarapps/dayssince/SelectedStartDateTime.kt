package com.quasarapps.dayssince

import android.content.Context
import java.time.Clock
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
     * Fallbacks (used only on fresh install before the user picks anything):
     * - date: today
     * - time: current time (truncated to the minute) so the app shows "0d 0h 0m" on first launch
     *
     * @param clock injectable for testing; defaults to the system clock.
     */
    fun load(context: Context, clock: Clock = Clock.systemDefaultZone()): Value {
        val prefs = Prefs.get(context)

        val date = prefs.getString(PREF_SELECTED_DATE, null)
            ?.runCatching(LocalDate::parse)
            ?.getOrNull() ?: LocalDate.now(clock)

        val time = prefs.getString(PREF_SELECTED_TIME, null)
            ?.runCatching(LocalTime::parse)
            ?.getOrNull() ?: LocalTime.now(clock).withSecond(0).withNano(0)

        return Value(date = date, time = time)
    }

    fun persistDate(context: Context, date: LocalDate) {
        Prefs.get(context).edit().putString(PREF_SELECTED_DATE, date.toString()).apply()
    }

    fun persistTime(context: Context, time: LocalTime) {
        Prefs.get(context).edit().putString(PREF_SELECTED_TIME, time.toString()).apply()
    }
}

