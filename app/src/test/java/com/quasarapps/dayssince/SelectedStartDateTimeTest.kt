package com.quasarapps.dayssince

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class SelectedStartDateTimeTest {

    private val fixedInstant = Instant.parse("2026-03-15T14:37:52Z")
    private val utc = ZoneId.of("UTC")
    private val fixedClock = Clock.fixed(fixedInstant, utc)

    // Expected fallback values when using fixedClock: 2026-03-15 14:37 (seconds/nanos stripped)
    private val expectedFallbackDate = LocalDate.of(2026, 3, 15)
    private val expectedFallbackTime = LocalTime.of(14, 37)

    @Test
    fun load_missingPrefs_returnsFallbackMatchingClock() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val picked = SelectedStartDateTime.load(context, clock = fixedClock)

        assertEquals(expectedFallbackDate, picked.date)
        assertEquals(expectedFallbackTime, picked.time)
    }

    @Test
    fun load_invalidPrefs_returnsFallbackMatchingClock() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString(SelectedStartDateTime.PREF_SELECTED_DATE, "not-a-date")
            .putString(SelectedStartDateTime.PREF_SELECTED_TIME, "not-a-time")
            .commit()

        val picked = SelectedStartDateTime.load(context, clock = fixedClock)

        assertEquals(expectedFallbackDate, picked.date)
        assertEquals(expectedFallbackTime, picked.time)
    }

    @Test
    fun load_invalidTime_doesNotBreakValidDate() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString(SelectedStartDateTime.PREF_SELECTED_DATE, "2026-01-01")
            .putString(SelectedStartDateTime.PREF_SELECTED_TIME, "not-a-time")
            .commit()

        val picked = SelectedStartDateTime.load(context, clock = fixedClock)

        assertEquals(LocalDate.of(2026, 1, 1), picked.date)
        // Time falls back to clock-based value since stored value is invalid
        assertEquals(expectedFallbackTime, picked.time)
    }

    @Test
    fun load_fallbackTime_secondsAndNanosAreStripped() {
        // Verify the fallback time is truncated to the minute (no seconds/nanos)
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val picked = SelectedStartDateTime.load(context, clock = fixedClock)

        assertEquals(0, picked.time.second)
        assertEquals(0, picked.time.nano)
    }

    @Test
    fun persistAndLoad_roundTrips() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val date = LocalDate.of(2026, 1, 1)
        val time = LocalTime.of(12, 34)

        SelectedStartDateTime.persistDate(context, date)
        SelectedStartDateTime.persistTime(context, time)

        val picked = SelectedStartDateTime.load(context, clock = fixedClock)
        assertEquals(date, picked.date)
        assertEquals(time, picked.time)
    }
}
