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
class DaysHoursMinutesSinceWidgetTest {

    private data class DhmText(val days: String, val hours: String, val minutes: String)

    private fun renderDhmText(context: android.content.Context): DhmText {
        val provider = DaysHoursMinutesSinceWidgetProvider()
        val rv = provider.buildRemoteViews(context)
        val view = rv.apply(context, null)

        fun textOf(id: Int): String {
            val tv = view.findViewById<TextView>(id)
            assertNotNull(tv)
            return tv.text?.toString().orEmpty()
        }

        return DhmText(
            days = textOf(R.id.widget_days_value),
            hours = textOf(R.id.widget_hours_value),
            minutes = textOf(R.id.widget_minutes_value)
        )
    }

    private fun DhmText.assertAllNumeric() {
        assertTrue(days.isNotBlank() && days.all { it.isDigit() })
        assertTrue(hours.isNotBlank() && hours.all { it.isDigit() })
        assertTrue(minutes.isNotBlank() && minutes.all { it.isDigit() })
    }

    @Test
    fun buildRemoteViews_withValidPrefs_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString("selected_date", "2026-01-01")
            .putString("selected_time", "00:00")
            .commit()

        renderDhmText(context).assertAllNumeric()
    }

    @Test
    fun buildRemoteViews_missingPrefs_doesNotCrash_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()

        renderDhmText(context).assertAllNumeric()
    }

    @Test
    fun buildRemoteViews_invalidPrefs_fallBacks_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()
        Prefs.get(context).edit().clear().commit()
        Prefs.get(context).edit()
            .putString("selected_date", "not-a-date")
            .putString("selected_time", "not-a-time")
            .commit()

        renderDhmText(context).assertAllNumeric()
    }
}
