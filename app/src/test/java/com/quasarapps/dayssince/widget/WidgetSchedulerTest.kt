package com.quasarapps.dayssince.widget

import android.app.AlarmManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WidgetSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Clear any alarms left over from previous tests
        shadowOf(alarmManager).scheduledAlarms.clear()
    }

    @Test
    fun scheduleInexactRepeating_registersExactlyOneAlarm() {
        WidgetScheduler.scheduleInexactRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
            intervalMs = AlarmManager.INTERVAL_HOUR,
            wakeup = true
        )

        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun scheduleInexactRepeating_calledTwice_doesNotDuplicateAlarm() {
        repeat(2) {
            WidgetScheduler.scheduleInexactRepeating(
                context = context,
                receiverClass = DaysSinceWidgetProvider::class.java,
                requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
                action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
                intervalMs = AlarmManager.INTERVAL_HOUR,
                wakeup = true
            )
        }

        // FLAG_UPDATE_CURRENT cancels + replaces — should still be exactly one alarm
        assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun cancelRepeating_removesScheduledAlarm() {
        WidgetScheduler.scheduleInexactRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
            intervalMs = AlarmManager.INTERVAL_HOUR,
            wakeup = true
        )

        WidgetScheduler.cancelRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS
        )

        assertEquals(0, shadowOf(alarmManager).scheduledAlarms.size)
    }

    @Test
    fun scheduleInexactRepeating_scheduledAlarmHasCorrectAction() {
        WidgetScheduler.scheduleInexactRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS,
            intervalMs = AlarmManager.INTERVAL_HOUR,
            wakeup = true
        )

        val alarm = shadowOf(alarmManager).scheduledAlarms.firstOrNull()
        assertNotNull(alarm)
        val intent = shadowOf(alarm!!.operation).savedIntent
        assertEquals(WidgetBroadcasts.ACTION_UPDATE_WIDGETS, intent.action)
    }

    @Test
    fun cancelRepeating_whenNothingScheduled_doesNotCrash() {
        // Should complete without throwing
        WidgetScheduler.cancelRepeating(
            context = context,
            receiverClass = DaysSinceWidgetProvider::class.java,
            requestCode = WidgetRequestCodes.ALARM_DAYS_SINCE,
            action = WidgetBroadcasts.ACTION_UPDATE_WIDGETS
        )

        assertNull(shadowOf(alarmManager).scheduledAlarms.firstOrNull())
    }
}
