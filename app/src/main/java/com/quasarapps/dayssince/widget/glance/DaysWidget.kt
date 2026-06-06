package com.quasarapps.dayssince.widget.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import com.quasarapps.dayssince.data.MilestonesRepository
import kotlinx.coroutines.flow.first

/** 1x1 widget: whole days since the bound milestone. */
class DaysWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val dataFlow = MilestonesRepository(context).widgetRenderDataFlow(appWidgetId)
        // Collect inside the composition so the widget re-renders with fresh data when the bound
        // milestone is edited — not just the snapshot captured the first time it was composed.
        val initial = dataFlow.first()
        provideContent {
            val data by dataFlow.collectAsState(initial = initial)
            DaysWidgetContent(data.milestone, data.transparent)
        }
    }
}

class DaysWidgetReceiver : MilestoneGlanceWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysWidget()
}
