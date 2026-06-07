package com.quasarapps.pulsar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // ---- accent persistence: stable key + legacy index compatibility ----

    @Test
    fun encode_writesStableAccentKey_notTheListIndex() {
        // Derive the expected key from the registry so this stays correct if the palette is
        // reordered — hardcoding "index 3 == rose" would defeat the purpose of stable keys.
        val index = 3
        val expectedKey = AccentKeys.keyForIndex(index)
        val json = MilestoneJson.encode(
            listOf(Milestone("a", "T", LocalDate.of(2025, 1, 1), LocalTime.of(8, 0), accent = index)),
        )

        assertTrue("expected stable key in JSON", json.contains("\"accent\":\"$expectedKey\""))
        // The bare numeric index must NOT be serialized.
        assertFalse("raw index must not be serialized", json.contains("\"accent\":$index"))
    }

    @Test
    fun decode_stableKey_resolvesToItsCurrentIndex() {
        // A key maps to whatever index currently holds it, so reordering won't recolor milestones.
        val key = "slate"
        val json = """[{"id":"x","title":"T","date":"2025-01-01","time":"08:00","accent":"$key"}]"""

        assertEquals(AccentKeys.indexForKey(key), MilestoneJson.decode(json).single().accent)
    }

    @Test
    fun decode_legacyIntegerAccent_isPreserved() {
        // Pre-key data stored the raw index as a number; it must still load unchanged.
        val json = """[{"id":"x","title":"T","date":"2025-01-01","time":"08:00","accent":5}]"""

        assertEquals(5, MilestoneJson.decode(json).single().accent)
    }

    @Test
    fun decode_unknownAccentKey_fallsBackToDefault() {
        val json = """[{"id":"x","title":"T","date":"2025-01-01","time":"08:00","accent":"removed"}]"""

        assertEquals(AccentKeys.DEFAULT_INDEX, MilestoneJson.decode(json).single().accent)
    }

    @Test
    fun encodeThenDecode_roundTripsAccentThroughItsKey() {
        val list = AccentKeys.ordered.indices.map {
            Milestone("id$it", "T$it", LocalDate.of(2025, 1, 1), LocalTime.of(8, 0), accent = it, createdAt = it.toLong())
        }

        val decoded = MilestoneJson.decode(MilestoneJson.encode(list))

        assertEquals(list.map { it.accent }, decoded.map { it.accent })
    }
}
