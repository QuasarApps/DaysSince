package com.quasarapps.dayssince.widget.glance

import android.content.Context
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasContentDescription
import androidx.glance.testing.unit.hasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quasarapps.dayssince.data.Milestone
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit tests for the Glance widget content composables ([DaysWidgetContent] /
 * [DaysHoursMinutesWidgetContent]) using `runGlanceAppWidgetUnitTest`.
 *
 * These assert the emitted Glance node tree (text + content descriptions) for both the
 * unconfigured ("set up") state and a bound milestone. A real [Context] is supplied because the
 * scaffold reads `LocalContext.current` and builds a launch [android.content.Intent].
 */
@RunWith(AndroidJUnit4::class)
class WidgetContentInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // A far-past date so the day count is a stable, non-zero, multi-digit number.
    private val milestone = Milestone(
        id = "a",
        title = "Sober",
        date = LocalDate.of(2020, 1, 1),
        time = LocalTime.of(0, 0),
        accent = 1,
    )

    @Test
    fun daysWidget_unconfigured_showsSetUpPrompt() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysWidgetContent(milestone = null) }

        onNode(hasTextEqualTo("Set up")).assertExists()
        onNode(hasContentDescriptionEqualTo("Tap to choose a milestone")).assertExists()
    }

    @Test
    fun daysWidget_boundMilestone_showsDaysLabelAndDescription() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysWidgetContent(milestone = milestone) }

        onNode(hasTextEqualTo("DAYS")).assertExists()
        // Assert the stable suffix as a substring rather than the full string: the leading day count
        // is computed from "now" inside the composable, so an exact match could flake across a
        // midnight boundary. (The day-count math itself is covered deterministically in
        // DaysSinceInstrumentedTest with a fixed Clock.)
        onNode(hasContentDescription("days since Sober, 1st of January 2020")).assertExists()
    }

    @Test
    fun dhmWidget_unconfigured_showsSetUpPrompt() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysHoursMinutesWidgetContent(milestone = null) }

        onNode(hasTextEqualTo("Tap to set up")).assertExists()
        onNode(hasContentDescriptionEqualTo("Tap to choose a milestone")).assertExists()
    }

    @Test
    fun dhmWidget_boundMilestone_showsDaysHoursMinutesLabels() = runGlanceAppWidgetUnitTest {
        setContext(context)
        provideComposable { DaysHoursMinutesWidgetContent(milestone = milestone) }

        onNode(hasTextEqualTo("DAYS")).assertExists()
        onNode(hasTextEqualTo("HRS")).assertExists()
        onNode(hasTextEqualTo("MIN")).assertExists()
    }
}
