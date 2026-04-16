package com.fairprice.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.steadyPulseSpec

/**
 * "Information Slab" — Base card following the "No-Line" rule.
 *
 * Design rules:
 * - NO 1px solid borders to define sections
 * - Hierarchy via background shifts (tonal layering)
 * - surface-container-lowest (white) cards on surface-container-low backgrounds
 * - 16dp internal padding, 16dp rounded corners
 * - Animated content size changes
 */
@Composable
fun FairPriceCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentPadding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = ShapeTokens.Card,
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .animateContentSize(animationSpec = steadyPulseSpec()),
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = ShapeTokens.Card,
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .animateContentSize(animationSpec = steadyPulseSpec()),
                content = content
            )
        }
    }
}

/**
 * Large card variant with extra rounding for hero sections.
 */
@Composable
fun FairPriceCardLarge(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeTokens.CardLarge,
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .animateContentSize(animationSpec = steadyPulseSpec()),
            content = content
        )
    }
}
