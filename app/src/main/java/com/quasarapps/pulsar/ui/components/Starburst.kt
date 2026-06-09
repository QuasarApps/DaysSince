package com.quasarapps.pulsar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/**
 * A four-point pulsar starburst: two crossing rays with a glowing core. Purely decorative — used on
 * the home/empty/preview surfaces and the FAB. Sized to its [modifier]; colored by [color].
 */
@Composable
fun Starburst(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f
        val gap = r * 0.32f
        val stroke = r * 0.10f
        drawLine(color, Offset(cx, cy - r), Offset(cx, cy - gap), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + r), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(cx - r, cy), Offset(cx - gap, cy), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(cx + gap, cy), Offset(cx + r, cy), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color, radius = r * 0.16f, center = Offset(cx, cy))
    }
}
