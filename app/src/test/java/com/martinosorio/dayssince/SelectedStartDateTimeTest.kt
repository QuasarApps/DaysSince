package com.quasarapps.dayssince

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SelectedStartDateTimeTest {

    @Test
    fun load_missingPrefs_returnsFallbacks() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val picked = SelectedStartDateTime.load(context)

        // Date fallback is "today" (hard to assert exact date in unit tests without a clock);
        // but time fallback should be midnight.
        assertEquals(java.time.LocalTime.MIDNIGHT, picked.time)
    }

    @Test
    fun load_invalidPrefs_returnsFallbacks() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString(SelectedStartDateTime.PREF_SELECTED_DATE, "not-a-date")
            .putString(SelectedStartDateTime.PREF_SELECTED_TIME, "not-a-time")
            .commit()

        val picked = SelectedStartDateTime.load(context)

        assertEquals(java.time.LocalTime.MIDNIGHT, picked.time)
    }

    @Test
    fun load_invalidTime_doesNotBreakValidDate() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString(SelectedStartDateTime.PREF_SELECTED_DATE, "2026-01-01")
            .putString(SelectedStartDateTime.PREF_SELECTED_TIME, "not-a-time")
            .commit()

        val picked = SelectedStartDateTime.load(context)
        assertEquals(java.time.LocalDate.of(2026, 1, 1), picked.date)
        assertEquals(java.time.LocalTime.MIDNIGHT, picked.time)
    }

    @Test
    fun persistAndLoad_roundTrips() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val date = java.time.LocalDate.of(2026, 1, 1)
        val time = java.time.LocalTime.of(12, 34)

        SelectedStartDateTime.persistDate(context, date)
        SelectedStartDateTime.persistTime(context, time)

        val picked = SelectedStartDateTime.load(context)
        assertEquals(date, picked.date)
        assertEquals(time, picked.time)
    }
}
