package com.quasarapps.dayssince

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DaysSince {

    /**
     * Whole days since the user-picked date & time in the device time zone.
     *
     * - Returns 0 if the picked timestamp is in the future.
     * - Uses floor whole days (i.e., <24h => 0).
     */
    fun sincePicked(
        pickedDate: LocalDate,
        pickedTime: LocalTime,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val start = LocalDateTime.of(pickedDate, pickedTime)
        val now = LocalDateTime.now(clock.withZone(zoneId))
        val days = ChronoUnit.DAYS.between(start, now)
        return if (days < 0) 0 else days
    }
}
