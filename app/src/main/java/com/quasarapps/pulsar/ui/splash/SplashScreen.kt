package com.quasarapps.pulsar.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.ui.components.Starburst
import com.quasarapps.pulsar.ui.components.rememberReduceMotion
import com.quasarapps.pulsar.ui.theme.PulsarDarkColors
import kotlinx.coroutines.delay

// The splash is always the dark cosmic brand surface (like the launcher icon), independent of the
// light/dark app theme, so the cold-start system splash and this screen read as one continuous moment.
// The base surface and mark are sourced from the canonical dark palette below (and the XML splash
// background is kept in sync with PulsarDarkColors.background) so they can't drift; only the glow,
// which exists nowhere else, is a splash-local constant.
private val SplashGlow = Color(0xFF3A1457)

private const val EnterMillis = 650
private const val HoldMillis = 550
private const val ReduceHoldMillis = 500

/**
 * Branded launch splash: the pulsar mark, the wordmark, and the tagline on the cosmic brand surface.
 *
 * The mark and text ease in (unless the user has reduced motion), then [onFinished] fires after a
 * short hold so the caller can dismiss it. Shown as an overlay above the app content, which composes
 * underneath so the home screen is ready when the splash clears.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val reduceMotion = rememberReduceMotion()
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            progress.animateTo(1f, animationSpec = tween(EnterMillis, easing = FastOutSlowInEasing))
            delay(HoldMillis.toLong())
        } else {
            delay(ReduceHoldMillis.toLong())
        }
        onFinished()
    }

    val p = progress.value
    val markScale = 0.85f + 0.15f * p
    // Text trails the mark in slightly.
    val textAlpha = ((p - 0.4f) / 0.6f).coerceIn(0f, 1f)
    val baseColor = PulsarDarkColors.background
    val markColor = PulsarDarkColors.onPrimaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Treat the splash as a single modal a11y surface. The caller clears the semantics of the
            // content behind it while it's up, so a screen reader stays within the splash.
            .semantics { isTraversalGroup = true }
            // Swallow all touches so nothing falls through to the app content behind the overlay
            // while the splash is up. (No-op clickable would mislabel the splash as a button.)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(SplashGlow, baseColor),
                        center = center,
                        radius = size.minDimension * 0.75f,
                    ),
                )
            }
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Starburst(
            color = markColor,
            modifier = Modifier
                .size(92.dp)
                .graphicsLayer {
                    alpha = p
                    scaleX = markScale
                    scaleY = markScale
                },
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textAlpha),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textAlpha),
        )
    }
}
