package com.quasarapps.pulsar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme.
 *
 * - A fixed Pulsar brand palette (no Material You / wallpaper adaptation) so every device shows the
 *   same identity.
 * - [darkTheme] follows the system setting by default; Settings can override it (wired in a later
 *   phase).
 *
 * Edge-to-edge is configured by the host Activity (see MainActivity).
 */
@Composable
fun PulsarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PulsarDarkColors else PulsarLightColors,
        typography = PulsarTypography,
        shapes = PulsarShapes,
        content = content,
    )
}
