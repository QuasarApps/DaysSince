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
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.ui.theme.accentOrDefault
import com.quasarapps.dayssince.util.EnglishDateFormat

/** Background + foreground color providers for a widget, dynamic-color aware. */
@Composable
private fun widgetColors(milestone: Milestone?): Pair<ColorProvider, ColorProvider> =
    if (milestone == null || milestone.accent == 0) {
        GlanceTheme.colors.primaryContainer to GlanceTheme.colors.onPrimaryContainer
    } else {
        val accent = accentOrDefault(milestone.accent)
        ColorProvider(accent.start) to ColorProvider(Color.White)
    }

@Composable
private fun WidgetScaffold(
    milestone: Milestone?,
    description: String,
    content: @Composable (fg: ColorProvider) -> Unit,
) {
    GlanceTheme {
        val context = LocalContext.current
        val (bg, fg) = widgetColors(milestone)
        val launch = Intent(context, MainActivity::class.java).apply {
            if (milestone != null) putExtra(MainActivity.EXTRA_MILESTONE_ID, milestone.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(bg)
                .cornerRadius(20.dp)
                .padding(8.dp)
                .clickable(actionStartActivity(launch))
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            content(fg)
        }
    }
}

@Composable
internal fun DaysWidgetContent(milestone: Milestone?) {
    if (milestone == null) {
        WidgetScaffold(null, "Tap to choose a milestone") { fg ->
            Text("Set up", style = TextStyle(color = fg, fontSize = 13.sp))
        }
        return
    }
    val dhm = DaysSince.sincePickedDhm(milestone.date, milestone.time)
    val description = "${dhm.days} days since ${milestone.title}, " +
        EnglishDateFormat.formatOrdinalDate(milestone.date)
    WidgetScaffold(milestone, description) { fg ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dhm.days.toString(),
                style = TextStyle(color = fg, fontSize = 30.sp, fontWeight = FontWeight.Bold),
            )
            Text("DAYS", style = TextStyle(color = fg, fontSize = 11.sp))
        }
    }
}

@Composable
internal fun DaysHoursMinutesWidgetContent(milestone: Milestone?) {
    if (milestone == null) {
        WidgetScaffold(null, "Tap to choose a milestone") { fg ->
            Text("Tap to set up", style = TextStyle(color = fg, fontSize = 14.sp))
        }
        return
    }
    val dhm = DaysSince.sincePickedDhm(milestone.date, milestone.time)
    val description = "${milestone.title}: ${dhm.days} days, ${dhm.hours} hours, ${dhm.minutes} minutes"
    WidgetScaffold(milestone, description) { fg ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Stat(dhm.days, "DAYS", fg)
            Spacer(GlanceModifier.width(16.dp))
            Stat(dhm.hours, "HRS", fg)
            Spacer(GlanceModifier.width(16.dp))
            Stat(dhm.minutes, "MIN", fg)
        }
    }
}

@Composable
private fun Stat(value: Long, label: String, fg: ColorProvider) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = TextStyle(color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold),
        )
        Text(label, style = TextStyle(color = fg, fontSize = 10.sp))
    }
}
