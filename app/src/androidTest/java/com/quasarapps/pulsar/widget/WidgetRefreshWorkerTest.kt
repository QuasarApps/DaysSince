package com.quasarapps.pulsar.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.quasarapps.pulsar.data.MilestonesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the [WidgetRefreshWorker]: it runs its widget refresh to completion and reports success,
 * and — when given [WidgetRefreshWorker.KEY_UNBIND_IDS] — removes those widgets' bindings (the
 * cleanup path the receiver's `onDeleted` routes through). Uses [TestListenableWorkerBuilder] so
 * `doWork()` executes directly without needing WorkManager scheduling.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRefreshWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    // High, test-only id so it can't collide with a real placed widget on the device.
    private val testWidgetId = 987_654

    @After
    fun cleanUp() {
        runBlocking { MilestonesRepository(context).unbindWidget(testWidgetId) }
    }

    @Test
    fun doWork_refreshesWidgetsAndSucceeds() {
        val worker = TestListenableWorkerBuilder<WidgetRefreshWorker>(context).build()

        val result = runBlocking { worker.doWork() }

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_withUnbindIds_removesThoseBindings() = runBlocking {
        val repo = MilestonesRepository(context)
        repo.bindWidget(testWidgetId, "some-milestone-id")
        assertNotNull("precondition: binding exists", repo.bindingForWidget(testWidgetId))

        val worker = TestListenableWorkerBuilder<WidgetRefreshWorker>(context)
            .setInputData(workDataOf(WidgetRefreshWorker.KEY_UNBIND_IDS to intArrayOf(testWidgetId)))
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertNull(repo.bindingForWidget(testWidgetId))
    }
}
