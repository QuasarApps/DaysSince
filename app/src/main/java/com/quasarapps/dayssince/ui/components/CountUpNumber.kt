package com.quasarapps.dayssince.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Renders [target] with tabular figures (digits keep a fixed width so the number doesn't jump),
 * animating a count-up from 0 on first appearance unless the user has reduced motion.
 *
 * The font auto-shrinks from [style]'s size so even a large day count fits on one line (no
 * truncation). It's sized to the final [target] string — the widest the count-up ever reaches — so
 * the size stays constant throughout the animation rather than jumping as digits are added.
 */
@Composable
fun CountUpNumber(
    target: Long,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    val animated = remember { Animatable(if (reduceMotion) target.toFloat() else 0f) }

    LaunchedEffect(target, reduceMotion) {
        if (reduceMotion) {
            animated.snapTo(target.toFloat())
        } else {
            animated.animateTo(target.toFloat(), animationSpec = tween(durationMillis = 850))
        }
    }

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val targetText = target.toString()
        val baseSize = style.fontSize.takeIf { it != TextUnit.Unspecified } ?: 112.sp
        // Shrink the font until the widest value (the final target) fits the available width.
        val fittedSize = remember(targetText, maxWidth, baseSize) {
            val maxWidthPx = with(density) { maxWidth.toPx() }
            var size = baseSize
            // Measure with the same tabular-figures feature used when rendering, so digit widths
            // match and a tight fit doesn't end up clipping.
            while (
                size.value > MIN_FONT_SP &&
                measurer.measure(
                    targetText,
                    style.copy(fontSize = size, fontFeatureSettings = "tnum"),
                ).size.width > maxWidthPx
            ) {
                size = (size.value * STEP).sp
            }
            size
        }

        Text(
            text = animated.value.toLong().toString(),
            style = style.copy(
                fontSize = fittedSize,
                lineHeight = fittedSize,
                fontFeatureSettings = "tnum",
            ),
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}

// Shrink in ~6% steps down to a sensible floor; the floor is only reached by implausibly large
// day counts and still renders fully on one line on a phone.
private const val STEP = 0.94f
private const val MIN_FONT_SP = 24f
