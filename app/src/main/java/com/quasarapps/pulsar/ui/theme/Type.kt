package com.quasarapps.pulsar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.quasarapps.pulsar.R

/*
 * Pulsar uses two downloadable Google Fonts (no bundled binaries, no APK bloat):
 *
 * - Chakra Petch — semi-techno, geometric, tabular figures. The "spacecraft readout" voice; used
 *   for counters, display/headline text, and the small tracked uppercase labels.
 * - Hanken Grotesk — warm humanist grotesque; used for titles, body, and buttons.
 *
 * If the font provider is unavailable on a device (or the certs in res/values/font_certs.xml need
 * regenerating), the families degrade gracefully to the platform font — no crash.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val chakraPetch = GoogleFont("Chakra Petch")
private val hankenGrotesk = GoogleFont("Hanken Grotesk")

/** Display / counter / label face — Chakra Petch. */
val DisplayFontFamily = FontFamily(
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.Bold),
)

/** Title / body / button face — Hanken Grotesk. */
val BodyFontFamily = FontFamily(
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.ExtraBold),
)

private val baseline = Typography()

/**
 * M3 type scale re-skinned in the Pulsar voice (sizes kept; families/weights swapped):
 * - display & headline → Chakra Petch (the cosmic readout)
 * - title, body, label → Hanken Grotesk (titles extra-bold, labels/buttons bold)
 *
 * The small tracked-uppercase "kicker" captions (e.g. "DAYS SINCE", unit labels) are styled at the
 * call site with [DisplayFontFamily] + letter spacing, since M3 has no dedicated role for them.
 */
val PulsarTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
    titleLarge = baseline.titleLarge.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.ExtraBold),
    titleMedium = baseline.titleMedium.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold),
    titleSmall = baseline.titleSmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = BodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = BodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = BodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold),
    labelMedium = baseline.labelMedium.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold),
    labelSmall = baseline.labelSmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold),
)
