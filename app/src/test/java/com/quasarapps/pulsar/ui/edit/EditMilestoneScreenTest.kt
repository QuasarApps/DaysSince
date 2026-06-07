package com.quasarapps.pulsar.ui.edit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI test for [EditMilestoneScreen], run on the JVM under Robolectric (no emulator).
 *
 * Covers the screen's contract: it prefills from an existing milestone, distinguishes the new vs.
 * edit header, and hands the (possibly edited) values back through onSave / onCancel.
 *
 * Pinned to en-US so the English-copy and date-field assertions don't depend on the host's default
 * locale (the app now ships translations).
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class EditMilestoneScreenTest {

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
            PulsarTheme {
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
    fun existingMilestone_showsEditHeaderAndPrefillsTitle() {
        setContent(
            existing = Milestone("a", "Sober", sampleDate, sampleTime, accent = 2, createdAt = 1L),
        )

        composeRule.onNodeWithText("Edit milestone").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertTextContains("Sober")
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
        // Fields the user didn't touch pass through unchanged.
        assertEquals(sampleDate, savedDate)
        assertEquals(sampleTime, savedTime)
        assertEquals(2, savedAccent)
    }

    @Test
    fun tappingClose_invokesOnCancel() {
        var cancelled = false
        setContent(onCancel = { cancelled = true })

        composeRule.onNodeWithContentDescription("Cancel").performClick()

        assertTrue(cancelled)
    }
}
