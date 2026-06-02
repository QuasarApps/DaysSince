package com.quasarapps.dayssince.widget.glance

import android.content.Context
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.data.Milestone
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Unit tests for the Glance widget content composables ([DaysWidgetContent] /
 * [DaysHoursMinutesWidgetContent]) using `runGlanceAppWidgetUnitTest`.
 *
 * The composables take an injectable [Clock], so the rendered counts are deterministic here: each
 * test computes the expected elapsed value from the same fixed clock and asserts that exact number
 * (and the full content description) is emitted — not just the static labels.
 *
 * A real [Context] is supplied because the scaffold reads `LocalContext.current` and builds a
 * launch [android.content.Intent].
 */
@RunWith(AndroidJUnit4::class)
class WidgetContentInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val milestone = Milestone(
        id = "a",
        title = "Sober",
        date = LocalDate.of(2020, 1, 1),
        time = LocalTime.of(0, 0),
        accent = 1,
    )

    // A fixed instant pins "now" so the day/hour/minute breakdown is stable. The composable applies
    // the system default zone (as in production); we compute the expected value the same way, so the
    // assertions stay correct regardless of the device's time zone.
    private val clock = Clock.fixed(Instant.parse("2025-03-10T05:30:00Z"), ZoneId.of("UTC"))
    private val expected = DaysSince.sincePickedDhm(milestone.date, milestone.time, clock)

    @Test
    fun daysWidget_unconfigured_showsSetUpPrompt() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysWidgetContent(milestone = null) }

        onNode(hasTextEqualTo("Set up")).assertExists()
        onNode(hasContentDescriptionEqualTo("Tap to choose a milestone")).assertExists()
    }

    @Test
    fun daysWidget_boundMilestone_rendersDayCountAndDescription() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysWidgetContent(milestone = milestone, clock = clock) }

        // The actual elapsed day count is rendered (not just the static "DAYS" label).
        onNode(hasTextEqualTo(expected.days.toString())).assertExists()
        onNode(hasTextEqualTo("DAYS")).assertExists()
        onNode(
            hasContentDescriptionEqualTo("${expected.days} days since Sober, 1st of January 2020"),
        ).assertExists()
    }

    @Test
    fun dhmWidget_unconfigured_showsSetUpPrompt() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysHoursMinutesWidgetContent(milestone = null) }

        onNode(hasTextEqualTo("Tap to set up")).assertExists()
        onNode(hasContentDescriptionEqualTo("Tap to choose a milestone")).assertExists()
    }

    @Test
    fun dhmWidget_boundMilestone_rendersDaysHoursMinutes() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysHoursMinutesWidgetContent(milestone = milestone, clock = clock) }

        // The visible day count is the real computed value...
        onNode(hasTextEqualTo(expected.days.toString())).assertExists()
        onNode(hasTextEqualTo("DAYS")).assertExists()
        onNode(hasTextEqualTo("HRS")).assertExists()
        onNode(hasTextEqualTo("MIN")).assertExists()
        // ...and the content description carries the full, correct d/h/m breakdown (this is what
        // would fail if sincePickedDhm or the Stat wiring produced the wrong numbers).
        onNode(
            hasContentDescriptionEqualTo(
                "Sober: ${expected.days} days, ${expected.hours} hours, ${expected.minutes} minutes",
            ),
        ).assertExists()
    }
}
