package com.quasarapps.pulsar.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.quasarapps.pulsar.R

/*
 * The Pulsar brand palette — a fixed identity (no Material You). The app ships these dark and light
 * schemes on every device; light/dark follows the system unless overridden in Settings. Dark is the
 * canonical brand surface (cosmic purple-black). Values come from the Pulsar design system.
 */

val PulsarDarkColors = darkColorScheme(
    primary = Color(0xFFE7A6FF),
    onPrimary = Color(0xFF4B0072),
    primaryContainer = Color(0xFF6A009A),
    onPrimaryContainer = Color(0xFFF6D9FF),
    inversePrimary = Color(0xFF7A19A6),
    secondary = Color(0xFFCDB8FF),
    onSecondary = Color(0xFF341C6B),
    secondaryContainer = Color(0xFF4B358C),
    onSecondaryContainer = Color(0xFFE7DEFF),
    tertiary = Color(0xFFFFADE0),
    onTertiary = Color(0xFF5C0E48),
    tertiaryContainer = Color(0xFF7C2A63),
    onTertiaryContainer = Color(0xFFFFD8EC),
    error = Color(0xFFFFB3AE),
    onError = Color(0xFF5E1116),
    errorContainer = Color(0xFF8C2F2C),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D0712),
    onBackground = Color(0xFFEBE0EC),
    surface = Color(0xFF0D0712),
    onSurface = Color(0xFFEBE0EC),
    surfaceDim = Color(0xFF0D0712),
    surfaceBright = Color(0xFF352B3B),
    surfaceContainerLowest = Color(0xFF070409),
    surfaceContainerLow = Color(0xFF15101B),
    surfaceContainer = Color(0xFF191320),
    surfaceContainerHigh = Color(0xFF241C2C),
    surfaceContainerHighest = Color(0xFF2F2637),
    surfaceVariant = Color(0xFF241C2C),
    onSurfaceVariant = Color(0xFFCFC1D1),
    outline = Color(0xFF9A8C9E),
    outlineVariant = Color(0xFF4D4351),
    inverseSurface = Color(0xFFEBE0EC),
    inverseOnSurface = Color(0xFF332D38),
    scrim = Color(0xFF000000),
)

val PulsarLightColors = lightColorScheme(
    primary = Color(0xFF7A19A6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3CCFF),
    onPrimaryContainer = Color(0xFF2E004A),
    inversePrimary = Color(0xFFE7A6FF),
    secondary = Color(0xFF635591),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7DEFF),
    onSecondaryContainer = Color(0xFF23074F),
    tertiary = Color(0xFF9C447F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8EC),
    onTertiaryContainer = Color(0xFF3A0726),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFCF5FD),
    onBackground = Color(0xFF1D1626),
    surface = Color(0xFFFCF5FD),
    onSurface = Color(0xFF1D1626),
    surfaceDim = Color(0xFFDDD0DE),
    surfaceBright = Color(0xFFFCF5FD),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6ECF7),
    surfaceContainer = Color(0xFFF1E5F3),
    surfaceContainerHigh = Color(0xFFEBDDEE),
    surfaceContainerHighest = Color(0xFFE5D6E9),
    surfaceVariant = Color(0xFFEBDDEE),
    onSurfaceVariant = Color(0xFF5C5060),
    outline = Color(0xFF7C7480),
    outlineVariant = Color(0xFFDBCDDD),
    inverseSurface = Color(0xFF322B36),
    inverseOnSurface = Color(0xFFF5EDF6),
    scrim = Color(0xFF000000),
)

/**
 * Per-milestone accent. Drives the card/hero gradient and the widget background.
 *
 * @param key stable identifier persisted with a milestone (never changes once shipped, unlike the
 *   list position or localized [labelRes]). Order must stay in sync with `data.AccentKeys` (enforced
 *   by `AccentTest`).
 * @param labelRes localized accent name, used as the accent picker's accessibility label.
 * @param onAccent legible foreground for text/icons on the accent (white for most; dark on Solar).
 */
data class Accent(
    val key: String,
    @StringRes val labelRes: Int,
    val start: Color,
    val end: Color,
    val onAccent: Color = Color.White,
)

/** The eight milestone accents from the Pulsar design system, in persistence order. */
val MilestoneAccents: List<Accent> = listOf(
    Accent("magenta", R.string.accent_magenta, Color(0xFFF482DC), Color(0xFFD131BC)),
    Accent("violet", R.string.accent_violet, Color(0xFFB25FDF), Color(0xFF7B1AAE)),
    Accent("indigo", R.string.accent_indigo, Color(0xFF5B6BF0), Color(0xFF2A1AA8)),
    Accent("nebula", R.string.accent_nebula, Color(0xFF3FA9F5), Color(0xFF1C2C9A)),
    Accent("aurora", R.string.accent_aurora, Color(0xFF2BD9B0), Color(0xFF0E8A77)),
    Accent("solar", R.string.accent_solar, Color(0xFFFFC36B), Color(0xFFE08A2C), onAccent = Color(0xFF3A2400)),
    Accent("ember", R.string.accent_ember, Color(0xFFFF7A59), Color(0xFFD8341C)),
    Accent("deep", R.string.accent_deep, Color(0xFF2A2740), Color(0xFF0E1024)),
)

/** Lookup with a safe fallback to the default accent for a stored index that no longer exists. */
fun accentOrDefault(index: Int): Accent =
    MilestoneAccents.getOrElse(index) { MilestoneAccents[0] }
