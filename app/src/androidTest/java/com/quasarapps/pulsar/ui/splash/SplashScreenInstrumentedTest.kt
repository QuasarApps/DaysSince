package com.quasarapps.pulsar.ui.splash

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test for [SplashScreen] on a device/emulator.
 *
 * Covers the brand lockup rendering and that the screen signals completion after its entrance + hold
 * (real-device timing, so the delay-based finish is reliable).
 */
@RunWith(AndroidJUnit4::class)
class SplashScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun rendersBrandLockupThenFinishes() {
        var finished = false
        composeRule.setContent {
            PulsarTheme {
                SplashScreen(onFinished = { finished = true })
            }
        }

        // Resolve copy from resources so the test isn't pinned to one locale / literal copy.
        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.splash_tagline)).assertExists()

        // After the entrance + hold, the splash signals completion so the host can dismiss it.
        composeRule.waitUntil(timeoutMillis = 5_000) { finished }
        assertTrue(finished)
    }
}
