package com.quasarapps.pulsar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI test for [HomeScreen] on a device/emulator.
 *
 * Covers the empty-state vs. populated-list branches and the click contracts (add / open).
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun milestone(id: String, title: String) = Milestone(
        id = id,
        title = title,
        date = LocalDate.of(2025, 1, 1),
        time = LocalTime.of(9, 0),
        accent = 1,
        createdAt = 1L,
    )

    private fun setContent(
        milestones: List<Milestone>,
        onAdd: () -> Unit = {},
        onOpen: (String) -> Unit = {},
    ) {
        // Note: we deliberately do NOT freeze the test clock here. The milestone cards'
        // rememberElapsedDhm loop is delay-based (it suspends between minute ticks, which counts as
        // idle), so on a real device waitForIdle settles fine. Freezing the clock instead stalls
        // layout/animation and makes nodes report as not-displayed.
        composeRule.setContent {
            PulsarTheme {
                HomeScreen(milestones = milestones, onAdd = onAdd, onOpen = onOpen)
            }
        }
    }

    @Test
    fun emptyState_showsPromptAndCta() {
        setContent(milestones = emptyList())

        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add your first milestone").assertIsDisplayed()
    }

    @Test
    fun emptyState_hidesFab() {
        setContent(milestones = emptyList())

        // The "New" FAB is gated behind milestones.isNotEmpty(); it must be absent on the empty
        // state (the CTA button is the only way to add the first milestone).
        composeRule.onNodeWithText("New", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun emptyState_ctaInvokesOnAdd() {
        var added = false
        setContent(milestones = emptyList(), onAdd = { added = true })

        composeRule.onNodeWithText("Add your first milestone").performClick()

        assertTrue(added)
    }

    @Test
    fun populatedList_rendersTitlesAndFab() {
        setContent(milestones = listOf(milestone("a", "Sober"), milestone("b", "Gym streak")))

        composeRule.onNodeWithText("Sober").assertIsDisplayed()
        composeRule.onNodeWithText("Gym streak").assertIsDisplayed()
        // The "New" FAB only appears once there is at least one milestone. Its label lives in the
        // unmerged tree: ExtendedFloatingActionButton does not merge its text into the button's
        // semantics node on this Compose version, so the default (merged) lookup can't see it.
        composeRule.onNodeWithText("New", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun fab_hasAccessibilityLabel_forTalkBack() {
        // Regression guard for the a11y fix: because the FAB's "New" text doesn't merge into the
        // button's semantics node, an explicit contentDescription is set so TalkBack doesn't
        // announce an unlabeled "Button".
        setContent(milestones = listOf(milestone("a", "Sober")))

        composeRule.onNodeWithContentDescription("Add milestone").assertIsDisplayed()
    }

    @Test
    fun tappingCard_invokesOnOpenWithThatMilestoneId() {
        var opened: String? = null
        setContent(
            milestones = listOf(milestone("abc", "Sober")),
            onOpen = { opened = it },
        )

        composeRule.onNodeWithText("Sober").performClick()

        assertEquals("abc", opened)
    }

    @Test
    fun tappingFab_invokesOnAdd() {
        var added = false
        setContent(milestones = listOf(milestone("a", "Sober")), onAdd = { added = true })

        // See populatedList_rendersTitlesAndFab: the FAB label is only in the unmerged tree.
        composeRule.onNodeWithText("New", useUnmergedTree = true).performClick()

        assertTrue(added)
    }
}
