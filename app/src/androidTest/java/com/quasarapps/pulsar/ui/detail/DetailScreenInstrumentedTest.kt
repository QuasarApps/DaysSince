package com.quasarapps.pulsar.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
 * (edit, and delete behind a confirmation dialog).
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
        onBack: () -> Unit = {},
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        // We don't freeze the clock (real-device tests): CountUpNumber's animation is finite and
        // rememberElapsedDhm's loop is delay-based, so waitForIdle settles. Assertions avoid the
        // animated day count and target static labels/titles instead.
        composeRule.setContent {
            PulsarTheme(dynamicColor = false) {
                DetailScreen(milestone = milestone, onBack = onBack, onEdit = onEdit, onDelete = onDelete)
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
    fun deleteFlow_requiresConfirmation_thenInvokesOnDelete() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        // Click the menu's "Delete" (unique: the dialog isn't open yet).
        composeRule.onNodeWithText("Delete").performClick()

        // The confirmation dialog appears; deletion only fires after confirming.
        composeRule.onNodeWithText("Delete milestone?").assertIsDisplayed()
        assertTrue(!deleted)

        // The dropdown's "Delete" item and the dialog's "Delete" confirm button briefly coexist
        // while the menu animates out. Wait until exactly one "Delete" remains so the click is
        // unambiguous on a slow emulator (see issue #24) before pressing the confirm button.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Delete").fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithText("Delete").performClick()
        assertTrue(deleted)
    }

    @Test
    fun deleteFlow_cancelDismissesWithoutDeleting() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Delete milestone?").assertIsDisplayed()

        composeRule.onNodeWithText("Cancel").performClick()

        // Cancel dismisses the dialog and deletes nothing.
        composeRule.onNodeWithText("Delete milestone?").assertDoesNotExist()
        assertTrue(!deleted)
    }
}
