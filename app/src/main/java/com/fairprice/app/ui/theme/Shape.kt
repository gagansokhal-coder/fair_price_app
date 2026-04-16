package com.fairprice.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * PDS Fair Price App — Shape System
 *
 * Design system uses ROUND_EIGHT base with specific overrides:
 * - Buttons: rounded-xl (24dp) or pill (9999dp)
 * - Cards: xl (12dp) — no sharp corners ever
 * - Input fields: md (12dp) — "Soft Tray" approach
 * - Status badges: full (9999dp) — pill-shaped
 */
val FairPriceShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// Custom shape tokens used across components
object ShapeTokens {
    val Card = RoundedCornerShape(16.dp)
    val CardLarge = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(24.dp)
    val ButtonPill = RoundedCornerShape(9999.dp)
    val InputField = RoundedCornerShape(12.dp)
    val StatusBadge = RoundedCornerShape(9999.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Dialog = RoundedCornerShape(28.dp)
    val Chip = RoundedCornerShape(9999.dp)
}
