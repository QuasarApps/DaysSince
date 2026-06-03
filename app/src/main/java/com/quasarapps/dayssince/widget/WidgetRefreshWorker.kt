package com.quasarapps.dayssince.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Periodic worker that re-renders every placed widget so the Days·Hours·Minutes widget's
 * hours/minutes don't sit stale between data changes.
 *
 * Scheduling (and cancellation when no widgets remain) is owned by [WidgetRefreshScheduler]; this
 * worker just performs the refresh.
 */
class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        MilestoneWidgets.refreshAll(applicationContext)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "milestone_widget_refresh"
    }
}
