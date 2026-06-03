package com.quasarapps.dayssince.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for [AccentKeys], the persistence-layer key↔index registry that lets the accent
 * palette be reordered without recoloring stored milestones.
 *
 * Assertions derive from the registry itself (rather than hardcoding e.g. "rose" == 3) so the tests
 * stay correct if the palette is reordered or extended — the whole point of stable keys.
 */
class AccentKeysTest {

    @Test
    fun orderedKeys_areDistinctAndNonBlank() {
        assertEquals("keys must be unique", AccentKeys.ordered.size, AccentKeys.ordered.toSet().size)
        assertFalse("no key may be blank", AccentKeys.ordered.any { it.isBlank() })
    }

    @Test
    fun keyAndIndex_roundTripForEveryAccent() {
        AccentKeys.ordered.indices.forEach { index ->
            assertEquals(index, AccentKeys.indexForKey(AccentKeys.keyForIndex(index)))
        }
    }

    @Test
    fun indexForKey_unknownNullOrBlank_fallsBackToDefault() {
        assertEquals(AccentKeys.DEFAULT_INDEX, AccentKeys.indexForKey("no-such-accent"))
        assertEquals(AccentKeys.DEFAULT_INDEX, AccentKeys.indexForKey(null))
        assertEquals(AccentKeys.DEFAULT_INDEX, AccentKeys.indexForKey(""))
    }

    @Test
    fun keyForIndex_outOfRange_fallsBackToTheDefaultAccentKey() {
        // Mirrors ui.theme.accentOrDefault's index-1 fallback for a corrupt/out-of-range index.
        val fallbackKey = AccentKeys.ordered[1]
        assertEquals(fallbackKey, AccentKeys.keyForIndex(AccentKeys.ordered.size))
        assertEquals(fallbackKey, AccentKeys.keyForIndex(-1))
        assertEquals(fallbackKey, AccentKeys.keyForIndex(Int.MAX_VALUE))
    }

    @Test
    fun dynamicKeyIsTheDefaultIndex() {
        // "dynamic" (Material You) is contractually the zero index / default.
        assertEquals(0, AccentKeys.DEFAULT_INDEX)
        assertEquals("dynamic", AccentKeys.ordered[AccentKeys.DEFAULT_INDEX])
    }
}
