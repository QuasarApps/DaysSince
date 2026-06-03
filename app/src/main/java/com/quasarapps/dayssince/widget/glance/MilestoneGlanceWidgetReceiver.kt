package com.quasarapps.dayssince.widget.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.quasarapps.dayssince.widget.WidgetRefreshScheduler

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

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.cancelIfNoWidgets(context)
    }
}
