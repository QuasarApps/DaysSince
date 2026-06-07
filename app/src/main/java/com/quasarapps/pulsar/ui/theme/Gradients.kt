package com.quasarapps.pulsar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The two stops of a milestone accent. For [DYNAMIC_ACCENT] these are pulled from the active
 * Material You scheme so the gradient adapts to the user's wallpaper; otherwise they come from
 * the fixed [MilestoneAccents] palette.
 */
@Composable
fun accentStops(accentIndex: Int): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return if (accentIndex == DYNAMIC_ACCENT) {
        scheme.primary to scheme.tertiary
    } else {
        val accent = accentOrDefault(accentIndex)
        accent.start to accent.end
    }
}

/** Diagonal hero/card gradient for a milestone accent. */
@Composable
fun accentBrush(accentIndex: Int): Brush {
    val (start, end) = accentStops(accentIndex)
    return Brush.linearGradient(
        colors = listOf(start, end),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
}

/**
 * A soft top-down legibility scrim. Layered over the accent gradient behind white text so
 * contrast holds up on lighter accents (e.g. amber) regardless of wallpaper.
 */
val LegibilityScrim: Brush = Brush.verticalGradient(
    colors = listOf(Color(0x00000000), Color(0x40000000)),
)
