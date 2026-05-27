package com.quasarapps.dayssince.widget

import android.widget.TextView
import com.quasarapps.dayssince.Prefs
import com.quasarapps.dayssince.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DaysSinceWidgetTest {

    private fun renderDayText(context: android.content.Context): String {
        val provider = DaysSinceWidgetProvider()
        val remoteViews = provider.buildRemoteViews(context)
        val view = remoteViews.apply(context, null)
        val tv = view.findViewById<TextView>(R.id.widget_day_number)
        assertNotNull(tv)
        return tv.text?.toString().orEmpty()
    }

    @Test
    fun buildRemoteViews_withValidPrefs_rendersNumericText() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString("selected_date", "2026-01-01")
            .putString("selected_time", "00:00")
            .commit()

        val text = renderDayText(context)

        assertTrue(text.isNotBlank())
        assertTrue(text.all { ch -> ch.isDigit() })
    }

    @Test
    fun buildRemoteViews_missingPrefs_doesNotCrash_rendersNumericText() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        val text = renderDayText(context)

        assertTrue(text.isNotBlank())
        assertTrue(text.all { ch -> ch.isDigit() })
    }

    @Test
    fun buildRemoteViews_invalidDateFallsBack_rendersNumericText() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString("selected_date", "not-a-date")
            .putString("selected_time", "00:00")
            .commit()

        val text = renderDayText(context)

        assertTrue(text.isNotBlank())
        assertTrue(text.all { ch -> ch.isDigit() })
    }

    @Test
    fun buildRemoteViews_invalidTimeFallsBack_rendersNumericText() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString("selected_date", "2026-01-01")
            .putString("selected_time", "not-a-time")
            .commit()

        val text = renderDayText(context)

        assertTrue(text.isNotBlank())
        assertTrue(text.all { ch -> ch.isDigit() })
    }
}
