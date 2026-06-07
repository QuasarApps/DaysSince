package com.quasarapps.pulsar

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object ElapsedTime {

    /**
     * Elapsed time, broken down. [seconds] is only populated by [sincePickedDhms] — the minute-
     * resolution [sincePickedDhm] (used by widgets) leaves it 0.
     */
    data class ElapsedDhm(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long = 0,
    )

    /**
     * Whole days since the user-picked date & time in the device time zone.
     *
     * This delegates to [sincePickedDhm] so the DHM breakdown is the single source of truth.
     */
    fun sincePicked(
        pickedDate: LocalDate,
        pickedTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return sincePickedDhm(pickedDate, pickedTime, clock = clock, zoneId = zoneId).days
    }

    /**
     * Elapsed time since the picked date/time broken down into days/hours/minutes.
     *
     * - Clamps to 0d 0h 0m if the picked timestamp is in the future.
     * - Uses whole minutes (seconds are ignored), for stable widget rendering.
     * - Uses [ZonedDateTime] so DST transitions (spring-forward / fall-back) are accounted for
     *   correctly rather than always assuming 24-hour days.
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
     * Elapsed time broken down to the second, for the live detail screen. Same DST-correct,
     * future-clamped behaviour as [sincePickedDhm] but at second resolution (so the detail's seconds
     * tile ticks). Days/hours/minutes match [sincePickedDhm] for the same instant.
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
}
