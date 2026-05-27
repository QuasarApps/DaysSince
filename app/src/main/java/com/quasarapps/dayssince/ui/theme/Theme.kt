package com.quasarapps.dayssince.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * App theme.
 *
 * - Material You dynamic color on Android 12+ (adapts to wallpaper).
 * - Curated brand fallback on older devices and in @Preview.
 * - Follows the system light/dark setting by default.
 *
 * Edge-to-edge is configured by the host Activity (see MainActivity).
 */
@Composable
fun DaysSinceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> FallbackDarkColors
        else -> FallbackLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DaysSinceTypography,
        shapes = DaysSinceShapes,
        content = content,
    )
}
