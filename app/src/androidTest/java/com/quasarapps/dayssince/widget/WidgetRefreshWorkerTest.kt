package com.quasarapps.dayssince.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the [WidgetRefreshWorker] runs its widget refresh to completion and reports success.
 * Uses [TestListenableWorkerBuilder] so `doWork()` executes directly without needing WorkManager
 * scheduling.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRefreshWorkerTest {

    @Test
    fun doWork_refreshesWidgetsAndSucceeds() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<WidgetRefreshWorker>(context).build()

        val result = runBlocking { worker.doWork() }

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
