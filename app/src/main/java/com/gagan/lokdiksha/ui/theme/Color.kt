package com.gagan.lokdiksha.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PDS Fair Price App — "The Dignified Anchor" Color System
 *
 * Derived from the Stitch "fairprice" design system.
 * Palette rooted in government authority, executed with modern sophistication.
 *
 * Key Principle: "Verdant Depth" — greens that convey growth, trust, and integrity.
 */

// ─── Primary: "Growth Green" ───────────────────────────────────
val Primary = Color(0xFF0D631B)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF2E7D32)
val OnPrimaryContainer = Color(0xFFCBFFC2)
val PrimaryFixed = Color(0xFFA3F69C)
val PrimaryFixedDim = Color(0xFF88D982)

// ─── Secondary: "Forest Mist" ──────────────────────────────────
val Secondary = Color(0xFF476644)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFC6E9BE)
val OnSecondaryContainer = Color(0xFF4C6A48)
val SecondaryFixed = Color(0xFFC9ECC1)
val SecondaryFixedDim = Color(0xFFADD0A6)

// ─── Tertiary: "Alert Rose" ────────────────────────────────────
val Tertiary = Color(0xFF923357)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFB14B6F)
val OnTertiaryContainer = Color(0xFFFFEDF0)
val TertiaryFixed = Color(0xFFFFD9E2)
val TertiaryFixedDim = Color(0xFFFFB1C7)

// ─── Error ─────────────────────────────────────────────────────
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// ─── Surface Tiers: "Fine Paper Stack" ─────────────────────────
// Base Layer
val Surface = Color(0xFFF9F9F9)
val OnSurface = Color(0xFF1A1C1C)
val SurfaceBright = Color(0xFFF9F9F9)
val SurfaceDim = Color(0xFFDADADA)

// Container Tiers (Physical paper analogy)
val SurfaceContainerLowest = Color(0xFFFFFFFF)   // Max pop — Cards
val SurfaceContainerLow = Color(0xFFF3F3F3)      // Section backgrounds
val SurfaceContainer = Color(0xFFEEEEEE)          // Mid-tier
val SurfaceContainerHigh = Color(0xFFE8E8E8)      // Prominent containers
val SurfaceContainerHighest = Color(0xFFE2E2E2)   // Input field fills

// Surface Variants
val SurfaceVariant = Color(0xFFE2E2E2)
val OnSurfaceVariant = Color(0xFF40493D)
val SurfaceTint = Color(0xFF1B6D24)

// ─── Inverse ───────────────────────────────────────────────────
val InverseSurface = Color(0xFF2F3131)
val InverseOnSurface = Color(0xFFF1F1F1)
val InversePrimary = Color(0xFF88D982)

// ─── Outline ───────────────────────────────────────────────────
val Outline = Color(0xFF707A6C)
val OutlineVariant = Color(0xFFBFCABA)

// ─── Background ────────────────────────────────────────────────
val Background = Color(0xFFF9F9F9)
val OnBackground = Color(0xFF1A1C1C)

// ─── Scrim ─────────────────────────────────────────────────────
val Scrim = Color(0xFF000000)

// ═══════════════════════════════════════════════════════════════
// ─── DARK MODE: "Midnight Canopy" ──────────────────────────────
// Deep blacks with emerald luminance — dignified night identity.
// ═══════════════════════════════════════════════════════════════

val DarkPrimary = Color(0xFF88D982)
val DarkOnPrimary = Color(0xFF003909)
val DarkPrimaryContainer = Color(0xFF0A5115)
val DarkOnPrimaryContainer = Color(0xFFA3F69C)

val DarkSecondary = Color(0xFFADD0A6)
val DarkOnSecondary = Color(0xFF1A3718)
val DarkSecondaryContainer = Color(0xFF304E2D)
val DarkOnSecondaryContainer = Color(0xFFC9ECC1)

val DarkTertiary = Color(0xFFFFB1C7)
val DarkOnTertiary = Color(0xFF5C1133)
val DarkTertiaryContainer = Color(0xFF7A2849)
val DarkOnTertiaryContainer = Color(0xFFFFD9E2)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkSurface = Color(0xFF121412)
val DarkOnSurface = Color(0xFFE2E3DE)
val DarkSurfaceBright = Color(0xFF383A37)
val DarkSurfaceDim = Color(0xFF121412)

val DarkSurfaceContainerLowest = Color(0xFF0D0F0D)
val DarkSurfaceContainerLow = Color(0xFF1A1C1A)
val DarkSurfaceContainer = Color(0xFF1E201E)
val DarkSurfaceContainerHigh = Color(0xFF292B28)
val DarkSurfaceContainerHighest = Color(0xFF343633)

val DarkSurfaceVariant = Color(0xFF414941)
val DarkOnSurfaceVariant = Color(0xFFC1C9BC)
val DarkSurfaceTint = Color(0xFF88D982)

val DarkInverseSurface = Color(0xFFE2E3DE)
val DarkInverseOnSurface = Color(0xFF2F3131)
val DarkInversePrimary = Color(0xFF0D631B)

val DarkOutline = Color(0xFF8B9387)
val DarkOutlineVariant = Color(0xFF414941)

val DarkBackground = Color(0xFF121412)
val DarkOnBackground = Color(0xFFE2E3DE)

// ─── Extended Palette (Custom tokens for app-specific use) ─────
object FairPriceColors {
    // Gradient endpoints for primary CTA buttons
    val GradientStart = Primary           // #0d631b
    val GradientEnd = PrimaryContainer    // #2e7d32

    // Dark mode gradient
    val DarkGradientStart = DarkPrimary
    val DarkGradientEnd = DarkPrimaryContainer

    // Status Colors
    val StatusSuccess = Primary
    val StatusWarning = Color(0xFFF57C00)  // Amber
    val StatusDanger = Error
    val StatusInfo = Color(0xFF1976D2)     // Blue

    // Glassmorphism
    val GlassBackground = Surface.copy(alpha = 0.80f)
    val DarkGlassBackground = DarkSurface.copy(alpha = 0.85f)

    // Ghost Border (15% opacity of outline-variant)
    val GhostBorder = OutlineVariant.copy(alpha = 0.15f)

    // Shimmer for loading states
    val ShimmerBase = SurfaceContainerLow
    val ShimmerHighlight = SurfaceContainerLowest

    // Verification states
    val VerifiedGreen = PrimaryFixed
    val DiscrepancyAmber = TertiaryFixed
}
