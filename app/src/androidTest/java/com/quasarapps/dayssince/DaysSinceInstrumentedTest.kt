package com.quasarapps.dayssince

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * On-device tests for [DaysSince], the elapsed-time core that every screen and widget renders from.
 *
 * A fixed [Clock] and explicit [ZoneId] pin "now" so the assertions are deterministic instead of
 * depending on when the suite runs. This duplicates the JVM unit coverage on purpose: running the
 * same arithmetic against the device's real `java.time` implementation guards against
 * desugaring / platform differences the host JVM wouldn't surface.
 */
@RunWith(AndroidJUnit4::class)
class DaysSinceInstrumentedTest {

    private val utc = ZoneId.of("UTC")

    private fun clockAt(dateTime: LocalDateTime, zone: ZoneId = utc): Clock =
        Clock.fixed(dateTime.atZone(zone).toInstant(), zone)

    @Test
    fun sincePickedDhm_breaksElapsedIntoDaysHoursMinutes() {
        val picked = LocalDate.of(2025, 1, 1)
        val pickedTime = LocalTime.of(0, 0)
        val now = LocalDateTime.of(2025, 1, 3, 5, 30)

        val dhm = DaysSince.sincePickedDhm(picked, pickedTime, clockAt(now), utc)

        assertEquals(2, dhm.days)
        assertEquals(5, dhm.hours)
        assertEquals(30, dhm.minutes)
    }

    @Test
    fun sincePicked_returnsWholeDaysOnly() {
        val days = DaysSince.sincePicked(
            LocalDate.of(2025, 1, 1),
            LocalTime.of(0, 0),
            clockAt(LocalDateTime.of(2025, 1, 3, 5, 30)),
            utc,
        )

        assertEquals(2, days)
    }

    @Test
    fun secondsAreIgnored_minutesAreFloored() {
        // 0 days, 0 hours, 0 minutes + 59 seconds -> still 0 minutes.
        val now = LocalDateTime.of(2025, 1, 1, 0, 0, 59)

        val dhm = DaysSince.sincePickedDhm(
            LocalDate.of(2025, 1, 1), LocalTime.of(0, 0), clockAt(now), utc,
        )

        assertEquals(DaysSince.ElapsedDhm(0, 0, 0), dhm)
    }

    @Test
    fun futureTimestamp_clampsToZero() {
        val now = LocalDateTime.of(2025, 1, 1, 12, 0)

        val dhm = DaysSince.sincePickedDhm(
            LocalDate.of(2030, 1, 1), LocalTime.of(0, 0), clockAt(now), utc,
        )

        assertEquals(DaysSince.ElapsedDhm(0, 0, 0), dhm)
    }

    @Test
    fun exactlyNow_isZero() {
        val now = LocalDateTime.of(2025, 6, 15, 9, 30)

        val dhm = DaysSince.sincePickedDhm(
            now.toLocalDate(), now.toLocalTime(), clockAt(now), utc,
        )

        assertEquals(DaysSince.ElapsedDhm(0, 0, 0), dhm)
    }

    @Test
    fun springForwardDst_isAccountedFor_notAssumed24hDays() {
        // Europe/London springs forward on 2023-03-26 (01:00 -> 02:00), so the wall-clock span of
        // 48h is only 47h of real elapsed time. A naive 24h-per-day calc would say "2 days"; the
        // ZonedDateTime-based implementation must report 1d 23h 0m.
        val london = ZoneId.of("Europe/London")
        val picked = LocalDate.of(2023, 3, 25)
        val pickedTime = LocalTime.of(12, 0)
        val now = LocalDateTime.of(2023, 3, 27, 12, 0)

        val dhm = DaysSince.sincePickedDhm(picked, pickedTime, clockAt(now, london), london)

        assertEquals(1, dhm.days)
        assertEquals(23, dhm.hours)
        assertEquals(0, dhm.minutes)
    }
}
