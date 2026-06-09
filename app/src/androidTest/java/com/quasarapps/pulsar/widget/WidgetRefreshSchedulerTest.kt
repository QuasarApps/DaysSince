package com.quasarapps.pulsar.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies [WidgetRefreshScheduler] enqueues the periodic refresh as a single unique work, and that
 * re-arming it (every widget update calls [WidgetRefreshScheduler.ensureScheduled]) doesn't pile up
 * duplicates thanks to the KEEP policy. Drives the real WorkManager and cleans up after itself.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRefreshSchedulerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val workManager get() = WorkManager.getInstance(context)

    @Before
    fun clearWork() {
        workManager.cancelUniqueWork(WidgetRefreshWorker.UNIQUE_WORK_NAME).result.get()
        workManager.cancelUniqueWork(WidgetRefreshScheduler.IMMEDIATE_WORK_NAME).result.get()
    }

    @After
    fun cleanup() {
        workManager.cancelUniqueWork(WidgetRefreshWorker.UNIQUE_WORK_NAME).result.get()
        workManager.cancelUniqueWork(WidgetRefreshScheduler.IMMEDIATE_WORK_NAME).result.get()
    }

    private fun hasActiveWork(uniqueName: String): Boolean =
        workManager.getWorkInfosForUniqueWork(uniqueName).get()
            .any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }

    @Test
    fun ensureScheduled_enqueuesASingleUniquePeriodicWork() {
        // Await the enqueue Operation so the query sees the committed write deterministically,
        // rather than relying on WorkManager's internal task ordering.
        WidgetRefreshScheduler.ensureScheduled(context).result.get()

        val infos = workManager
            .getWorkInfosForUniqueWork(WidgetRefreshWorker.UNIQUE_WORK_NAME)
            .get()
        assertEquals(1, infos.size)
    }

    @Test
    fun ensureScheduled_isIdempotent_underKeepPolicy() {
        WidgetRefreshScheduler.ensureScheduled(context).result.get()
        WidgetRefreshScheduler.ensureScheduled(context).result.get()

        val infos = workManager
            .getWorkInfosForUniqueWork(WidgetRefreshWorker.UNIQUE_WORK_NAME)
            .get()
        // KEEP -> the second call must not create a duplicate.
        assertEquals(1, infos.size)
    }

    @Test
    fun refreshNow_withNoWidgetsPlaced_enqueuesNothing() {
        // No widgets are placed in the test environment, so the one-off refresh is gated off.
        val operation = WidgetRefreshScheduler.refreshNow(context)

        assertNull(operation)
        assertTrue(
            "no immediate refresh work should be queued without a placed widget",
            !hasActiveWork(WidgetRefreshScheduler.IMMEDIATE_WORK_NAME),
        )
    }

    @Test
    fun ensureScheduledIfWidgetsPlaced_withNoWidgetsPlaced_doesNotSchedule() {
        WidgetRefreshScheduler.ensureScheduledIfWidgetsPlaced(context)

        assertTrue(
            "periodic refresh must not be scheduled when no widget is placed",
            !hasActiveWork(WidgetRefreshWorker.UNIQUE_WORK_NAME),
        )
    }
}
