package com.quasarapps.pulsar.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.quasarapps.pulsar.widget.glance.DaysHoursMinutesWidget
import com.quasarapps.pulsar.widget.glance.DaysWidget

/** Re-renders every placed widget. Call after milestones change. */
object MilestoneWidgets {
    suspend fun refreshAll(context: Context) {
        DaysWidget().updateAll(context)
        DaysHoursMinutesWidget().updateAll(context)
    }
}
