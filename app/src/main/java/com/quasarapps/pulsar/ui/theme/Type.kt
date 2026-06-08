package com.quasarapps.pulsar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font as BundledFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.quasarapps.pulsar.R

/*
 * Pulsar uses two brand faces:
 *
 * - Chakra Petch — semi-techno, geometric, tabular figures. The "spacecraft readout" voice; used
 *   for counters, display/headline text, and the small tracked uppercase labels.
 * - Hanken Grotesk — warm humanist grotesque; used for titles, body, and buttons.
 *
 * Each family is declared as a downloadable Google Font (preferred — shared across apps, kept fresh)
 * paired with a bundled fallback for every weight it uses, so the brand face still renders when the
 * font provider is unavailable: offline, on first launch before the download completes, or on a
 * device without Google Play services. Compose resolves the bundled (blocking) font immediately and
 * the downloadable one upgrades it in if/when it loads — so there's no fallback-to-system flash.
 *
 * Chakra Petch is bundled as static per-weight files; Hanken Grotesk is bundled once as its variable
 * font, with each weight selected via the `wght` variation axis. License texts (OFL) for both live in
 * app/licenses/.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val chakraPetch = GoogleFont("Chakra Petch")
private val hankenGrotesk = GoogleFont("Hanken Grotesk")

/** Display / counter / label face — Chakra Petch (downloadable, with bundled per-weight fallbacks). */
val DisplayFontFamily = FontFamily(
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.Medium),
    BundledFont(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.SemiBold),
    BundledFont(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(googleFont = chakraPetch, fontProvider = provider, weight = FontWeight.Bold),
    BundledFont(R.font.chakra_petch_bold, FontWeight.Bold),
)

/** Title / body / button face — Hanken Grotesk (downloadable, with a bundled variable-font fallback). */
val BodyFontFamily = FontFamily(
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Normal),
    hankenFallback(FontWeight.Normal),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Medium),
    hankenFallback(FontWeight.Medium),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.SemiBold),
    hankenFallback(FontWeight.SemiBold),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.Bold),
    hankenFallback(FontWeight.Bold),
    Font(googleFont = hankenGrotesk, fontProvider = provider, weight = FontWeight.ExtraBold),
    hankenFallback(FontWeight.ExtraBold),
)

/**
 * One weight of the bundled Hanken Grotesk *variable* font: the `wght` axis is pinned to [weight] so
 * a single file covers every weight the type scale uses.
 */
@OptIn(ExperimentalTextApi::class)
private fun hankenFallback(weight: FontWeight) = BundledFont(
    resId = R.font.hanken_grotesk,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
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
