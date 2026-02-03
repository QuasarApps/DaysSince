package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.R
import com.quasarapps.dayssince.SelectedStartDateTime

/**
 * Home screen widget provider (1x1).
 *
 * Shows the number of whole days since the user-selected start date/time.
 */
class DaysSinceWidgetProvider : BaseDaysSinceWidgetProvider() {

    override val receiverClass: Class<out AppWidgetProvider> = DaysSinceWidgetProvider::class.java
    override val alarmRequestCode: Int = REQUEST_CODE_ALARM
    override val refreshIntervalMs: Long = android.app.AlarmManager.INTERVAL_HOUR
    override val wakeup: Boolean = true

    override fun buildRemoteViews(context: Context): RemoteViews {
        val picked = SelectedStartDateTime.load(context)
        val dhm = DaysSince.sincePickedDhm(picked.date, picked.time)

        val launchPendingIntent = WidgetIntents.launchMainActivity(context)

        return RemoteViews(context.packageName, R.layout.widget_days_since_1x1).apply {
            setTextViewText(R.id.widget_day_number, dhm.days.toString())
            setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        }
    }

    companion object {
        private const val REQUEST_CODE_ALARM = 10101

        @Deprecated(
            "Use WidgetBroadcasts.ACTION_UPDATE_WIDGETS",
            ReplaceWith("WidgetBroadcasts.ACTION_UPDATE_WIDGETS")
        )
        const val ACTION_UPDATE_WIDGETS: String = WidgetBroadcasts.ACTION_UPDATE_WIDGETS

        fun requestUpdate(context: Context) {
            WidgetBroadcasts.requestUpdate(context, DaysSinceWidgetProvider::class.java)
        }
    }
}
