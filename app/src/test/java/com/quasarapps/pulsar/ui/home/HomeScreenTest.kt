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
 * Mirrors the on-device coverage for fast CI feedback: empty-state vs. populated-list branches,
 * the FAB gating, and the add / open click contracts.
 *
 * Pinned to en-US so the English-copy assertions don't depend on the host's default locale (the app
 * now ships translations, so a non-English default would otherwise load localized strings).
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
            PulsarTheme(dynamicColor = false) {
                HomeScreen(milestones = milestones, onAdd = onAdd, onOpen = onOpen)
            }
        }
    }

    @Test
    fun emptyState_showsPromptAndHidesFab() {
        setContent(milestones = emptyList())

        composeRule.onNodeWithText("No milestones yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add your first milestone").assertIsDisplayed()
        // The FAB is gated behind a non-empty list.
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
        // ExtendedFloatingActionButton doesn't merge its text into the button node, so the label
        // is only reachable via the unmerged tree.
        composeRule.onNodeWithText("New", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun fab_hasAccessibilityLabel_forTalkBack() {
        // Regression guard for the a11y fix: the FAB's "New" text doesn't merge into the button's
        // semantics node, so an explicit contentDescription keeps it from being an unlabeled
        // "Button" for TalkBack.
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

        composeRule.onNodeWithText("New", useUnmergedTree = true).performClick()

        assertTrue(added)
    }
}
