package com.quasarapps.dayssince.widget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

internal object WidgetScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleInexactRepeating(
        context: Context,
        receiverClass: Class<*>,
        requestCode: Int,
        action: String,
        intervalMs: Long,
        wakeup: Boolean,
        firstDelayMs: Long = 60_000L
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, receiverClass).apply { this.action = action }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val firstTriggerElapsed = SystemClock.elapsedRealtime() + firstDelayMs

        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(
            if (wakeup) AlarmManager.ELAPSED_REALTIME_WAKEUP else AlarmManager.ELAPSED_REALTIME,
            firstTriggerElapsed,
            intervalMs,
            pendingIntent
        )
    }

    fun cancelRepeating(
        context: Context,
        receiverClass: Class<*>,
        requestCode: Int,
        action: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, receiverClass).apply { this.action = action }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

