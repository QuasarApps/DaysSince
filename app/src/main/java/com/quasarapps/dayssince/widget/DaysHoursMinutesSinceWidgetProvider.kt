package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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
class DaysHoursMinutesSinceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val views = buildRemoteViews(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        WidgetScheduler.scheduleInexactRepeating(
            context = context,
            receiverClass = DaysHoursMinutesSinceWidgetProvider::class.java,
            requestCode = REQUEST_CODE_ALARM,
            action = DaysSinceWidgetProvider.ACTION_UPDATE_WIDGETS,
            intervalMs = 15 * 60_000L,
            wakeup = false
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            DaysSinceWidgetProvider.ACTION_UPDATE_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK -> {
                WidgetUpdateHelper.updateAll(
                    context = context,
                    providerClass = DaysHoursMinutesSinceWidgetProvider::class.java,
                    buildRemoteViews = ::buildRemoteViews
                )
            }
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
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
        private const val REQUEST_CODE_ALARM = 20201

        fun requestUpdate(context: Context) {
            val intent = Intent(context, DaysHoursMinutesSinceWidgetProvider::class.java).apply {
                action = DaysSinceWidgetProvider.ACTION_UPDATE_WIDGETS
            }
            context.sendBroadcast(intent)
        }
    }
}
