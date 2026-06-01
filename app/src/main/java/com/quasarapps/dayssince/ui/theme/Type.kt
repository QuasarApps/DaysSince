package com.quasarapps.dayssince.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.quasarapps.dayssince.R

/*
 * Display typeface via downloadable Google Fonts (no bundled binary, no APK bloat).
 *
 * If the font provider is unavailable on a device (or the certs in res/values/font_certs.xml
 * need regenerating), the FontFamily degrades gracefully to the platform font — no crash.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val displayGoogleFont = GoogleFont("Space Grotesk")

val DisplayFontFamily = FontFamily(
    Font(googleFont = displayGoogleFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = displayGoogleFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = displayGoogleFont, fontProvider = provider, weight = FontWeight.Bold),
)

private val baseline = Typography()

/** Display/headline/title styles use the expressive display face; body/label stay on platform font. */
val DaysSinceTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    titleLarge = baseline.titleLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
)
