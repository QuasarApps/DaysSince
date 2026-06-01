package com.quasarapps.dayssince.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [WidgetConfigActivity] — the activity Android launches when a widget is
 * placed. Verifies the invalid-id guard (cancel + finish) and that a valid launch renders the
 * milestone picker.
 *
 * Uses [createEmptyComposeRule] (which does not launch its own activity) so we can drive an
 * activity launched with a custom configuration intent via [ActivityScenario].
 */
@RunWith(AndroidJUnit4::class)
class WidgetConfigActivityInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun launchedWithoutWidgetId_cancelsAndFinishesImmediately() {
        // No EXTRA_APPWIDGET_ID -> resolves to INVALID_APPWIDGET_ID -> RESULT_CANCELED + finish().
        val intent = Intent(context, WidgetConfigActivity::class.java)

        ActivityScenario.launchActivityForResult<WidgetConfigActivity>(intent).use { scenario ->
            assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
            val returnedId = scenario.result.resultData
                ?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -99)
            assertEquals(AppWidgetManager.INVALID_APPWIDGET_ID, returnedId)
        }
    }

    @Test
    fun launchedWithValidWidgetId_showsMilestonePicker() {
        val intent = Intent(context, WidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123)

        ActivityScenario.launch<WidgetConfigActivity>(intent).use {
            composeRule.onNodeWithText("Choose a milestone").assertIsDisplayed()
        }
    }
}
