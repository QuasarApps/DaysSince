package com.quasarapps.dayssince.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for the widget bindings (de)serialization in [MilestonesRepository].
 *
 * Robolectric is required because the implementation uses org.json, whose runtime is stubbed
 * out in plain JVM tests.
 */
@RunWith(RobolectricTestRunner::class)
class MilestonesRepositoryBindingsTest {

    @Test
    fun encodeThenDecode_roundTrips() {
        val bindings = mapOf(
            12 to WidgetBinding(milestoneId = "abc-123", transparent = false),
            15 to WidgetBinding(milestoneId = "def-456", transparent = true),
        )

        val decoded = MilestonesRepository.decodeBindings(
            MilestonesRepository.encodeBindings(bindings),
        )

        assertEquals(bindings, decoded)
    }

    @Test
    fun decode_legacyBareStringValues_upgradeToTransparentFalse() {
        // Pre-transparent format shipped by the merged redesign: a flat
        // appWidgetId -> milestoneId string map.
        val legacyJson = """{"12":"abc-123","15":"def-456"}"""

        val decoded = MilestonesRepository.decodeBindings(legacyJson)

        assertEquals(2, decoded.size)
        assertEquals(WidgetBinding("abc-123", transparent = false), decoded[12])
        assertEquals(WidgetBinding("def-456", transparent = false), decoded[15])
    }

    @Test
    fun decode_mixedLegacyAndNewEntries_handlesBoth() {
        val mixed = """{"7":"legacy-id","8":{"id":"new-id","transparent":true}}"""

        val decoded = MilestonesRepository.decodeBindings(mixed)

        assertEquals(WidgetBinding("legacy-id", transparent = false), decoded[7])
        assertEquals(WidgetBinding("new-id", transparent = true), decoded[8])
    }

    @Test
    fun decode_nullOrBlank_returnsEmpty() {
        assertTrue(MilestonesRepository.decodeBindings(null).isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("").isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("   ").isEmpty())
    }

    @Test
    fun decode_malformedJson_returnsEmpty() {
        assertTrue(MilestonesRepository.decodeBindings("not json").isEmpty())
        // a JSON array is not the expected object shape — should be rejected, not crash
        assertTrue(MilestonesRepository.decodeBindings("[1,2,3]").isEmpty())
    }

    @Test
    fun decode_skipsEntriesWithBlankOrMissingId() {
        val json = """
            {
              "1": {"id":"","transparent":true},
              "2": "",
              "3": {"transparent":true},
              "4": null
            }
        """.trimIndent()

        val decoded = MilestonesRepository.decodeBindings(json)

        assertTrue(decoded.isEmpty())
    }

    @Test
    fun decode_skipsNonIntegerWidgetIdKeys() {
        val json = """{"not-an-int":{"id":"x","transparent":false},"99":{"id":"keep","transparent":false}}"""

        val decoded = MilestonesRepository.decodeBindings(json)

        assertEquals(1, decoded.size)
        assertEquals(WidgetBinding("keep", transparent = false), decoded[99])
    }
}
