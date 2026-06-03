package com.quasarapps.dayssince.widget.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import com.quasarapps.dayssince.data.MilestonesRepository

/** 1x1 widget: whole days since the bound milestone. */
class DaysWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val repo = MilestonesRepository(context)
        val binding = repo.bindingForWidget(appWidgetId)
        val milestone = binding?.let { repo.getById(it.milestoneId) }
        val transparent = binding?.transparent ?: false
        provideContent { DaysWidgetContent(milestone, transparent) }
    }
}

class DaysWidgetReceiver : MilestoneGlanceWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysWidget()
}
