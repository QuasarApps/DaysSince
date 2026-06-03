package com.quasarapps.dayssince.widget.glance

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import com.quasarapps.dayssince.util.EnglishDateFormat
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
    val description = context.getString(
        R.string.widget_days_content_description,
        dhm.days,
        milestone.title,
        EnglishDateFormat.formatOrdinalDate(milestone.date),
    )
    WidgetScaffold(milestone, transparent, description) { fg ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dhm.days.toString(),
                style = TextStyle(color = fg, fontSize = 30.sp, fontWeight = FontWeight.Bold),
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
    val description = context.getString(
        R.string.widget_dhm_content_description,
        milestone.title,
        dhm.days,
        dhm.hours,
        dhm.minutes,
    )
    WidgetScaffold(milestone, transparent, description) { fg ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Stat(dhm.days, context.getString(R.string.widget_unit_days), fg)
            Spacer(GlanceModifier.width(12.dp))
            Stat(dhm.hours, context.getString(R.string.widget_unit_hours), fg)
            Spacer(GlanceModifier.width(12.dp))
            Stat(dhm.minutes, context.getString(R.string.widget_unit_minutes), fg)
        }
    }
}

@Composable
private fun Stat(value: Long, label: String, fg: ColorProvider) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = TextStyle(color = fg, fontSize = 32.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text(label, style = TextStyle(color = fg, fontSize = 12.sp))
    }
}
