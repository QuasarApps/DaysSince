package com.quasarapps.dayssince.widget

import android.content.Context
import android.content.Intent

internal object WidgetBroadcasts {

    /**
     * Shared custom action used to request an immediate widget refresh.
     */
    const val ACTION_UPDATE_WIDGETS = "com.quasarapps.dayssince.widget.ACTION_UPDATE_WIDGETS"

    fun requestUpdate(context: Context, receiverClass: Class<*>) {
        val intent = Intent(context, receiverClass).apply {
            action = ACTION_UPDATE_WIDGETS
        }
        context.sendBroadcast(intent)
    }
}

