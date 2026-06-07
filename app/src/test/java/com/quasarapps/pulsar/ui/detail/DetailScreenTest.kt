package com.quasarapps.pulsar.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI test for [DetailScreen], run on the JVM under Robolectric (no emulator).
 *
 * Mirrors the on-device coverage for fast CI feedback: the missing-milestone fallback, the
 * hero/footer content, and the overflow-menu actions including the delete confirmation gate.
 *
 * Pinned to en-US: the assertions check English copy and en-US long dates (e.g. "June 15, 2025"),
 * which the screen formats from the active configuration locale. Without this the test would depend
 * on the host's default locale and break wherever that isn't English.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class DetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sample = Milestone(
        id = "a",
        title = "Sober",
        date = LocalDate.of(2025, 6, 15),
        time = LocalTime.of(9, 30),
        accent = 2,
        createdAt = 1L,
    )

    private fun setContent(
        milestone: Milestone? = sample,
        onBack: () -> Unit = {},
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        // CountUpNumber animates and rememberElapsedDhm loops forever; freeze the virtual clock so
        // the test settles under Robolectric. Transitions are then driven explicitly via
        // settleTransitions() where a popup needs to finish animating.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PulsarTheme(dynamicColor = false) {
                DetailScreen(milestone = milestone, onBack = onBack, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }

    /**
     * Advances the frozen clock enough to finish a dropdown/dialog transition. A single 400ms call
     * stays under the milestone "tick every minute" effect's minimum 1s delay, so one call won't
     * wake that loop. (Tests that call this several times do cross 1s cumulatively and let the tick
     * fire — harmless here, since none of these tests assert on the elapsed count.)
     */
    private fun settleTransitions() {
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.waitForIdle()
    }

    @Test
    fun nullMilestone_showsMissingStateAndBackAction() {
        var backed = false
        setContent(milestone = null, onBack = { backed = true })

        composeRule.onNodeWithText("This milestone is no longer available.").assertIsDisplayed()
        composeRule.onNodeWithText("Go back").performClick()

        assertTrue(backed)
    }

    @Test
    fun rendersTitleAndSinceLine() {
        setContent()

        composeRule.onNodeWithText("Sober").assertIsDisplayed()
        composeRule.onNodeWithText("DAYS").assertIsDisplayed()
        composeRule.onNodeWithText("since June 15, 2025", substring = true).assertIsDisplayed()
    }

    @Test
    fun backButton_invokesOnBack() {
        var backed = false
        setContent(onBack = { backed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backed)
    }

    @Test
    fun overflowMenu_editInvokesOnEdit() {
        var edited = false
        setContent(onEdit = { edited = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        settleTransitions()
        composeRule.onNodeWithText("Edit").performClick()

        assertTrue(edited)
    }

    @Test
    fun deleteIsGatedBehindAConfirmationDialog() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        settleTransitions()
        composeRule.onNodeWithText("Delete").performClick()
        settleTransitions()

        // Choosing "Delete" from the menu opens a confirmation dialog and must NOT delete yet — the
        // safety gate. (The full confirm -> onDelete path is exercised on a real device in
        // DetailScreenInstrumentedTest, where stacked-popup button clicks are reliable.)
        composeRule.onNodeWithText("Delete milestone?").assertIsDisplayed()
        assertTrue("delete must not fire until the user confirms", !deleted)
    }

    @Test
    fun deleteFlow_cancelDismissesWithoutDeleting() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        settleTransitions()
        composeRule.onNodeWithText("Delete").performClick()
        settleTransitions()
        composeRule.onNodeWithText("Cancel").performClick()
        settleTransitions()

        composeRule.onNodeWithText("Delete milestone?").assertDoesNotExist()
        assertTrue(!deleted)
    }
}
