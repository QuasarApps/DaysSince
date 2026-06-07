package com.quasarapps.pulsar.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device coverage for the widget-binding (de)serialization in [MilestonesRepository], including
 * the backwards-compatible decoding of the legacy bare-string format. Exercises the device's real
 * `org.json` runtime.
 */
@RunWith(AndroidJUnit4::class)
class MilestonesRepositoryBindingsInstrumentedTest {

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
        val legacyJson = """{"12":"abc-123","15":"def-456"}"""

        val decoded = MilestonesRepository.decodeBindings(legacyJson)

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
    fun decode_nullBlankOrMalformed_returnsEmpty() {
        assertTrue(MilestonesRepository.decodeBindings(null).isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("").isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("   ").isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("not json").isEmpty())
        assertTrue(MilestonesRepository.decodeBindings("[1,2,3]").isEmpty())
    }

    @Test
    fun decode_skipsBlankIdsAndNonIntegerKeys() {
        val json = """{"1":{"id":"","transparent":true},"not-an-int":"x","99":{"id":"keep"}}"""

        val decoded = MilestonesRepository.decodeBindings(json)

        assertEquals(1, decoded.size)
        assertEquals(WidgetBinding("keep", transparent = false), decoded[99])
    }
}
