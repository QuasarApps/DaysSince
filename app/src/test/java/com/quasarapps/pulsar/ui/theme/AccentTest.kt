package com.quasarapps.pulsar.ui.theme

import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.AccentKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for the accent palette lookup. The only branching logic here is
 * [accentOrDefault]'s out-of-range fallback, which several widget/UI paths rely on to avoid an
 * IndexOutOfBounds when a stored milestone references an accent index that no longer exists.
 */
class AccentTest {

    @Test
    fun defaultAccent_isMagentaAtIndexZero() {
        assertEquals(0, AccentKeys.DEFAULT_INDEX)
        assertEquals("magenta", MilestoneAccents[0].key)
        assertEquals(R.string.accent_magenta, MilestoneAccents[0].labelRes)
    }

    @Test
    fun accentOrDefault_validIndices_returnThatAccent() {
        assertSame(MilestoneAccents[0], accentOrDefault(0))
        assertSame(MilestoneAccents[1], accentOrDefault(1))
        // Last valid index.
        val last = MilestoneAccents.lastIndex
        assertSame(MilestoneAccents[last], accentOrDefault(last))
    }

    @Test
    fun accentOrDefault_outOfRange_fallsBackToDefaultAccent() {
        val fallback = MilestoneAccents[0]
        // Past the end, negative, and far out of range all fall back to the same default.
        assertSame(fallback, accentOrDefault(MilestoneAccents.size))
        assertSame(fallback, accentOrDefault(-1))
        assertSame(fallback, accentOrDefault(Int.MAX_VALUE))
        assertSame(fallback, accentOrDefault(Int.MIN_VALUE))
    }

    @Test
    fun milestoneAccents_haveDistinctNonZeroLabelResources() {
        val labels = MilestoneAccents.map { it.labelRes }
        assertEquals("label resources must be unique", labels.size, labels.toSet().size)
        // JUnit assertion (not Kotlin `assert`, which is a no-op unless JVM -ea is set).
        // 0 is the "no resource" sentinel — every accent must point at a real string.
        assertFalse("no accent label resource may be 0", labels.any { it == 0 })
    }

    @Test
    fun milestoneAccents_haveDistinctNonBlankStableKeys() {
        val keys = MilestoneAccents.map { it.key }
        // Keys are the persistence contract — they must be unique and stable.
        assertEquals("keys must be unique", keys.size, keys.toSet().size)
        assertFalse("no accent key may be blank", keys.any { it.isBlank() })
    }

    @Test
    fun paletteKeys_matchTheDataLayerRegistryInOrder() {
        // The persistence registry (data.AccentKeys) and the UI palette must agree on key *order* —
        // index N in one has to be the same accent as index N in the other, or a stored accent
        // would resolve to the wrong color. (Resolver behavior itself is covered by AccentKeysTest.)
        assertEquals(AccentKeys.ordered, MilestoneAccents.map { it.key })
    }
}
