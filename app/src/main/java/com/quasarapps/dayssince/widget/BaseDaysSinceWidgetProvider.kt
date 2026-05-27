package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Shared widget provider plumbing:
 * - Handles update triggers (custom action + time changes)
 * - Updates all instances
 * - Schedules periodic refresh via [WidgetScheduler]
 *
 * Subclasses provide:
 * - which receiver class to target for alarms/updates
 * - how to build their [RemoteViews]
 * - desired refresh interval/wakeup behavior
 */
abstract class BaseDaysSinceWidgetProvider : AppWidgetProvider() {

    protected abstract val receiverClass: Class<out AppWidgetProvider>
    protected abstract val alarmRequestCode: Int
    protected abstract val refreshIntervalMs: Long
    protected abstract val wakeup: Boolean

    // `internal` so tests in the same module can call it directly without reflection.
    internal abstract fun buildRemoteViews(context: Context): RemoteViews

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
            receiverClass = receiverClass,
            requestCode = alarmRequestCode,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
            intervalMs = refreshIntervalMs,
            wakeup = wakeup
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetScheduler.cancelRepeating(
            context = context,
            receiverClass = receiverClass,
            requestCode = alarmRequestCode,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Note: ACTION_TIME_TICK is intentionally not handled here. It is only delivered to
        // receivers registered at runtime via Context.registerReceiver, never to manifest-declared
        // receivers like this one, so handling it would be dead code. Minute-level refreshes come
        // from the alarm scheduled in onUpdate instead.
        when (intent.action) {
            WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                WidgetUpdateHelper.updateAll(
                    context = context,
                    providerClass = receiverClass,
                    buildRemoteViews = ::buildRemoteViews
                )
            }
        }
    }
}
