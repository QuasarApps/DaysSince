package com.quasarapps.dayssince

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DaysSinceTest {

    private val utc = ZoneId.of("UTC")

    @Test
    fun sincePicked_sameInstant_returns0() {
        val pickedDate = LocalDate.of(2026, 1, 7)
        val pickedTime = LocalTime.of(0, 0)

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(0, days)
    }

    @Test
    fun sincePicked_oneSecondBefore24h_returns0() {
        val pickedDate = LocalDate.of(2026, 1, 6)
        val pickedTime = LocalTime.of(12, 0)

        val now = Instant.parse("2026-01-07T11:59:59Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(0, days)
    }

    @Test
    fun sincePicked_exactly24h_returns1() {
        val pickedDate = LocalDate.of(2026, 1, 6)
        val pickedTime = LocalTime.of(12, 0)

        val now = Instant.parse("2026-01-07T12:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(1, days)
    }

    @Test
    fun sincePicked_fortyEightHours_returns2() {
        val pickedDate = LocalDate.of(2026, 1, 5)
        val pickedTime = LocalTime.of(12, 0)

        val now = Instant.parse("2026-01-07T12:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(2, days)
    }

    @Test
    fun sincePicked_future_returns0() {
        val pickedDate = LocalDate.of(2026, 1, 8)
        val pickedTime = LocalTime.of(0, 0)

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(0, days)
    }

    @Test
    fun sincePicked_futureSameDayLaterTime_returns0() {
        val pickedDate = LocalDate.of(2026, 1, 7)
        val pickedTime = LocalTime.of(0, 1)

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(0, days)
    }

    @Test
    fun sincePicked_largeInterval_isComputedCorrectly() {
        val pickedDate = LocalDate.of(2020, 1, 1)
        val pickedTime = LocalTime.MIDNIGHT

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        // 2020-01-01 -> 2026-01-07
        assertEquals(2198, days)
    }

    @Test
    fun sincePicked_leapDayHandled_correctly() {
        val pickedDate = LocalDate.of(2024, 2, 29)
        val pickedTime = LocalTime.MIDNIGHT

        val now = Instant.parse("2024-03-01T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val days = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(1, days)
    }

    @Test
    fun sincePicked_zoneMatters_usesProvidedZoneId() {
        // Same instant, but local date/time differs depending on zone.
        val zoneKiritimati = ZoneId.of("Pacific/Kiritimati") // UTC+14

        val pickedDate = LocalDate.of(2026, 1, 7)
        val pickedTime = LocalTime.MIDNIGHT

        // This instant is 2026-01-07 00:00 in UTC, but 14:00 on 2026-01-07 in Kiritimati.
        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val daysUtc = DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = utc)
        val daysKiritimati =
            DaysSince.sincePicked(pickedDate, pickedTime, clock = clock, zoneId = zoneKiritimati)

        // Both should still be 0 because less than 24h since local midnight.
        assertEquals(0, daysUtc)
        assertEquals(0, daysKiritimati)
    }

    @Test
    fun sincePickedDhm_sameInstant_returnsZeros() {
        val pickedDate = LocalDate.of(2026, 1, 7)
        val pickedTime = LocalTime.of(0, 0)

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val dhm = DaysSince.sincePickedDhm(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(DaysSince.ElapsedDhm(days = 0, hours = 0, minutes = 0), dhm)
    }

    @Test
    fun sincePickedDhm_oneDayTwoHoursThreeMinutes_isComputedCorrectly() {
        val pickedDate = LocalDate.of(2026, 1, 6)
        val pickedTime = LocalTime.of(9, 0)

        // 1 day, 2 hours, 3 minutes later
        val now = Instant.parse("2026-01-07T11:03:00Z")
        val clock = Clock.fixed(now, utc)

        val dhm = DaysSince.sincePickedDhm(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(DaysSince.ElapsedDhm(days = 1, hours = 2, minutes = 3), dhm)
    }

    @Test
    fun sincePickedDhm_future_returnsZeros() {
        val pickedDate = LocalDate.of(2026, 1, 8)
        val pickedTime = LocalTime.of(0, 0)

        val now = Instant.parse("2026-01-07T00:00:00Z")
        val clock = Clock.fixed(now, utc)

        val dhm = DaysSince.sincePickedDhm(pickedDate, pickedTime, clock = clock, zoneId = utc)
        assertEquals(DaysSince.ElapsedDhm(days = 0, hours = 0, minutes = 0), dhm)
    }

    // DST tests — America/New_York (EST=UTC-5, EDT=UTC-4)

    @Test
    fun sincePickedDhm_springForward_23hDayCountedCorrectly() {
        // On 2025-03-09 clocks spring forward at 02:00 EST -> 03:00 EDT.
        // The wall-clock day is only 23 hours long.
        // picked: 2025-03-08 10:00 EST (= 2025-03-08T15:00Z)
        // now:    2025-03-09 10:00 EDT (= 2025-03-09T14:00Z)
        // Real elapsed: 23 h → 0 days, 23 hours, 0 minutes
        val ny = ZoneId.of("America/New_York")
        val picked = LocalDate.of(2025, 3, 8)
        val pickedTime = LocalTime.of(10, 0)

        val nowInstant = Instant.parse("2025-03-09T14:00:00Z")
        val clock = Clock.fixed(nowInstant, ny)

        val dhm = DaysSince.sincePickedDhm(picked, pickedTime, clock = clock, zoneId = ny)
        assertEquals(DaysSince.ElapsedDhm(days = 0, hours = 23, minutes = 0), dhm)
    }

    @Test
    fun sincePickedDhm_fallBack_25hDayCountedCorrectly() {
        // On 2025-11-02 clocks fall back at 02:00 EDT -> 01:00 EST.
        // The wall-clock day is 25 hours long.
        // picked: 2025-11-01 10:00 EDT (= 2025-11-01T14:00Z)
        // now:    2025-11-02 10:00 EST (= 2025-11-02T15:00Z)
        // Real elapsed: 25 h → 1 day, 1 hour, 0 minutes
        val ny = ZoneId.of("America/New_York")
        val picked = LocalDate.of(2025, 11, 1)
        val pickedTime = LocalTime.of(10, 0)

        val nowInstant = Instant.parse("2025-11-02T15:00:00Z")
        val clock = Clock.fixed(nowInstant, ny)

        val dhm = DaysSince.sincePickedDhm(picked, pickedTime, clock = clock, zoneId = ny)
        assertEquals(DaysSince.ElapsedDhm(days = 1, hours = 1, minutes = 0), dhm)
    }
}
