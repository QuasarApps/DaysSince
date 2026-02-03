package com.quasarapps.dayssince.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.quasarapps.dayssince.MainActivity

internal object WidgetIntents {

    private const val REQUEST_CODE_LAUNCH = 50001

    fun launchMainActivity(context: Context): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_LAUNCH,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

