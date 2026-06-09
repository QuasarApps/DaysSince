package com.quasarapps.pulsar.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** The two stops of a milestone accent, from the fixed [MilestoneAccents] palette. */
fun accentStops(accentIndex: Int): Pair<Color, Color> {
    val accent = accentOrDefault(accentIndex)
    return accent.start to accent.end
}

/** Diagonal hero/card gradient for a milestone accent. */
fun accentBrush(accentIndex: Int): Brush {
    val (start, end) = accentStops(accentIndex)
    return Brush.linearGradient(
        colors = listOf(start, end),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
}

/** The signature Quasar gradient (magenta → violet → indigo) — used for the FAB and brand moments. */
val QuasarBrush: Brush = Brush.linearGradient(
    listOf(
        Color(0xFFFFE8FB),
        Color(0xFFF482DC),
        Color(0xFFD131BC),
        Color(0xFF9925C3),
        Color(0xFF480E93),
    ),
)

/** The radial "new beginning" gradient for a 0-day milestone — a white-hot core blooming to violet. */
val NewBeginningBrush: Brush = Brush.radialGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFF0FB),
        0.14f to Color(0xFFF482DC),
        0.34f to Color(0xFFD131BC),
        0.58f to Color(0xFF9925C3),
        1.0f to Color(0xFF3A0E6E),
    ),
)
