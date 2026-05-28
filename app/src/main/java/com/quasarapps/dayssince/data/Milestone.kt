package com.quasarapps.dayssince.data

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * A single "days since" counter.
 *
 * [accent] indexes into `MilestoneAccents` in ui.theme; 0 means "Dynamic" (Material You).
 */
data class Milestone(
    val id: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val accent: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}
