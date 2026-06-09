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
 * - Chakra Petch — geometric, tabular figures; the "readout" voice for counters/display/labels.
 * - Hanken Grotesk — humanist grotesque; titles, body, buttons.
 *
 * Each is a downloadable Google Font paired with a bundled fallback for every weight, so the brand
 * face still renders offline / before the download / without Play services (Compose shows the bundled
 * font immediately and upgrades to the downloadable one with no flash). Chakra Petch is bundled as
 * static per-weight files; Hanken Grotesk once as its variable font (weight via the `wght` axis).
 * Both are OFL; license texts are under assets/licenses/.
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

/** One weight of the bundled Hanken Grotesk variable font: `wght` pinned to [weight]. */
@OptIn(ExperimentalTextApi::class)
private fun hankenFallback(weight: FontWeight) = BundledFont(
    resId = R.font.hanken_grotesk,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val baseline = Typography()

/**
 * M3 type scale re-skinned in the Pulsar voice (sizes kept; families/weights swapped): display &
 * headline → Chakra Petch; title/body/label → Hanken Grotesk. The tracked-uppercase "kicker" captions
 * (e.g. "DAYS SINCE") are styled at the call site with [DisplayFontFamily], since M3 has no role for them.
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
