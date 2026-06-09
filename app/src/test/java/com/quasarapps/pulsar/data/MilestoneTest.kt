package com.quasarapps.pulsar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Unit tests for [Milestone] — specifically its [Milestone.newId] factory (used as the stable key
 * for persistence and widget bindings) and its defaulting.
 */
class MilestoneTest {

    @Test
    fun newId_isANonBlankUuid() {
        val id = Milestone.newId()

        assertTrue("id must not be blank", id.isNotBlank())
        // Round-trips through UUID.fromString -> it's a well-formed UUID, not just any string.
        assertEquals(id, UUID.fromString(id).toString())
    }

    @Test
    fun newId_isUniquePerCall() {
        val ids = List(1_000) { Milestone.newId() }

        assertEquals("every generated id should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun defaults_accentIsDynamicAndCreatedAtIsSet() {
        val before = System.currentTimeMillis()
        val milestone = Milestone(
            id = "a",
            title = "Birthday",
            date = LocalDate.of(2025, 1, 1),
            time = LocalTime.of(9, 0),
        )
        val after = System.currentTimeMillis()

        // accent defaults to 0 ("Dynamic").
        assertEquals(0, milestone.accent)
        // createdAt defaults to "now".
        assertTrue(milestone.createdAt in before..after)
    }
}
