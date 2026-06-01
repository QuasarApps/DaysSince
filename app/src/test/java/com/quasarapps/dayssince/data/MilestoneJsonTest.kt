package com.quasarapps.dayssince.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class MilestoneJsonTest {

    @Test
    fun encodeThenDecode_roundTrips() {
        val list = listOf(
            Milestone(
                id = "a",
                title = "Sober",
                date = LocalDate.of(2025, 1, 2),
                time = LocalTime.of(9, 30),
                accent = 3,
                createdAt = 100L,
            ),
            Milestone(
                id = "b",
                title = "Gym",
                date = LocalDate.of(2024, 12, 31),
                time = LocalTime.MIDNIGHT,
                accent = 0,
                createdAt = 200L,
            ),
        )

        val decoded = MilestoneJson.decode(MilestoneJson.encode(list))

        assertEquals(list, decoded)
    }

    @Test
    fun decode_nullOrBlank_returnsEmpty() {
        assertTrue(MilestoneJson.decode(null).isEmpty())
        assertTrue(MilestoneJson.decode("").isEmpty())
        assertTrue(MilestoneJson.decode("   ").isEmpty())
    }

    @Test
    fun decode_malformedJson_returnsEmpty() {
        assertTrue(MilestoneJson.decode("not json").isEmpty())
        // a JSON object (not an array) should be rejected rather than crash
        assertTrue(MilestoneJson.decode("{\"oops\":true}").isEmpty())
    }

    @Test
    fun decode_appliesDefaultsForMissingOptionalFields() {
        val json = """[{"id":"x","title":"T","date":"2026-01-01","time":"08:00"}]"""

        val decoded = MilestoneJson.decode(json)

        assertEquals(1, decoded.size)
        assertEquals("x", decoded[0].id)
        assertEquals(LocalDate.of(2026, 1, 1), decoded[0].date)
        assertEquals(LocalTime.of(8, 0), decoded[0].time)
        assertEquals(0, decoded[0].accent)
    }

    @Test
    fun decode_badDateOrTime_fallsBackInsteadOfThrowing() {
        val json = """[{"id":"x","title":"T","date":"nope","time":"nope","accent":2,"createdAt":5}]"""

        val decoded = MilestoneJson.decode(json)

        assertEquals(1, decoded.size)
        assertEquals(LocalTime.MIDNIGHT, decoded[0].time)
        assertEquals(2, decoded[0].accent)
    }
}
