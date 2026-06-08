package com.quasarapps.pulsar.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.Settings
import com.quasarapps.pulsar.data.ThemeMode
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test for [SettingsScreen] on a device/emulator.
 *
 * Covers rendering, the prefilled-selection state, and the theme/units/back callbacks.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenInstrumentedTest {

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
    fun rendersThemeOptions() {
        setContent()

        composeRule.onNodeWithText("System default").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun prefilledThemeMode_isMarkedSelected() {
        setContent(settings = Settings(themeMode = ThemeMode.LIGHT))

        composeRule.onNodeWithText("Light").assertIsSelected()
    }

    @Test
    fun selectingDark_invokesCallbackWithDark() {
        var picked: ThemeMode? = null
        setContent(onSetThemeMode = { picked = it })

        composeRule.onNodeWithText("Dark").performClick()

        assertEquals(ThemeMode.DARK, picked)
    }

    @Test
    fun togglingUnitsRow_invokesCallback() {
        var toggledTo: Boolean? = null
        setContent(settings = Settings(showUnits = true), onToggleUnits = { toggledTo = it })

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
