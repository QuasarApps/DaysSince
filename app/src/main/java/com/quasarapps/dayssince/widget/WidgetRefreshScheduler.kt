package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quasarapps.dayssince.widget.glance.DaysHoursMinutesWidgetReceiver
import com.quasarapps.dayssince.widget.glance.DaysWidgetReceiver
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic [WidgetRefreshWorker] while at least one widget is placed.
 *
 * "Days since" is the headline unit and changes at most once a day; the Days·Hours·Minutes widget's
 * hours/minutes are a nice-to-have. So this favours battery over freshness — an hourly refresh that
 * is skipped while the battery is low, rather than WorkManager's 15-minute floor or the battery cost
 * of per-minute exact alarms. That still brings the DHM widget from ~6 h stale down to ~1 h, at a
 * fraction of the wake-ups, and comfortably catches the once-a-day rollover that actually matters.
 */
object WidgetRefreshScheduler {

    /**
     * Tunable refresh cadence. Hourly (not WorkManager's 15-minute minimum) because the day count is
     * the headline unit and battery matters more than to-the-minute hour/minute freshness.
     */
    const val REFRESH_INTERVAL_MINUTES = 60L

    private val widgetProviders = listOf(
        DaysWidgetReceiver::class.java,
        DaysHoursMinutesWidgetReceiver::class.java,
    )

    /**
     * Ensures the periodic refresh is running. Idempotent ([ExistingPeriodicWorkPolicy.KEEP]) so it
     * can be called from every `onUpdate` without resetting the schedule. Returns the enqueue
     * [Operation] (callers can ignore it; tests await it for determinism).
     */
    fun ensureScheduled(context: Context): Operation {
        // Don't wake the device to redraw a widget when power is scarce; a slightly stale count is
        // an acceptable trade. The widget catches up on the next run once the battery isn't low.
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES,
        ).setConstraints(constraints).build()
        return WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetRefreshWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Unique name for the one-off, data-changed refresh (kept separate from the periodic work). */
    const val IMMEDIATE_WORK_NAME = "milestone_widget_refresh_now"

    /**
     * Enqueues a one-off refresh after a milestone change so placed widgets re-render promptly.
     *
     * Routed through WorkManager (rather than only an in-process `updateAll`) so the refresh still
     * runs if the app is backgrounded or its process is torn down right after the edit.
     * [ExistingWorkPolicy.REPLACE] collapses rapid successive edits into a single refresh of the
     * latest data.
     */
    fun refreshNow(context: Context): Operation {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        return WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /** Cancels the periodic refresh once no widgets of either type remain placed. */
    fun cancelIfNoWidgets(context: Context) {
        if (!hasPlacedWidgets(context)) {
            WorkManager.getInstance(context).cancelUniqueWork(WidgetRefreshWorker.UNIQUE_WORK_NAME)
        }
    }

    private fun hasPlacedWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        return widgetProviders.any { provider ->
            manager.getAppWidgetIds(ComponentName(context, provider)).isNotEmpty()
        }
    }
}
