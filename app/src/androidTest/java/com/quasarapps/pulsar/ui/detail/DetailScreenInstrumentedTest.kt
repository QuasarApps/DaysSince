package com.quasarapps.pulsar.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI test for [DetailScreen] on a device/emulator.
 *
 * Covers the missing-milestone fallback, the hero/footer content, and the overflow-menu actions
 * (edit, reset behind a confirmation dialog, and immediate delete — undo is offered by the caller).
 */
@RunWith(AndroidJUnit4::class)
class DetailScreenInstrumentedTest {

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
        showUnits: Boolean = true,
        onBack: () -> Unit = {},
        onEdit: () -> Unit = {},
        onReset: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        // We don't freeze the clock (real-device tests): CountUpNumber's animation is finite and the
        // elapsed loop is delay-based, so waitForIdle settles. Assertions avoid the animated day
        // count and target static labels/titles instead.
        composeRule.setContent {
            PulsarTheme {
                DetailScreen(
                    milestone = milestone,
                    showUnits = showUnits,
                    onBack = onBack,
                    onEdit = onEdit,
                    onReset = onReset,
                    onDelete = onDelete,
                )
            }
        }
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

        // "DAYS" sits in the vertically-centered hero, so it's on-screen in any orientation.
        composeRule.onNodeWithText("DAYS").assertIsDisplayed()
        // The title and since-line live in the bottom footer, which can clip below the fold in
        // landscape. This test is about the content being rendered/wired correctly, not its
        // viewport position, so assert existence rather than display.
        composeRule.onNodeWithText("Sober").assertExists()
        composeRule.onNodeWithText("since June 15, 2025", substring = true).assertExists()
    }

    @Test
    fun showUnitsFalse_hidesUnitsBreakdown_butKeepsDayCount() {
        setContent(showUnits = false)

        // The day count stays; the Settings opt-out removes only the H/M/S breakdown.
        composeRule.onNodeWithText("DAYS").assertIsDisplayed()
        composeRule.onNodeWithText("HOURS").assertDoesNotExist()
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
        composeRule.onNodeWithText("Edit").performClick()

        assertTrue(edited)
    }

    @Test
    fun resetFlow_requiresConfirmation_thenInvokesOnReset() {
        var reset = false
        setContent(onReset = { reset = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Reset to now").performClick()

        // The confirmation dialog appears; reset only fires after confirming.
        composeRule.onNodeWithText("Reset to now?").assertIsDisplayed()
        assertTrue(!reset)

        // The confirm button ("Reset") is distinct from the menu item ("Reset to now"), so there's
        // no coexistence ambiguity while the menu animates out.
        composeRule.onNodeWithText("Reset").performClick()
        assertTrue(reset)
    }

    @Test
    fun deleteMenuItem_invokesOnDeleteImmediately() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        // Delete fires immediately — undo is offered via a snackbar by the caller, so there's no
        // confirmation dialog (unlike reset).
        assertTrue(deleted)
        composeRule.onNodeWithText("Delete milestone?").assertDoesNotExist()
    }
}
