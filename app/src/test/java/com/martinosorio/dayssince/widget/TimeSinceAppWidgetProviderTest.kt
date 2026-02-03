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

    private fun invokeBuildRemoteViews(
        provider: DaysHoursMinutesSinceWidgetProvider,
        context: android.content.Context
    ): android.widget.RemoteViews {
        return provider
            .javaClass
            .getDeclaredMethod("buildRemoteViews", android.content.Context::class.java)
            .apply { isAccessible = true }
            .invoke(provider, context) as android.widget.RemoteViews
    }

    private fun renderText(
        remoteViews: android.widget.RemoteViews,
        context: android.content.Context,
        id: Int
    ): String {
        val view = remoteViews.apply(context, null)
        val tv = view.findViewById<TextView>(id)
        assertNotNull(tv)
        return tv.text?.toString().orEmpty()
    }

    @Test
    fun buildRemoteViews_withValidPrefs_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()

        val prefs = Prefs.get(context)
        prefs.edit().clear().commit()
        prefs.edit()
            .putString("selected_date", "2026-01-01")
            .putString("selected_time", "00:00")
            .commit()

        val provider = DaysHoursMinutesSinceWidgetProvider()
        val rv = invokeBuildRemoteViews(provider, context)

        val days = renderText(rv, context, R.id.widget_days_value)
        val hours = renderText(rv, context, R.id.widget_hours_value)
        val minutes = renderText(rv, context, R.id.widget_minutes_value)

        assertTrue(days.isNotBlank() && days.all { it.isDigit() })
        assertTrue(hours.isNotBlank() && hours.all { it.isDigit() })
        assertTrue(minutes.isNotBlank() && minutes.all { it.isDigit() })
    }

    @Test
    fun buildRemoteViews_missingPrefs_doesNotCrash_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()

        val prefs = Prefs.get(context)
        prefs.edit().clear().commit()

        val provider = DaysHoursMinutesSinceWidgetProvider()
        val rv = invokeBuildRemoteViews(provider, context)

        val days = renderText(rv, context, R.id.widget_days_value)
        val hours = renderText(rv, context, R.id.widget_hours_value)
        val minutes = renderText(rv, context, R.id.widget_minutes_value)

        assertTrue(days.isNotBlank() && days.all { it.isDigit() })
        assertTrue(hours.isNotBlank() && hours.all { it.isDigit() })
        assertTrue(minutes.isNotBlank() && minutes.all { it.isDigit() })
    }

    @Test
    fun buildRemoteViews_invalidPrefs_fallBacks_rendersNumericTexts() {
        val context = RuntimeEnvironment.getApplication()

        val prefs = Prefs.get(context)
        prefs.edit().clear().commit()
        prefs.edit()
            .putString("selected_date", "not-a-date")
            .putString("selected_time", "not-a-time")
            .commit()

        val provider = DaysHoursMinutesSinceWidgetProvider()
        val rv = invokeBuildRemoteViews(provider, context)

        val days = renderText(rv, context, R.id.widget_days_value)
        val hours = renderText(rv, context, R.id.widget_hours_value)
        val minutes = renderText(rv, context, R.id.widget_minutes_value)

        assertTrue(days.isNotBlank() && days.all { it.isDigit() })
        assertTrue(hours.isNotBlank() && hours.all { it.isDigit() })
        assertTrue(minutes.isNotBlank() && minutes.all { it.isDigit() })
    }
}
