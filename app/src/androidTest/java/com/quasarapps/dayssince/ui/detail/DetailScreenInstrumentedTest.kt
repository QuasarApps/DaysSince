package com.quasarapps.dayssince.ui.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
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
        // CountUpNumber and rememberElapsedDhm both animate / loop; freeze the clock so the test
        // settles. (The animated day count stays at its start value, so assertions avoid it.)
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            DaysSinceTheme(dynamicColor = false) {
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

        composeRule.onNodeWithText("Sober").assertIsDisplayed()
        composeRule.onNodeWithText("DAYS").assertIsDisplayed()
        composeRule.onNodeWithText("since 15th of June 2025", substring = true).assertIsDisplayed()
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
        composeRule.onNodeWithText("Delete").performClick()

        // The confirmation dialog appears; deletion only fires after confirming.
        composeRule.onNodeWithText("Delete milestone?").assertIsDisplayed()
        assertTrue(!deleted)

        composeRule.onNodeWithText("Delete").performClick()
        assertTrue(deleted)
    }

    @Test
    fun deleteFlow_cancelDismissesWithoutDeleting() {
        var deleted = false
        setContent(onDelete = { deleted = true })

        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        assertTrue(!deleted)
    }
}
