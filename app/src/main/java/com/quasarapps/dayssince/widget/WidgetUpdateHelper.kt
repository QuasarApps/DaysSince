package com.quasarapps.dayssince.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

internal object WidgetUpdateHelper {

    fun <T> updateAll(
        context: Context,
        providerClass: Class<T>,
        buildRemoteViews: (Context) -> RemoteViews
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
        if (ids.isEmpty()) return

        val views = buildRemoteViews(context)
        for (id in ids) {
            manager.updateAppWidget(id, views)
        }
    }
}

