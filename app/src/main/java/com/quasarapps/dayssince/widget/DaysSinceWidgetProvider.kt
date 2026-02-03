package com.quasarapps.dayssince.widget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.R
import com.quasarapps.dayssince.SelectedStartDateTime

/**
 * Home screen widget provider (1x1).
 *
 * Shows the number of whole days since the user-selected start date/time.
 */
class DaysSinceWidgetProvider : AppWidgetProvider() {

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
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = REQUEST_CODE_ALARM,
            action = ACTION_UPDATE_WIDGETS,
            intervalMs = AlarmManager.INTERVAL_HOUR,
            wakeup = true
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK -> {
                WidgetUpdateHelper.updateAll(
                    context = context,
                    providerClass = DaysSinceWidgetProvider::class.java,
                    buildRemoteViews = ::buildRemoteViews
                )
            }
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val picked = SelectedStartDateTime.load(context)

        val dhm = DaysSince.sincePickedDhm(picked.date, picked.time)

        val launchPendingIntent = WidgetIntents.launchMainActivity(context)

        return RemoteViews(context.packageName, R.layout.widget_days_since_1x1).apply {
            setTextViewText(R.id.widget_day_number, dhm.days.toString())
            setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleHourlyUpdate(context: Context) {
        // Intentionally left for binary compatibility; now delegated to WidgetScheduler.
        WidgetScheduler.scheduleInexactRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = REQUEST_CODE_ALARM,
            action = ACTION_UPDATE_WIDGETS,
            intervalMs = AlarmManager.INTERVAL_HOUR,
            wakeup = true
        )
    }

    companion object {
        private const val REQUEST_CODE_ALARM = 10101

        const val ACTION_UPDATE_WIDGETS = "com.quasarapps.dayssince.widget.ACTION_UPDATE_WIDGETS"

        fun requestUpdate(context: Context) {
            val intent = Intent(context, DaysSinceWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGETS
            }
            context.sendBroadcast(intent)
        }
    }
}
