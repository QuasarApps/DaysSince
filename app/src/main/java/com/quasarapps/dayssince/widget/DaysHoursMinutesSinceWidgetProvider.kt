package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.R
import com.quasarapps.dayssince.SelectedStartDateTime

/**
 * Home screen widget provider (1x3).
 *
 * Shows elapsed time since the user-selected start date/time broken into:
 * - days
 * - hours
 * - minutes
 */
class DaysHoursMinutesSinceWidgetProvider : BaseDaysSinceWidgetProvider() {

    override val receiverClass: Class<out AppWidgetProvider> =
        DaysHoursMinutesSinceWidgetProvider::class.java
    override val alarmRequestCode: Int = WidgetRequestCodes.ALARM_DAYS_HOURS_MINUTES_SINCE
    override val refreshIntervalMs: Long = 15 * 60_000L
    override val wakeup: Boolean = false

    override fun buildRemoteViews(context: Context): RemoteViews {
        val picked = SelectedStartDateTime.load(context)
        val dhm = DaysSince.sincePickedDhm(picked.date, picked.time)

        val launchPendingIntent = WidgetIntents.launchMainActivity(context)

        return RemoteViews(
            context.packageName,
            R.layout.widget_days_hours_minutes_since_1x3
        ).apply {
            setTextViewText(R.id.widget_days_value, dhm.days.toString())
            setTextViewText(R.id.widget_hours_value, dhm.hours.toString())
            setTextViewText(R.id.widget_minutes_value, dhm.minutes.toString())
            setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            WidgetBroadcasts.requestUpdate(context, DaysHoursMinutesSinceWidgetProvider::class.java)
        }
    }
}
