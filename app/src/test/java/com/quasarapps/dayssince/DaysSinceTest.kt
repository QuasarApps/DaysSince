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
}
