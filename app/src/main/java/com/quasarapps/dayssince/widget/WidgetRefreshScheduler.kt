package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quasarapps.dayssince.widget.glance.DaysHoursMinutesWidgetReceiver
import com.quasarapps.dayssince.widget.glance.DaysWidgetReceiver
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic [WidgetRefreshWorker] while at least one widget is placed.
 *
 * The system's `updatePeriodMillis` is floored at 30 minutes and WorkManager's periodic minimum is
 * 15 minutes, so 15 minutes is the freshest a *deferrable* refresh can be. That keeps the
 * Days·Hours·Minutes widget within ~15 min instead of the previous ~6 h, without the battery cost
 * of per-minute exact alarms. (The Days widget changes at most once a day, so this is ample for it.)
 */
object WidgetRefreshScheduler {

    /** Tunable: the refresh cadence. 15 min is WorkManager's periodic minimum. */
    const val REFRESH_INTERVAL_MINUTES = 15L

    private val widgetProviders = listOf(
        DaysWidgetReceiver::class.java,
        DaysHoursMinutesWidgetReceiver::class.java,
    )

    /**
     * Ensures the periodic refresh is running. Idempotent ([ExistingPeriodicWorkPolicy.KEEP]) so it
     * can be called from every `onUpdate` without resetting the schedule.
     */
    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES,
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetRefreshWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
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
