package com.quasarapps.dayssince

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object DaysSince {

    data class ElapsedDhm(
        val days: Long,
        val hours: Long,
        val minutes: Long
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
}
