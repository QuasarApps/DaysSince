package com.quasarapps.dayssince.widget.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.quasarapps.dayssince.data.MilestonesRepository

/** Wide widget: days / hours / minutes since the bound milestone. */
class DaysHoursMinutesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val milestone = MilestonesRepository(context).milestoneForWidget(appWidgetId)
        provideContent { DaysHoursMinutesWidgetContent(milestone) }
    }
}

class DaysHoursMinutesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysHoursMinutesWidget()
}
