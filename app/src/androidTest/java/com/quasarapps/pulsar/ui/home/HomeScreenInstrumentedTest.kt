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
 * Covers the empty-state vs. populated-list branches and the click contracts (add / open). The
 * "Mark" FAB is always present (the empty state has no separate CTA button).
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
        // idle), so on a real device waitForIdle settles fine.
        composeRule.setContent {
            PulsarTheme {
                HomeScreen(milestones = milestones, onAdd = onAdd, onOpen = onOpen)
            }
        }
    }

    @Test
    fun emptyState_showsPromptAndFab() {
        setContent(milestones = emptyList())

        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Add milestone").assertIsDisplayed()
    }

    @Test
    fun emptyState_fabInvokesOnAdd() {
        var added = false
        setContent(milestones = emptyList(), onAdd = { added = true })

        composeRule.onNodeWithContentDescription("Add milestone").performClick()

        assertTrue(added)
    }

    @Test
    fun populatedList_rendersTitlesAndFab() {
        setContent(milestones = listOf(milestone("a", "Sober"), milestone("b", "Gym streak")))

        // Each card now merges its number/kicker/title into one labeled node, so the title Text is
        // only in the unmerged tree.
        composeRule.onNodeWithText("Sober", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Gym streak", useUnmergedTree = true).assertIsDisplayed()
        // The FAB merges its children for TalkBack, so its "Mark" label is only in the unmerged tree.
        composeRule.onNodeWithText("Mark", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun card_mergesIntoLabeledNode_forTalkBack() {
        setContent(milestones = listOf(milestone("a", "Sober")))

        // The number / kicker / title merge into one node whose contentDescription leads with the
        // title, so TalkBack announces a single labeled "Sober, N days" button per card.
        composeRule.onNodeWithContentDescription("Sober", substring = true).assertIsDisplayed()
    }

    @Test
    fun newBeginningCard_announcesCelebratoryStateNotZeroDays() {
        setContent(
            milestones = listOf(
                // 0 days today (midnight is in the past, not the future) -> the "new beginning" state.
                Milestone(
                    id = "n",
                    title = "Fresh start",
                    date = LocalDate.now(),
                    time = LocalTime.MIDNIGHT,
                    accent = 1,
                    createdAt = 1L,
                ),
            ),
        )

        // The card's spoken label reflects the new-beginning kicker rather than "0 days".
        composeRule.onNodeWithContentDescription("A NEW BEGINNING", substring = true).assertIsDisplayed()
    }

    @Test
    fun fab_hasAccessibilityLabel_forTalkBack() {
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

        // The merged card node carries the contentDescription ("Sober, N days"); click it by that.
        composeRule.onNodeWithContentDescription("Sober", substring = true).performClick()

        assertEquals("abc", opened)
    }

    @Test
    fun tappingFab_invokesOnAdd() {
        var added = false
        setContent(milestones = listOf(milestone("a", "Sober")), onAdd = { added = true })

        composeRule.onNodeWithContentDescription("Add milestone").performClick()

        assertTrue(added)
    }
}
