package com.quasarapps.pulsar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
 * Compose UI test for [HomeScreen], run on the JVM under Robolectric (no emulator).
 *
 * Mirrors the on-device coverage for fast CI feedback: empty-state vs. populated-list branches and
 * the add / open click contracts. The "Mark" FAB is always present now (it's the only add path;
 * the empty state no longer has a separate CTA button).
 *
 * Pinned to en-US so the English-copy assertions don't depend on the host's default locale.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class HomeScreenTest {

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
        // The milestone cards' rememberElapsedDhm runs an infinite delay loop; under Robolectric the
        // virtual clock would advance through it forever, so freeze it and waitForIdle settles.
        composeRule.mainClock.autoAdvance = false
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
        // The FAB is always available — it's the way to add the first milestone.
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

        composeRule.onNodeWithText("Sober").assertIsDisplayed()
        composeRule.onNodeWithText("Gym streak").assertIsDisplayed()
        // The FAB merges its children for TalkBack, so its "Mark" label is only in the unmerged tree.
        composeRule.onNodeWithText("Mark", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun fab_hasAccessibilityLabel_forTalkBack() {
        // The FAB merges the decorative star + "Mark" text into one node with an explicit
        // contentDescription, so TalkBack announces a single labeled button.
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

        composeRule.onNodeWithContentDescription("Add milestone").performClick()

        assertTrue(added)
    }
}
