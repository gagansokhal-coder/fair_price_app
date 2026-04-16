package com.fairprice.app.ui.theme

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

// ─── Extended Palette (Custom tokens for app-specific use) ─────
object FairPriceColors {
    // Gradient endpoints for primary CTA buttons
    val GradientStart = Primary           // #0d631b
    val GradientEnd = PrimaryContainer    // #2e7d32

    // Status Colors
    val StatusSuccess = Primary
    val StatusWarning = Color(0xFFF57C00)  // Amber
    val StatusDanger = Error
    val StatusInfo = Color(0xFF1976D2)     // Blue

    // Glassmorphism
    val GlassBackground = Surface.copy(alpha = 0.80f)

    // Ghost Border (15% opacity of outline-variant)
    val GhostBorder = OutlineVariant.copy(alpha = 0.15f)

    // Shimmer for loading states
    val ShimmerBase = SurfaceContainerLow
    val ShimmerHighlight = SurfaceContainerLowest

    // Verification states
    val VerifiedGreen = PrimaryFixed
    val DiscrepancyAmber = TertiaryFixed
}
