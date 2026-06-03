package com.quasarapps.dayssince.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Brand fallback palette. Used on API < 31 (no Material You) and in @Preview, where dynamic
 * color from the wallpaper is unavailable. On Android 12+ the dynamic scheme takes over and
 * these values are not used.
 */

private val BrandPrimary = Color(0xFF5B5FE0)
private val BrandPrimaryDark = Color(0xFFBFC2FF)
private val BrandSecondary = Color(0xFF7C5BE0)
private val BrandTertiary = Color(0xFFE05B97)

val FallbackLightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E0FF),
    onPrimaryContainer = Color(0xFF12104A),
    secondary = BrandSecondary,
    onSecondary = Color.White,
    tertiary = BrandTertiary,
    onTertiary = Color.White,
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    outline = Color(0xFF787680),
)

val FallbackDarkColors = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF21195A),
    primaryContainer = Color(0xFF3A3273),
    onPrimaryContainer = Color(0xFFE2E0FF),
    secondary = Color(0xFFCEBDFF),
    onSecondary = Color(0xFF34215E),
    tertiary = Color(0xFFFFB1CE),
    onTertiary = Color(0xFF5A1138),
    background = Color(0xFF121218),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF121218),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC9C5D0),
    outline = Color(0xFF938F99),
)

/**
 * Per-milestone accent. Drives the card / hero gradient and the widget background.
 *
 * Index [DYNAMIC_ACCENT] (0) is special: it is rendered from the active Material You color
 * scheme at draw time (see Gradients.kt), so the placeholder stops here are only used if the
 * dynamic scheme is somehow unavailable.
 */
/**
 * @param key a stable identifier persisted with a milestone. Unlike the list position (which
 *   silently remaps a milestone's color if the palette is ever reordered) and [label] (which is
 *   user-facing and may be localized), [key] never changes once shipped.
 */
data class Accent(
    val key: String,
    val label: String,
    val start: Color,
    val end: Color,
    val onAccent: Color = Color.White,
)

const val DYNAMIC_ACCENT = 0

val MilestoneAccents: List<Accent> = listOf(
    Accent("dynamic", "Dynamic", Color(0xFF5B5FE0), Color(0xFFE05B97)),
    Accent("indigo", "Indigo", Color(0xFF4F46E5), Color(0xFF7C3AED)),
    Accent("violet", "Violet", Color(0xFF7C3AED), Color(0xFFC026D3)),
    Accent("rose", "Rose", Color(0xFFE11D48), Color(0xFFFB7185)),
    Accent("sunset", "Sunset", Color(0xFFF97316), Color(0xFFE11D48)),
    Accent("emerald", "Emerald", Color(0xFF059669), Color(0xFF14B8A6)),
    Accent("ocean", "Ocean", Color(0xFF0EA5E9), Color(0xFF6366F1)),
    Accent("slate", "Slate", Color(0xFF334155), Color(0xFF0F172A)),
)

fun accentOrDefault(index: Int): Accent =
    MilestoneAccents.getOrElse(index) { MilestoneAccents[1] }

/** Stable persistence key for the accent at [index] (falls back to the default if out of range). */
fun accentKey(index: Int): String = accentOrDefault(index).key

/**
 * Resolves a stable accent [key] back to its current list index, or [DYNAMIC_ACCENT] if the key is
 * null/unknown (e.g. an accent that was removed in a later release).
 */
fun accentIndexForKey(key: String?): Int {
    val index = MilestoneAccents.indexOfFirst { it.key == key }
    return if (index >= 0) index else DYNAMIC_ACCENT
}
