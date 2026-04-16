package com.fairprice.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.theme.FairPriceColors
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.PrimaryFixed
import com.fairprice.app.ui.theme.OnPrimaryContainer
import com.fairprice.app.ui.theme.TertiaryFixed
import com.fairprice.app.ui.theme.OnTertiaryContainer
import com.fairprice.app.ui.theme.ErrorContainer
import com.fairprice.app.ui.theme.OnErrorContainer
import com.fairprice.app.ui.theme.SecondaryContainer
import com.fairprice.app.ui.theme.OnSecondaryContainer

/**
 * High-Contrast Status Badge — pill-shaped indicator.
 *
 * Design rules:
 * - Pill-shaped (radius 9999dp) to contrast against softer card corners
 * - Oversized minimum 32px height for first-impression visibility
 * - Verified: on-primary-container text on primary-fixed background
 * - Discrepancy: on-tertiary-container text on tertiary-fixed background
 */
enum class BadgeType {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = when (type) {
        BadgeType.SUCCESS -> PrimaryFixed to OnPrimaryContainer
        BadgeType.WARNING -> TertiaryFixed to OnTertiaryContainer
        BadgeType.ERROR -> ErrorContainer to OnErrorContainer
        BadgeType.INFO -> SecondaryContainer to OnSecondaryContainer
        BadgeType.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHighest to
                MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(ShapeTokens.StatusBadge)
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
        )
    }
}
