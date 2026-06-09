package com.quasarapps.pulsar.ui.splash

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Compose UI test for [SplashScreen], run on the JVM under Robolectric (no emulator).
 *
 * Asserts the brand lockup renders. The entrance animation + hold/finish timing is exercised on a
 * real device in [SplashScreenInstrumentedTest] (delay-based timing is reliable there).
 *
 * Pinned to en-US so the wordmark/tagline copy doesn't depend on the host's default locale.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class SplashScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersWordmarkAndTagline() {
        // Freeze the clock: the entrance animation would otherwise keep the frame clock busy. Frozen,
        // the content is laid out at its first frame and waitForIdle settles.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PulsarTheme {
                SplashScreen(onFinished = {})
            }
        }

        // Resolve copy from resources so the test stays correct if the wordmark/tagline text changes.
        val res = RuntimeEnvironment.getApplication()
        composeRule.onNodeWithText(res.getString(R.string.app_name)).assertExists()
        composeRule.onNodeWithText(res.getString(R.string.splash_tagline)).assertExists()
    }
}
