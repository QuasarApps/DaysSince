package com.quasarapps.dayssince.ui.edit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.ui.theme.DaysSinceTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI test for [EditMilestoneScreen] on a device/emulator.
 *
 * Covers the screen's contract: new vs. edit header, prefill from an existing milestone, the accent
 * prefill, and handing the (possibly edited) values back through onSave / onCancel.
 */
@RunWith(AndroidJUnit4::class)
class EditMilestoneScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleDate = LocalDate.of(2025, 6, 15)
    private val sampleTime = LocalTime.of(9, 30)

    private fun setContent(
        existing: Milestone? = null,
        onSave: (String, LocalDate, LocalTime, Int) -> Unit = { _, _, _, _ -> },
        onCancel: () -> Unit = {},
    ) {
        // The preview strip's rememberElapsedDhm runs an infinite delay loop; freeze the clock so
        // waitForIdle settles instead of advancing time forever.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            DaysSinceTheme(dynamicColor = false) {
                EditMilestoneScreen(existing = existing, onSave = onSave, onCancel = onCancel)
            }
        }
    }

    @Test
    fun newMilestone_showsNewHeader() {
        setContent(existing = null)
        composeRule.onNodeWithText("New milestone").assertIsDisplayed()
    }

    @Test
    fun existingMilestone_showsEditHeaderAndPrefillsTitleAndAccent() {
        setContent(
            existing = Milestone("a", "Sober", sampleDate, sampleTime, accent = 2, createdAt = 1L),
        )

        composeRule.onNodeWithText("Edit milestone").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertTextContains("Sober")
        // Accent index 2 is "Violet"; the selected swatch surfaces its label as a check icon
        // content description, so this confirms the accent prefilled correctly.
        composeRule.onNodeWithContentDescription("Violet").assertIsDisplayed()
        // The picked date is shown in the Date field.
        composeRule.onNodeWithText("15th of June 2025").assertIsDisplayed()
    }

    @Test
    fun editingTitleThenSaving_passesEditedTitleAndUntouchedFields() {
        var savedTitle: String? = null
        var savedDate: LocalDate? = null
        var savedTime: LocalTime? = null
        var savedAccent = -1
        setContent(
            existing = Milestone("a", "Old", sampleDate, sampleTime, accent = 2, createdAt = 1L),
            onSave = { title, date, time, accent ->
                savedTitle = title
                savedDate = date
                savedTime = time
                savedAccent = accent
            },
        )

        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("Gym streak")
        composeRule.onNodeWithText("Save").performClick()

        assertEquals("Gym streak", savedTitle)
        assertEquals(sampleDate, savedDate)
        assertEquals(sampleTime, savedTime)
        assertEquals(2, savedAccent)
    }

    @Test
    fun newMilestone_saveImmediately_passesBlankTitleThrough() {
        // The screen hands back the raw title; the "Milestone" fallback is the view model's job.
        var savedTitle: String? = null
        setContent(onSave = { title, _, _, _ -> savedTitle = title })

        composeRule.onNodeWithText("Save").performClick()

        assertEquals("", savedTitle)
    }

    @Test
    fun tappingClose_invokesOnCancel() {
        var cancelled = false
        setContent(onCancel = { cancelled = true })

        composeRule.onNodeWithContentDescription("Cancel").performClick()

        assertTrue(cancelled)
    }
}
