package com.quasarapps.pulsar.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.pulsar.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * Verifies the `<plurals>` resolve to the correct CLDR category per locale. This guards two things
 * the English-baseline widget test can't:
 *   - the singular ("one") form, which the widget tests never hit (their day counts are large), and
 *   - the multi-form Slavic rules (Russian one/few/many), which a simple one/other string would get
 *     wrong.
 */
@RunWith(AndroidJUnit4::class)
class PluralsLocalizationInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun resourcesFor(locale: Locale): Resources {
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(config).resources
    }

    @Test
    fun english_dayFragment_singularVsPlural() {
        val res = resourcesFor(Locale.ENGLISH)
        assertEquals("1 day", res.getQuantityString(R.plurals.widget_a11y_days, 1, 1))
        assertEquals("2 days", res.getQuantityString(R.plurals.widget_a11y_days, 2, 2))
        assertEquals("21 days", res.getQuantityString(R.plurals.widget_a11y_days, 21, 21))
    }

    @Test
    fun russian_dayFragment_usesOneFewMany() {
        val res = resourcesFor(Locale("ru"))
        // one (1, 21, 31...)
        assertEquals("1 день", res.getQuantityString(R.plurals.widget_a11y_days, 1, 1))
        assertEquals("21 день", res.getQuantityString(R.plurals.widget_a11y_days, 21, 21))
        // few (2-4, 22-24...)
        assertEquals("2 дня", res.getQuantityString(R.plurals.widget_a11y_days, 2, 2))
        // many (5-20, 0...)
        assertEquals("5 дней", res.getQuantityString(R.plurals.widget_a11y_days, 5, 5))
        assertEquals("11 дней", res.getQuantityString(R.plurals.widget_a11y_days, 11, 11))
    }

    @Test
    fun configRowSubtitle_english_singularForm() {
        val res = resourcesFor(Locale.ENGLISH)
        assertEquals(
            "1 day · since June 1, 2026",
            res.getQuantityString(R.plurals.widget_config_row_subtitle, 1, 1, "June 1, 2026"),
        )
    }
}
