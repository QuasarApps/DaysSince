package com.quasarapps.pulsar.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quasarapps.pulsar.data.MilestonesRepository

/**
 * Periodic worker that re-renders every placed widget so the Days·Hours·Minutes widget's
 * hours/minutes don't sit stale between data changes.
 *
 * Scheduling (and cancellation when no widgets remain) is owned by [WidgetRefreshScheduler]; this
 * worker just performs the refresh.
 *
 * It also doubles as the cleanup path for removed widgets: when started with [KEY_UNBIND_IDS] in its
 * input data (see [WidgetRefreshScheduler.unbindWidgets]), it instead drops those widgets' bindings.
 * Doing the (suspend) DataStore write here — rather than in the receiver's `onDeleted` — keeps it off
 * the short-lived broadcast thread and clear of the single `goAsync()` slot that
 * [com.quasarapps.pulsar.widget.glance.MilestoneGlanceWidgetReceiver]'s superclass already uses.
 */
class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val unbindIds = inputData.getIntArray(KEY_UNBIND_IDS)
        if (unbindIds != null && unbindIds.isNotEmpty()) {
            // Cleanup path (a widget was removed): just drop the binding(s). Removing a deleted
            // widget's binding can't change what any *remaining* widget shows, so there's nothing to
            // re-render — skip refreshAll() and the extra wakeups it would cost.
            val repo = MilestonesRepository(applicationContext)
            unbindIds.forEach { repo.unbindWidget(it) }
            return Result.success()
        }
        // Refresh path (data changed / periodic tick): re-render every placed widget.
        MilestoneWidgets.refreshAll(applicationContext)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "milestone_widget_refresh"

        /** Input-data key: an int array of appWidgetIds whose bindings should be removed. */
        const val KEY_UNBIND_IDS = "unbind_widget_ids"
    }
}
