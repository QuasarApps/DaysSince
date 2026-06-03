package com.quasarapps.dayssince.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * On-device coverage for [MilestoneJson]. Runs against the platform's real `org.json`
 * implementation (the host JVM only has the stubbed one), so it verifies the encode/decode cycle
 * the persistence layer relies on actually behaves on a device.
 */
@RunWith(AndroidJUnit4::class)
class MilestoneJsonInstrumentedTest {

    @Test
    fun encodeThenDecode_roundTripsAllFields() {
        val original = listOf(
            Milestone("a", "Sober", LocalDate.of(2025, 6, 15), LocalTime.of(9, 30), accent = 3, createdAt = 100L),
            Milestone("b", "Gym", LocalDate.of(2024, 1, 1), LocalTime.of(0, 0), accent = 0, createdAt = 200L),
        )

        val decoded = MilestoneJson.decode(MilestoneJson.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun decode_nullOrBlank_returnsEmptyList() {
        assertTrue(MilestoneJson.decode(null).isEmpty())
        assertTrue(MilestoneJson.decode("").isEmpty())
        assertTrue(MilestoneJson.decode("   ").isEmpty())
    }

    @Test
    fun decode_malformedJson_returnsEmptyListInsteadOfThrowing() {
        assertTrue(MilestoneJson.decode("not json at all").isEmpty())
        assertTrue(MilestoneJson.decode("{\"oops\":true}").isEmpty())
    }

    @Test
    fun decode_partialEntry_fallsBackToSensibleDefaults() {
        // Missing date/time should fall back (today / midnight) rather than dropping the entry.
        val json = """[{"id":"x","title":"Half","accent":2}]"""

        // Capture "today" right before decoding so the assertion can't flake if the test happens to
        // straddle local midnight between this read and the decoder's own LocalDate.now() fallback.
        val today = LocalDate.now()
        val decoded = MilestoneJson.decode(json)

        assertEquals(1, decoded.size)
        val m = decoded.single()
        assertEquals("x", m.id)
        assertEquals("Half", m.title)
        assertEquals(2, m.accent)
        assertEquals(LocalTime.MIDNIGHT, m.time)
        assertEquals(today, m.date)
    }

    @Test
    fun decode_blankId_isReplacedWithGeneratedId() {
        val json = """[{"id":"","title":"NoId","date":"2025-01-01","time":"08:00"}]"""

        val decoded = MilestoneJson.decode(json)

        assertTrue(decoded.single().id.isNotBlank())
    }

    @Test
    fun encode_writesStableAccentKey_andDecodeResolvesIt() {
        // Derive the expected key from the registry so the test survives a palette reorder.
        val index = 7
        val expectedKey = AccentKeys.keyForIndex(index)
        val json = MilestoneJson.encode(
            listOf(Milestone("a", "T", LocalDate.of(2025, 1, 1), LocalTime.of(8, 0), accent = index)),
        )

        assertTrue(json.contains("\"accent\":\"$expectedKey\""))
        assertFalse("raw index must not be serialized", json.contains("\"accent\":$index"))
        assertEquals(index, MilestoneJson.decode(json).single().accent)
    }

    @Test
    fun decode_legacyIntegerAccent_andUnknownKey() {
        // Pre-key data stored the raw index as a number; still loads unchanged.
        assertEquals(
            5,
            MilestoneJson.decode(
                """[{"id":"x","title":"T","date":"2025-01-01","time":"08:00","accent":5}]""",
            ).single().accent,
        )
        // An accent key removed in a later release falls back to the default accent.
        assertEquals(
            AccentKeys.DEFAULT_INDEX,
            MilestoneJson.decode(
                """[{"id":"x","title":"T","date":"2025-01-01","time":"08:00","accent":"removed"}]""",
            ).single().accent,
        )
    }
}
