package com.quasarapps.dayssince.widget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.MainActivity
import com.quasarapps.dayssince.Prefs
import com.quasarapps.dayssince.R
import com.quasarapps.dayssince.widget.DayOfMonthAppWidgetProvider.Companion.PREF_SELECTED_DATE
import com.quasarapps.dayssince.widget.DayOfMonthAppWidgetProvider.Companion.PREF_SELECTED_TIME
import java.time.LocalDate
import java.time.LocalTime

/**
 * Home screen widget provider.
 *
 * # What it shows
 * Displays the number of **whole days** since a user-selected "start" date/time.
 *
 * The app persists the selected date/time in [Prefs] under:
 * - [PREF_SELECTED_DATE] (ISO-8601 [LocalDate], e.g. `2026-01-01`)
 * - [PREF_SELECTED_TIME] (ISO-8601 [LocalTime], e.g. `00:00`)
 *
 * # Update strategy (how/when it refreshes)
 * Widgets don't run continuously; we refresh from a few event sources:
 *
 * 1) **Manual refresh** when the user changes date/time in the app.
 *    The app calls [requestUpdate] which broadcasts [ACTION_UPDATE_WIDGETS] to this provider.
 *
 * 2) **System time changes**.
 *    If the user changes system time or timezone, the computed value should be recalculated.
 *    We listen for:
 *    - [Intent.ACTION_TIME_CHANGED]
 *    - [Intent.ACTION_TIMEZONE_CHANGED]
 *    - [Intent.ACTION_TIME_TICK] (best-effort; delivery may vary by Android version)
 *
 * 3) **Background periodic refresh**.
 *    We schedule an **inexact** repeating alarm once per hour via [AlarmManager]. This is a
 *    battery-friendly periodic refresh (not exact) to keep "days since" correct relative to now.
 *
 * Notes:
 * - We intentionally use `setInexactRepeating` rather than exact alarms.
 * - The refresh cadence is *best effort*: the system may batch alarms.
 *
 * # Click behavior
 * Tapping the widget launches [MainActivity].
 */
class DayOfMonthAppWidgetProvider : AppWidgetProvider() {

    /**
     * Called by the system to request widget updates (e.g., when the widget is first added).
     * We update all instances and ensure our hourly refresh is scheduled.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }

        // Recompute periodically so the value stays current relative to "now".
        scheduleHourlyUpdate(context)
    }

    /**
     * Receives both AppWidget events and a few system time change broadcasts.
     *
     * When any of these arrive, we trigger an update for all widget instances.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK -> {
                val manager = AppWidgetManager.getInstance(context)
                val component =
                    android.content.ComponentName(context, DayOfMonthAppWidgetProvider::class.java)
                val ids = manager.getAppWidgetIds(component)
                if (ids.isNotEmpty()) {
                    onUpdate(context, manager, ids)
                }
            }
        }
    }

    /**
     * Builds the widget UI.
     *
     * Implementation details:
     * - Reads the persisted date/time.
     * - Parses them using [LocalDate.parse] / [LocalTime.parse].
     * - Falls back safely if prefs are missing or corrupted so the widget never crashes.
     */
    private fun buildRemoteViews(context: Context): RemoteViews {
        val prefs = Prefs.get(context)

        // Parse persisted date; fall back to "today" if missing/bad.
        val pickedDate = prefs.getString(PREF_SELECTED_DATE, null)
            ?.runCatching(LocalDate::parse)
            ?.getOrNull() ?: LocalDate.now()

        // Parse persisted time; fall back to midnight if missing/bad.
        val pickedTime = prefs.getString(PREF_SELECTED_TIME, null)
            ?.runCatching(LocalTime::parse)
            ?.getOrNull() ?: LocalTime.MIDNIGHT

        val days = DaysSince.sincePicked(pickedDate, pickedTime)

        // Launch the app when the widget is tapped.
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            // ClearTop keeps the navigation stack tidy if the activity is already running.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val launchPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_LAUNCH,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return RemoteViews(context.packageName, R.layout.widget_day_of_month).apply {
            setTextViewText(R.id.widget_day_number, days.toString())
            setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        }
    }

    /**
     * Schedules a background refresh roughly once per hour.
     *
     * We use elapsed realtime (vs wall clock) so that time changes/timezone changes don't cause
     * weird scheduling behavior.
     *
     * This is intentionally *inexact* so we don't require privileged exact alarm permissions.
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleHourlyUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DayOfMonthAppWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGETS
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // First trigger after ~1 minute, then inexact hourly.
        val firstTriggerElapsed = SystemClock.elapsedRealtime() + 60_000L

        // Cancel then reschedule so repeated calls keep only one alarm active.
        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            firstTriggerElapsed,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }

    companion object {
        /** Request code used for the repeating background update PendingIntent. */
        private const val REQUEST_CODE = 10101

        /** Request code used for the launch-activity PendingIntent. */
        private const val REQUEST_CODE_LAUNCH = 10102

        /**
         * Custom action used to request an immediate widget refresh.
         *
         * The app sends this broadcast after the user changes the selected date/time.
         */
        const val ACTION_UPDATE_WIDGETS = "com.quasarapps.dayssince.widget.ACTION_UPDATE_WIDGETS"

        /** Preference key storing the selected date (ISO-8601 LocalDate). */
        private const val PREF_SELECTED_DATE = "selected_date"

        /** Preference key storing the selected time (ISO-8601 LocalTime). */
        private const val PREF_SELECTED_TIME = "selected_time"

        /**
         * Ask all widget instances to refresh immediately.
         *
         * This is safe to call from the app process after you persist new prefs values.
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, DayOfMonthAppWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGETS
            }
            context.sendBroadcast(intent)
        }
    }
}
