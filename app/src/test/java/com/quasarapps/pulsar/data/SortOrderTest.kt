package com.quasarapps.pulsar.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit tests for [SortOrder.sort] — pure list ordering, no clock or Android dependency.
 *
 * The three fixtures are arranged so each order yields a distinct result:
 * a "Mango" 2020 created@100, b "Zebra" 2024 created@300, c "Apple" 2022 created@200.
 */
class SortOrderTest {

    private fun m(id: String, title: String, year: Int, createdAt: Long) =
        Milestone(id, title, LocalDate.of(year, 1, 1), LocalTime.NOON, createdAt = createdAt)

    private val a = m("a", "Mango", 2020, createdAt = 100)
    private val b = m("b", "Zebra", 2024, createdAt = 300)
    private val c = m("c", "Apple", 2022, createdAt = 200)
    private val all = listOf(b, a, c) // deliberately unsorted input

    @Test
    fun recentlyAdded_isCreatedAtDescending() {
        assertEquals(listOf("b", "c", "a"), SortOrder.RECENTLY_ADDED.sort(all).map { it.id })
    }

    @Test
    fun mostDays_putsTheEarliestStartFirst() {
        // Earliest start = most elapsed: 2020 (a) < 2022 (c) < 2024 (b).
        assertEquals(listOf("a", "c", "b"), SortOrder.MOST_DAYS.sort(all).map { it.id })
    }

    @Test
    fun alphabetical_isCaseInsensitiveByTitle() {
        // Apple (c), Mango (a), Zebra (b) — case-insensitive.
        assertEquals(listOf("c", "a", "b"), SortOrder.ALPHABETICAL.sort(all).map { it.id })
    }

    @Test
    fun fromStorage_unknownOrMissingFallsBackToRecentlyAdded() {
        assertEquals(SortOrder.RECENTLY_ADDED, SortOrder.fromStorage(null))
        assertEquals(SortOrder.RECENTLY_ADDED, SortOrder.fromStorage("not-an-order"))
        assertEquals(SortOrder.ALPHABETICAL, SortOrder.fromStorage("ALPHABETICAL"))
    }
}
