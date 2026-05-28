package com.quasarapps.dayssince.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.quasarapps.dayssince.widget.glance.DaysHoursMinutesWidget
import com.quasarapps.dayssince.widget.glance.DaysWidget

/** Re-renders every placed widget. Call after milestones change. */
object MilestoneWidgets {
    suspend fun refreshAll(context: Context) {
        DaysWidget().updateAll(context)
        DaysHoursMinutesWidget().updateAll(context)
    }
}
