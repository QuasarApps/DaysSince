package com.quasarapps.pulsar.localization

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.home.HomeScreen
import com.quasarapps.pulsar.ui.theme.PulsarTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

/**
 * End-to-end check that the UI actually renders translated copy (and locale-formatted dates) when
 * the active locale changes — i.e. that `stringResource`, `<plurals>` and [LocalConfiguration]-driven
 * date formatting all resolve against the per-app locale, not just the device default.
 *
 * The screens read strings via `LocalContext` and the date locale via `LocalConfiguration`, so the
 * helper overrides BOTH composition locals with a configuration-context for the target locale.
 */
@RunWith(AndroidJUnit4::class)
class LocalizationInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setLocalizedContent(locale: Locale, content: @Composable () -> Unit) {
        composeRule.setContent {
            val base = LocalContext.current
            val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
            val localizedContext = base.createConfigurationContext(config)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides config,
            ) {
                PulsarTheme { content() }
            }
        }
    }

    private fun emptyHome(locale: Locale) = setLocalizedContent(locale) {
        HomeScreen(milestones = emptyList(), onAdd = {}, onOpen = {})
    }

    @Test
    fun emptyState_spanish_showsTranslatedCopy() {
        emptyHome(Locale("es"))

        composeRule.onNodeWithText("Aún no hay hitos").assertIsDisplayed()
        // Guards against a silent fallback to the English baseline.
        composeRule.onNodeWithText("No milestones yet").assertDoesNotExist()
    }

    @Test
    fun emptyState_russian_showsCyrillicCopy() {
        emptyHome(Locale("ru"))

        composeRule.onNodeWithText("Пока нет событий").assertIsDisplayed()
    }

    @Test
    fun emptyState_arabic_showsRtlCopy() {
        emptyHome(Locale("ar"))

        composeRule.onNodeWithText("لا توجد أحداث بعد").assertIsDisplayed()
    }

    @Test
    fun milestoneCard_spanish_rendersTranslatedCaption() {
        val milestone = Milestone(
            id = "a",
            title = "Sobriedad",
            date = LocalDate.of(2025, 6, 15),
            time = LocalTime.of(9, 0),
            accent = 1,
            createdAt = 1L,
        )
        setLocalizedContent(Locale("es")) {
            HomeScreen(milestones = listOf(milestone), onAdd = {}, onOpen = {})
        }

        // The card shows the title and the translated all-caps kicker. (The card no longer renders
        // the date — that moved to the detail screen — so locale date formatting is covered by
        // LocalizedDateFormat(Instrumented)Test instead.)
        composeRule.onNodeWithText("Sobriedad").assertIsDisplayed()
        composeRule.onNodeWithText("DÍAS DESDE").assertIsDisplayed()
    }
}
