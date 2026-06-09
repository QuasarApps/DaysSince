package com.quasarapps.pulsar

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object ElapsedTime {

    /** Elapsed time, broken down. [seconds] is only set by [sincePickedDhms]; [sincePickedDhm] leaves it 0. */
    data class ElapsedDhm(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long = 0,
    )

    /** Whole days since the picked date/time. Delegates to [sincePickedDhm] (the single source of truth). */
    fun sincePicked(
        pickedDate: LocalDate,
        pickedTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return sincePickedDhm(pickedDate, pickedTime, clock = clock, zoneId = zoneId).days
    }

    /**
     * Elapsed days/hours/minutes since the picked date/time. Clamps to zero if the start is in the
     * future, uses whole minutes (stable widget rendering), and uses [ZonedDateTime] so DST
     * transitions are handled rather than assuming 24-hour days.
     */
    fun sincePickedDhm(
        pickedDate: LocalDate,
        pickedTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ElapsedDhm {
        val start = ZonedDateTime.of(pickedDate, pickedTime, zoneId)
        val now = ZonedDateTime.now(clock.withZone(zoneId))

        val totalMinutes = ChronoUnit.MINUTES.between(start, now)
        if (totalMinutes <= 0) return ElapsedDhm(days = 0, hours = 0, minutes = 0)

        val days = totalMinutes / (60L * 24L)
        val hours = (totalMinutes % (60L * 24L)) / 60L
        val minutes = totalMinutes % 60L

        return ElapsedDhm(days = days, hours = hours, minutes = minutes)
    }

    /**
     * Like [sincePickedDhm] but to the second, for the live detail screen (so the seconds tile ticks).
     * Same DST-correct, future-clamped behaviour; d/h/m match [sincePickedDhm] for the same instant.
     */
    fun sincePickedDhms(
        pickedDate: LocalDate,
        pickedTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ElapsedDhm {
        val start = ZonedDateTime.of(pickedDate, pickedTime, zoneId)
        val now = ZonedDateTime.now(clock.withZone(zoneId))

        val totalSeconds = ChronoUnit.SECONDS.between(start, now)
        if (totalSeconds <= 0) return ElapsedDhm(days = 0, hours = 0, minutes = 0, seconds = 0)

        val days = totalSeconds / (60L * 60L * 24L)
        val hours = (totalSeconds % (60L * 60L * 24L)) / (60L * 60L)
        val minutes = (totalSeconds % (60L * 60L)) / 60L
        val seconds = totalSeconds % 60L

        return ElapsedDhm(days = days, hours = hours, minutes = minutes, seconds = seconds)
    }

    /**
     * Whether a milestone reads as a "new beginning": 0 elapsed [days] and a start not in the future
     * (a later-today time clamps days to 0 but isn't a genuine new beginning). [days] is passed in so
     * callers reuse their live count; the future check uses the same [ZonedDateTime] model as
     * [sincePickedDhm], keeping a DST-gap local time consistent with the day count.
     */
    fun isNewBeginning(
        days: Long,
        date: LocalDate,
        time: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val start = ZonedDateTime.of(date, time, zoneId)
        val now = ZonedDateTime.now(clock.withZone(zoneId))
        return days == 0L && !start.isAfter(now)
    }
}
