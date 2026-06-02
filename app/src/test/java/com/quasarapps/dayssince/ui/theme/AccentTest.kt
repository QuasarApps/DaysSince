package com.quasarapps.dayssince.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for the accent palette lookup. The only branching logic here is
 * [accentOrDefault]'s out-of-range fallback, which several widget/UI paths rely on to avoid an
 * IndexOutOfBounds when a stored milestone references an accent index that no longer exists.
 */
class AccentTest {

    @Test
    fun dynamicAccentIsIndexZero() {
        assertEquals(0, DYNAMIC_ACCENT)
        assertEquals("Dynamic", MilestoneAccents[DYNAMIC_ACCENT].label)
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
    fun accentOrDefault_outOfRange_fallsBackToIndexOne() {
        val fallback = MilestoneAccents[1]
        // Past the end, negative, and far out of range all fall back to the same default.
        assertSame(fallback, accentOrDefault(MilestoneAccents.size))
        assertSame(fallback, accentOrDefault(-1))
        assertSame(fallback, accentOrDefault(Int.MAX_VALUE))
        assertSame(fallback, accentOrDefault(Int.MIN_VALUE))
    }

    @Test
    fun milestoneAccents_haveDistinctNonBlankLabels() {
        val labels = MilestoneAccents.map { it.label }
        assertEquals("labels must be unique", labels.size, labels.toSet().size)
        assert(labels.none { it.isBlank() }) { "no accent label may be blank" }
    }
}
