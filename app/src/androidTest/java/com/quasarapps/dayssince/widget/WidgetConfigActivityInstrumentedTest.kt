package com.quasarapps.dayssince.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.data.MilestonesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Instrumented tests for [WidgetConfigActivity] — the activity Android launches when a widget is
 * placed. Verifies the invalid-id guard (cancel + finish) and, crucially, that picking a milestone
 * actually binds it to this widget id in the real repository — the activity's whole purpose.
 *
 * Uses [createEmptyComposeRule] (which does not launch its own activity) so we can drive an activity
 * launched with a custom configuration intent via [ActivityScenario].
 */
@RunWith(AndroidJUnit4::class)
class WidgetConfigActivityInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repo = MilestonesRepository(context)

    private companion object {
        const val WIDGET_ID = 4242
        const val SEED_ID = "seed-config-test"
        const val SEED_TITLE = "Run Streak"
    }

    @Before
    fun seedStore() {
        runBlocking {
            repo.snapshot().forEach { repo.delete(it.id) }
            repo.unbindWidget(WIDGET_ID)
            repo.upsert(
                Milestone(
                    id = SEED_ID,
                    title = SEED_TITLE,
                    date = LocalDate.of(2025, 1, 1),
                    time = LocalTime.of(9, 0),
                    accent = 1,
                    createdAt = 1L,
                ),
            )
        }
    }

    @After
    fun clearStore() {
        runBlocking {
            repo.unbindWidget(WIDGET_ID)
            repo.snapshot().forEach { repo.delete(it.id) }
        }
    }

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
    fun pickingMilestone_bindsItToTheWidget() {
        val intent = Intent(context, WidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WIDGET_ID)

        ActivityScenario.launch<WidgetConfigActivity>(intent).use {
            // The picker lists milestones from the real repository (proves it reads persisted data).
            composeRule.onNodeWithText("Choose a milestone").assertExists()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithText(SEED_TITLE).fetchSemanticsNodes().isNotEmpty()
            }

            // Tapping the row binds this widget id to that milestone (and finishes the activity).
            composeRule.onNodeWithText(SEED_TITLE).performClick()

            composeRule.waitUntil(timeoutMillis = 10_000) {
                runBlocking { repo.bindingForWidget(WIDGET_ID)?.milestoneId } == SEED_ID
            }
        }

        // The binding is persisted with the default (opaque) background.
        val binding = runBlocking { repo.bindingForWidget(WIDGET_ID) }
        assertEquals(SEED_ID, binding?.milestoneId)
        assertEquals(false, binding?.transparent)
    }
}
