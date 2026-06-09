package com.quasarapps.pulsar.widget.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.quasarapps.pulsar.widget.WidgetRefreshScheduler

/**
 * Base for the app's Glance widget receivers. Keeps the periodic refresh scheduled while widgets are
 * placed (re-armed idempotently on every update) and cancels it once the last widget is removed.
 */
abstract class MilestoneGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefreshScheduler.ensureScheduled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Drop the removed widgets' bindings (via WorkManager — the write is suspend and the
        // broadcast thread / goAsync() slot are claimed here by the superclass).
        WidgetRefreshScheduler.unbindWidgets(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.cancelIfNoWidgets(context)
    }
}
