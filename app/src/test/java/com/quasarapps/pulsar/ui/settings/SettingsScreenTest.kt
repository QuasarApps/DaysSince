package com.quasarapps.pulsar.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quasarapps.pulsar.data.Settings
import com.quasarapps.pulsar.data.ThemeMode
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI test for [SettingsScreen], run on the JVM under Robolectric (no emulator).
 *
 * Covers the screen's contract: it renders the theme options + About, marks the current theme as
 * selected, and hands edits back through onSetThemeMode / onToggleUnits / onBack.
 *
 * Pinned to en-US so the English-copy assertions don't depend on the host's default locale.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        settings: Settings = Settings(),
        onSetThemeMode: (ThemeMode) -> Unit = {},
        onToggleUnits: (Boolean) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            PulsarTheme {
                SettingsScreen(
                    settings = settings,
                    onSetThemeMode = onSetThemeMode,
                    onToggleUnits = onToggleUnits,
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun rendersThemeOptionsAndAbout() {
        setContent()

        composeRule.onNodeWithText("System default").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        // The About section is at the bottom of a scrollable column and can sit below the fold on a
        // small viewport, so assert it's rendered/wired rather than its viewport position.
        composeRule.onNodeWithText("Made by Quasar Apps").assertExists()
    }

    @Test
    fun currentThemeMode_isMarkedSelected() {
        setContent(settings = Settings(themeMode = ThemeMode.DARK))

        // Every option is a labeled radio; the active one carries the selected state.
        composeRule.onNodeWithText("Dark").assertIsSelected()
    }

    @Test
    fun selectingLight_invokesCallbackWithLight() {
        var picked: ThemeMode? = null
        setContent(onSetThemeMode = { picked = it })

        composeRule.onNodeWithText("Light").performClick()

        assertEquals(ThemeMode.LIGHT, picked)
    }

    @Test
    fun togglingUnitsRow_invokesCallbackWithFlippedValue() {
        var toggledTo: Boolean? = null
        setContent(settings = Settings(showUnits = true), onToggleUnits = { toggledTo = it })

        // The whole row is toggleable, so tapping the label flips the switch.
        composeRule.onNodeWithText("Show hours & minutes").performClick()

        assertEquals(false, toggledTo)
    }

    @Test
    fun backButton_invokesOnBack() {
        var backed = false
        setContent(onBack = { backed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backed)
    }
}
