package com.quasarapps.dayssince.widget

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WidgetBroadcastsTest {

    @Test
    fun requestUpdate_sendsBroadcastWithCorrectAction() {
        val context = RuntimeEnvironment.getApplication()

        WidgetBroadcasts.requestUpdate(context, DaysSinceWidgetProvider::class.java)

        val broadcasts = shadowOf(context).broadcastIntents
        val sent = broadcasts.lastOrNull { it.action == WidgetBroadcasts.ACTION_UPDATE_WIDGETS }
        assertNotNull("Expected broadcast with ACTION_UPDATE_WIDGETS", sent)
    }

    @Test
    fun requestUpdate_broadcastTargetsCorrectReceiverClass() {
        val context = RuntimeEnvironment.getApplication()

        WidgetBroadcasts.requestUpdate(context, DaysSinceWidgetProvider::class.java)

        val broadcasts = shadowOf(context).broadcastIntents
        val sent = broadcasts.last { it.action == WidgetBroadcasts.ACTION_UPDATE_WIDGETS }
        assertEquals(
            DaysSinceWidgetProvider::class.java.name,
            sent.component?.className
        )
    }

    @Test
    fun requestUpdate_dhm_sendsBroadcastTargetingDhmProvider() {
        val context = RuntimeEnvironment.getApplication()

        WidgetBroadcasts.requestUpdate(context, DaysHoursMinutesSinceWidgetProvider::class.java)

        val broadcasts = shadowOf(context).broadcastIntents
        val sent = broadcasts.last { it.action == WidgetBroadcasts.ACTION_UPDATE_WIDGETS }
        assertEquals(
            DaysHoursMinutesSinceWidgetProvider::class.java.name,
            sent.component?.className
        )
    }

    @Test
    fun actionUpdateWidgets_constantValue_isStable() {
        // Guard against accidental renaming of the broadcast action constant,
        // which would break widgets that are already installed on user devices.
        assertEquals(
            "com.quasarapps.dayssince.widget.ACTION_UPDATE_WIDGETS",
            WidgetBroadcasts.ACTION_UPDATE_WIDGETS
        )
    }
}
