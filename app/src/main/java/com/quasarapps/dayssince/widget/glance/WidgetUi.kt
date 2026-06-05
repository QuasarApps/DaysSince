package com.quasarapps.dayssince.widget.glance

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quasarapps.dayssince.DaysSince
import com.quasarapps.dayssince.MainActivity
import com.quasarapps.dayssince.R
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.ui.theme.accentOrDefault
import com.quasarapps.dayssince.util.LocalizedDateFormat
import java.time.Clock

@Composable
private fun foregroundColor(milestone: Milestone?, transparent: Boolean): ColorProvider {
    return if (transparent) {
        // On a transparent background, use the milestone accent (or the theme primary for the
        // dynamic accent) so the number stays legible over the user's wallpaper.
        if (milestone == null || milestone.accent == 0) GlanceTheme.colors.primary
        else ColorProvider(accentOrDefault(milestone.accent).end)
    } else {
        if (milestone == null || milestone.accent == 0) GlanceTheme.colors.onPrimaryContainer
        else ColorProvider(Color.White)
    }
}

@Composable
private fun backgroundColor(milestone: Milestone?): ColorProvider {
    return if (milestone == null || milestone.accent == 0) GlanceTheme.colors.primaryContainer
    else ColorProvider(accentOrDefault(milestone.accent).start)
}

@Composable
private fun WidgetScaffold(
    milestone: Milestone?,
    transparent: Boolean,
    description: String,
    content: @Composable (fg: ColorProvider) -> Unit,
) {
    GlanceTheme {
        val context = LocalContext.current
        val fg = foregroundColor(milestone, transparent)
        val launch = Intent(context, MainActivity::class.java).apply {
            if (milestone != null) putExtra(MainActivity.EXTRA_MILESTONE_ID, milestone.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity(launch))
            .semantics { contentDescription = description }

        val modifier = if (transparent) {
            baseModifier.padding(4.dp)
        } else {
            baseModifier
                .background(backgroundColor(milestone))
                .cornerRadius(20.dp)
                .padding(8.dp)
        }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            content(fg)
        }
    }
}

@Composable
internal fun DaysWidgetContent(
    milestone: Milestone?,
    transparent: Boolean = false,
    // Injectable clock so the rendered count is deterministic under test; production uses the
    // device's wall clock.
    clock: Clock = Clock.systemDefaultZone(),
) {
    val context = LocalContext.current
    if (milestone == null) {
        WidgetScaffold(null, transparent, context.getString(R.string.widget_setup_content_description)) { fg ->
            Text(context.getString(R.string.widget_setup_short), style = TextStyle(color = fg, fontSize = 13.sp))
        }
        return
    }
    val dhm = DaysSince.sincePickedDhm(milestone.date, milestone.time, clock)
    val locale = context.resources.configuration.locales[0]
    val daysFragment =
        context.resources.getQuantityString(R.plurals.widget_a11y_days, dhm.days.toInt(), dhm.days)
    val description = context.getString(
        R.string.widget_days_content_description,
        daysFragment,
        milestone.title,
        LocalizedDateFormat.formatLongDate(milestone.date, locale),
    )
    val daysText = cappedDays(dhm.days)
    WidgetScaffold(milestone, transparent, description) { fg ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = daysText,
                // Shrink for longer values (Glance has no auto-size) so up to "9999+" fits the 1x1.
                style = TextStyle(color = fg, fontSize = daysWidgetFontSize(daysText), fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text(context.getString(R.string.widget_unit_days), style = TextStyle(color = fg, fontSize = 11.sp))
        }
    }
}

@Composable
internal fun DaysHoursMinutesWidgetContent(
    milestone: Milestone?,
    transparent: Boolean = false,
    // Injectable clock so the rendered breakdown is deterministic under test; production uses the
    // device's wall clock.
    clock: Clock = Clock.systemDefaultZone(),
) {
    val context = LocalContext.current
    if (milestone == null) {
        WidgetScaffold(null, transparent, context.getString(R.string.widget_setup_content_description)) { fg ->
            Text(context.getString(R.string.widget_setup_long), style = TextStyle(color = fg, fontSize = 14.sp))
        }
        return
    }
    val dhm = DaysSince.sincePickedDhm(milestone.date, milestone.time, clock)
    val res = context.resources
    val description = context.getString(
        R.string.widget_dhm_content_description,
        milestone.title,
        res.getQuantityString(R.plurals.widget_a11y_days, dhm.days.toInt(), dhm.days),
        res.getQuantityString(R.plurals.widget_a11y_hours, dhm.hours.toInt(), dhm.hours),
        res.getQuantityString(R.plurals.widget_a11y_minutes, dhm.minutes.toInt(), dhm.minutes),
    )
    val daysText = cappedDays(dhm.days)
    // Size all three stats off the (widest) days value so they shrink together and the row keeps
    // fitting the 2x1 footprint as the day count grows — Glance has no text auto-size.
    val statFontSize = dhmStatFontSize(daysText)
    WidgetScaffold(milestone, transparent, description) { fg ->
        // Three stats in a row, sized to fit a 2x1 footprint (narrower than the old 3x1 strip).
        Row(verticalAlignment = Alignment.CenterVertically) {
            Stat(daysText, context.getString(R.string.widget_unit_days), fg, statFontSize)
            Spacer(GlanceModifier.width(6.dp))
            Stat(dhm.hours.toString(), context.getString(R.string.widget_unit_hours), fg, statFontSize)
            Spacer(GlanceModifier.width(6.dp))
            Stat(dhm.minutes.toString(), context.getString(R.string.widget_unit_minutes), fg, statFontSize)
        }
    }
}

@Composable
private fun Stat(value: String, label: String, fg: ColorProvider, fontSize: TextUnit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(color = fg, fontSize = fontSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text(label, style = TextStyle(color = fg, fontSize = 10.sp))
    }
}

/** Whole days, capped at 4 digits; anything larger renders as "9999+". */
private fun cappedDays(days: Long): String = if (days > 9999) "9999+" else days.toString()

/** Font size for the 1x1 Days widget number, shrinking as the string gets longer. */
private fun daysWidgetFontSize(daysText: String): TextUnit = when (daysText.length) {
    in 0..2 -> 30.sp
    3 -> 26.sp
    4 -> 20.sp
    else -> 16.sp // "9999+"
}

/** Font size for the 2x1 DHM stats, shrinking as the days string gets longer. */
private fun dhmStatFontSize(daysText: String): TextUnit = when (daysText.length) {
    in 0..2 -> 22.sp
    3 -> 20.sp
    4 -> 17.sp
    else -> 14.sp // "9999+"
}
