package com.fairprice.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

/**
 * PDS Fair Price App — "The Editorial Voice" Typography System
 *
 * Dual-typeface strategy:
 * - Public Sans (Headlines): Clean, geometric authority for government context
 * - Inter (Body & Labels): Exceptional legibility at all scales
 *
 * Key Rule: Body text is NEVER smaller than body-lg (16sp) for accessibility.
 * Display-md (44sp) for critical numbers — grain quantities, OTPs.
 */

private val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.fairprice.app.R.array.com_google_android_gms_fonts_certs
)

private val PublicSansFont = GoogleFont("Public Sans")
private val InterFont = GoogleFont("Inter")

val PublicSansFontFamily = FontFamily(
    Font(googleFont = PublicSansFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = PublicSansFont, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = PublicSansFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PublicSansFont, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
)

val InterFontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
)

val FairPriceTypography = Typography(
    // ─── Display: Impactful hero data (grain quantities, OTPs) ────
    displayLarge = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,    // "Hierarchy of Trust" — critical numbers
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // ─── Headlines: Screen titles (Public Sans for authority) ─────
    headlineLarge = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,    // headline-lg (2rem) — instant recognition
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // ─── Titles: Card titles, sub-headers (Inter for clarity) ─────
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,     // title-lg for labels (accessibility)
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ─── Body: Standard content (Inter — never smaller than 16sp) ─
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,     // Minimum body text per design system
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ─── Labels: Status badges, metadata (Inter — functional) ─────
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)
