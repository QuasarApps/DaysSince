package com.quasarapps.pulsar.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quasarapps.pulsar.data.MilestonesRepository

/**
 * Re-renders every placed widget so the DHM widget's hours/minutes don't sit stale between data
 * changes. Scheduling/cancellation is owned by [WidgetRefreshScheduler]; this worker just refreshes.
 *
 * It also doubles as the cleanup path for removed widgets: started with [KEY_UNBIND_IDS] (see
 * [WidgetRefreshScheduler.unbindWidgets]) it drops those bindings instead. Doing the suspend write
 * here keeps it off the receiver's broadcast thread and single `goAsync()` slot.
 */
class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val unbindIds = inputData.getIntArray(KEY_UNBIND_IDS)
        if (unbindIds != null && unbindIds.isNotEmpty()) {
            // Cleanup path (widget removed): just drop the binding(s). This can't change what any
            // remaining widget shows, so skip refreshAll() and its wakeups.
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
