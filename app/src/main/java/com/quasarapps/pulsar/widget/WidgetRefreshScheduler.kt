package com.quasarapps.pulsar.widget

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
import androidx.work.workDataOf
import com.quasarapps.pulsar.widget.glance.DaysHoursMinutesWidgetReceiver
import com.quasarapps.pulsar.widget.glance.DaysWidgetReceiver
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic [WidgetRefreshWorker] while at least one widget is placed.
 *
 * "Days since" is the headline unit and changes at most once a day; the Days·Hours·Minutes widget's
 * hours/minutes are a nice-to-have. So this favours battery over freshness — an hourly refresh that
 * is skipped while the battery is low, rather than WorkManager's 15-minute floor or the battery cost
 * of per-minute exact alarms. That still brings the DHM widget from ~6 h stale down to ~1 h, at a
 * fraction of the wake-ups, and comfortably catches the once-a-day rollover that actually matters.
 *
 * This battery-aware WorkManager job is the *single* periodic refresh mechanism: the widgets set
 * `updatePeriodMillis="0"`, so there's no platform AlarmManager refresh competing with (and
 * ignoring the battery constraint of) this one. It's armed from each provider's `onUpdate` and
 * re-armed on app start via [ensureScheduledIfWidgetsPlaced]; WorkManager persists it across reboots.
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

    /**
     * Re-arms the periodic refresh on app start, but only if a widget is actually placed — so a user
     * with no widgets never schedules background work. (Unlike [ensureScheduled], which is called
     * from `onUpdate` where a widget is known to exist.) This covers the case where the persisted
     * periodic work was lost — e.g. the OS dropped it, or an app-data clear wiped WorkManager's db —
     * since with `updatePeriodMillis=0` there's no platform alarm to fall back on.
     */
    fun ensureScheduledIfWidgetsPlaced(context: Context) {
        if (hasPlacedWidgets(context)) ensureScheduled(context)
    }

    /** Unique name for the one-off, data-changed refresh (kept separate from the periodic work). */
    const val IMMEDIATE_WORK_NAME = "milestone_widget_refresh_now"

    /**
     * Enqueues a one-off refresh after a milestone change so placed widgets re-render promptly —
     * the single redraw path for an edit (no redundant in-process `updateAll` alongside it).
     *
     * Routed through WorkManager (rather than an in-process `updateAll`) so the refresh still runs if
     * the app is backgrounded or its process is torn down right after the edit; the one-off is
     * unconstrained, so it runs within ~a second — imperceptible while the user is still in-app.
     * [ExistingWorkPolicy.REPLACE] collapses rapid successive edits into a single refresh of the
     * latest data. Returns null (enqueuing nothing) when no widget is placed.
     */
    fun refreshNow(context: Context): Operation? {
        if (!hasPlacedWidgets(context)) return null
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        return WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /**
     * Removes the saved bindings for [appWidgetIds] after their widgets are deleted, so the
     * bindings map can't grow without bound as widgets are added and removed over time.
     *
     * Runs the (suspend) DataStore write inside [WidgetRefreshWorker] rather than synchronously in
     * the receiver's `onDeleted`: the receiver runs on the broadcast thread and its superclass
     * already claims the single `goAsync()` slot, so this is the safe place to do durable async work.
     * Enqueued non-uniquely so concurrent deletions each carry (and remove) their own ids.
     */
    fun unbindWidgets(context: Context, appWidgetIds: IntArray): Operation {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInputData(workDataOf(WidgetRefreshWorker.KEY_UNBIND_IDS to appWidgetIds))
            .build()
        return WorkManager.getInstance(context).enqueue(request)
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
