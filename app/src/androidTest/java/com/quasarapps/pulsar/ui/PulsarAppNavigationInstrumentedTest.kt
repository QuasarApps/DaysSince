package com.quasarapps.pulsar.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.MilestonesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end navigation test that drives the real [PulsarApp] — the production NavHost wired to
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
class PulsarAppNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val title = "E2E Marathon"

    @Before
    fun resetStore() {
        // Delete every persisted milestone so we start on the empty state regardless of what else
        // has run on this device.
        val repo = MilestonesRepository(appContext)
        runBlocking { repo.snapshot().forEach { repo.delete(it.id) } }
    }

    @After
    fun tearDownStore() {
        val repo = MilestonesRepository(appContext)
        runBlocking { repo.snapshot().forEach { repo.delete(it.id) } }
    }

    /** Wait out the launch splash overlay (its tagline is unique to it) so it stops intercepting taps. */
    private fun awaitSplashGone() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Count the time").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun addMilestone_fromEmptyState_persistsAndShowsCardOnHome() {
        // Real device: let the clock run. The nav fade transitions and the async DataStore write +
        // recomposition all need real frames to progress; freezing the clock stalls them (and the
        // delay-based minute-tick loop is idle between ticks, so it never blocks waitForIdle).
        composeRule.setContent { PulsarApp() }
        awaitSplashGone()

        // 1. Empty state.
        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()

        // 2. Navigate to the add screen via the "Mark" FAB (the empty state has no separate CTA now).
        composeRule.onNodeWithContentDescription("Add milestone").performClick()
        composeRule.onNodeWithText("New milestone").assertIsDisplayed()

        // 3. Enter a title and save.
        composeRule.onNode(hasSetTextAction()).performTextInput(title)
        composeRule.onNodeWithText("Save").performClick()

        // 4. Back on home, the persisted milestone shows as a card. The "Mark" FAB is always present
        //    now, so the card title is the reliable "populated home" signal — wait for the async
        //    DataStore write to round-trip through the StateFlow and the card to render.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(title).onFirst().assertExists()
        // The milestone was created just now (0 days), so the card shows the "new beginning" kicker.
        composeRule.onNodeWithText("A NEW BEGINNING").assertExists()
    }

    @Test
    fun settingsGear_opensSettingsThenBackReturnsHome() {
        composeRule.setContent { PulsarApp() }
        awaitSplashGone()

        // The gear lives in the home top bar (present in the empty state too).
        composeRule.onNodeWithContentDescription("Settings").performClick()

        // "Theme" is a Settings-only section header, so it's an unambiguous "we're on Settings" signal.
        composeRule.onNodeWithText("Theme").assertIsDisplayed()

        // Back returns to the home empty state.
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()
    }
}
