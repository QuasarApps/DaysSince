package com.quasarapps.pulsar.widget.glance

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import com.quasarapps.pulsar.ElapsedTime
import com.quasarapps.pulsar.MainActivity
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.ui.theme.accentOrDefault
import com.quasarapps.pulsar.util.LocalizedDateFormat
import java.time.Clock

private fun foregroundColor(milestone: Milestone?, transparent: Boolean): ColorProvider {
    // On a transparent background the accent's bright end stays legible over the wallpaper; on the
    // solid accent background, use the accent's own on-color (white, or dark for the light Solar).
    val accent = accentOrDefault(milestone?.accent ?: 0)
    return ColorProvider(if (transparent) accent.end else accent.onAccent)
}

/** Pixel size of the (square) rasterized gradient; small + stretched to fill keeps the payload tiny. */
private const val GradientBitmapSizePx = 144

/**
 * A diagonal (corner-to-corner) accent gradient as a small bitmap, matching the app's card/hero
 * gradient. Glance's background modifier can't take a Compose Brush, so the gradient is rasterized
 * from the same accent palette colors (no drift) and stretched to fill the widget via FillBounds.
 */
private fun accentGradientBitmap(accentIndex: Int): Bitmap {
    val accent = accentOrDefault(accentIndex)
    val bitmap = Bitmap.createBitmap(GradientBitmapSizePx, GradientBitmapSizePx, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawPaint(
        Paint().apply {
            isDither = true
            shader = LinearGradient(
                0f, 0f, GradientBitmapSizePx.toFloat(), GradientBitmapSizePx.toFloat(),
                accent.start.toArgb(), accent.end.toArgb(),
                Shader.TileMode.CLAMP,
            )
        },
    )
    return bitmap
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
        // Cache the rasterized gradient per accent so a periodic refresh (a day-count change that
        // leaves the accent unchanged) reuses the bitmap instead of redrawing it each recomposition.
        val accentIndex = milestone?.accent ?: 0
        val background = remember(accentIndex) { accentGradientBitmap(accentIndex) }
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
                .background(ImageProvider(background), contentScale = ContentScale.FillBounds)
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
    val dhm = ElapsedTime.sincePickedDhm(milestone.date, milestone.time, clock)
    val locale = context.resources.configuration.locales[0]
    val daysFragment =
        context.resources.getQuantityString(R.plurals.widget_a11y_days, dhm.days.toInt(), dhm.days)
    val description = context.getString(
        R.string.widget_days_content_description,
        daysFragment,
        milestone.title,
        LocalizedDateFormat.formatLongDate(milestone.date, locale),
    )
    val daysText = cappedDays(dhm.days, context)
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
    val dhm = ElapsedTime.sincePickedDhm(milestone.date, milestone.time, clock)
    val res = context.resources
    val description = context.getString(
        R.string.widget_dhm_content_description,
        milestone.title,
        res.getQuantityString(R.plurals.widget_a11y_days, dhm.days.toInt(), dhm.days),
        res.getQuantityString(R.plurals.widget_a11y_hours, dhm.hours.toInt(), dhm.hours),
        res.getQuantityString(R.plurals.widget_a11y_minutes, dhm.minutes.toInt(), dhm.minutes),
    )
    val daysText = cappedDays(dhm.days, context)
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

/** Whole days, capped at 4 digits; anything larger renders as the capped string ("9999+"). */
private fun cappedDays(days: Long, context: Context): String =
    if (days > 9999) context.getString(R.string.widget_days_capped) else days.toString()

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
