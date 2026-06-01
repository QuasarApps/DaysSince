package com.quasarapps.dayssince.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

/**
 * Renders [target] with tabular figures (digits keep a fixed width so the number doesn't jump),
 * animating a count-up from 0 on first appearance unless the user has reduced motion.
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

    Text(
        text = animated.value.toLong().toString(),
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier,
    )
}
