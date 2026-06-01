package com.quasarapps.dayssince.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.Prefs
import com.quasarapps.dayssince.data.MilestonesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end navigation test that drives the real [DaysSinceApp] — the production NavHost wired to
 * a real [MilestonesViewModel] and the production DataStore — on a device/emulator.
 *
 * It walks the primary user journey: empty state -> add a milestone -> see it persisted as a card
 * back on the home screen. The production store is reset before and after so the run is isolated.
 *
 * Clock handling: several screens run infinite "tick every minute" effects and count-up animations.
 * The test clock is frozen ([mainClock.autoAdvance] = false) so `waitForIdle` settles; it is then
 * nudged forward by less than one tick interval after navigations to let the fade transitions
 * finish without re-arming those effects. Async DataStore writes are awaited with [waitUntil].
 */
@RunWith(AndroidJUnit4::class)
class DaysSinceAppNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val title = "E2E Marathon"

    @Before
    fun resetStore() {
        // Drop any legacy single-counter prefs and every persisted milestone so we start on the
        // empty state regardless of what else has run on this device.
        Prefs.get(appContext).edit().clear().commit()
        val repo = MilestonesRepository(appContext)
        runBlocking { repo.snapshot().forEach { repo.delete(it.id) } }
    }

    @After
    fun tearDownStore() {
        val repo = MilestonesRepository(appContext)
        runBlocking { repo.snapshot().forEach { repo.delete(it.id) } }
    }

    @Test
    fun addMilestone_fromEmptyState_persistsAndShowsCardOnHome() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent { DaysSinceApp() }
        composeRule.waitForIdle()

        // 1. Empty state.
        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()

        // 2. Navigate to the add screen.
        composeRule.onNodeWithText("Add your first milestone").performClick()
        composeRule.mainClock.advanceTimeBy(750) // let the fade transition complete
        composeRule.waitForIdle()
        composeRule.onNodeWithText("New milestone").assertIsDisplayed()

        // 3. Enter a title and save.
        composeRule.onNode(hasSetTextAction()).performTextInput(title)
        composeRule.onNodeWithText("Save").performClick()
        composeRule.mainClock.advanceTimeBy(750)

        // 4. Back on home, the persisted milestone shows as a card (the "New" FAB only renders once
        //    at least one milestone exists, so it's a reliable "populated home" signal).
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("New").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(title).onFirst().assertExists()
        composeRule.onNodeWithText("DAYS SINCE").assertExists()
    }
}
