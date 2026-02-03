package com.quasarapps.dayssince

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PrefsTest {

    private fun ctx(): Context = RuntimeEnvironment.getApplication()

    @Test
    fun prefs_get_returnsNonNullPrefs() {
        val prefs = Prefs.get(ctx())
        assertNotNull(prefs)
    }

    @Test
    fun prefs_roundTripString() {
        val prefs = Prefs.get(ctx())
        prefs.edit().clear().commit()

        prefs.edit().putString("k", "v").commit()
        assertEquals("v", prefs.getString("k", null))
    }

    @Test
    fun prefs_persistsAcrossInstances() {
        val context = ctx()

        val p1 = Prefs.get(context)
        p1.edit().clear().commit()
        p1.edit().putString("k", "v").commit()

        val p2 = Prefs.get(context)
        assertEquals("v", p2.getString("k", null))
    }

    @Test
    fun prefs_overwriteValue_lastWriteWins() {
        val prefs = Prefs.get(ctx())
        prefs.edit().clear().commit()

        prefs.edit().putString("k", "v1").commit()
        prefs.edit().putString("k", "v2").commit()

        assertEquals("v2", prefs.getString("k", null))
    }

    @Test
    fun prefs_removeKey_returnsDefault() {
        val prefs = Prefs.get(ctx())
        prefs.edit().clear().commit()

        prefs.edit().putString("k", "v").commit()
        prefs.edit().remove("k").commit()

        assertEquals("d", prefs.getString("k", "d"))
    }

    @Test
    fun prefs_clear_removesAllKeys() {
        val prefs = Prefs.get(ctx())
        prefs.edit().clear().commit()

        prefs.edit().putString("k1", "v1").putString("k2", "v2").commit()
        prefs.edit().clear().commit()

        assertEquals(null, prefs.getString("k1", null))
        assertEquals(null, prefs.getString("k2", null))
    }

    @Test
    fun prefs_storesEmptyAndUnicodeStrings() {
        val prefs = Prefs.get(ctx())
        prefs.edit().clear().commit()

        prefs.edit().putString("empty", "").commit()
        prefs.edit().putString("unicode", "Δays since 🚫")
            .commit()

        assertEquals("", prefs.getString("empty", null))
        assertEquals("Δays since 🚫", prefs.getString("unicode", null))
    }
}
