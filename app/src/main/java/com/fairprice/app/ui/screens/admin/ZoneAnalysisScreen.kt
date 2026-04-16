package com.fairprice.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.StatCard
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.theme.ShapeTokens

/**
 * Zone Analysis Screen — Geographical breakdown of poll responses.
 *
 * Shows zone-wise summary stats, response rates, and discrepancy
 * indicators across administrative areas.
 */

private data class ZoneData(
    val name: String,
    val level: String,
    val totalBeneficiaries: Int,
    val responses: Int,
    val discrepancies: Int,
) {
    val responseRate: Float get() = if (totalBeneficiaries > 0) responses.toFloat() / totalBeneficiaries else 0f
}

private val mockZones = listOf(
    ZoneData("Sadar Block", "Block", 500, 342, 18),
    ZoneData("Mohanlalganj Block", "Block", 380, 298, 12),
    ZoneData("Malihabad Block", "Block", 420, 210, 35),
    ZoneData("Bakshi Ka Talab Block", "Block", 350, 290, 8),
    ZoneData("Chinhat Block", "Block", 480, 100, 42),
    ZoneData("Kakori Block", "Block", 290, 240, 5),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneAnalysisScreen(
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Zone Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            ),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Overview Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        icon = Icons.Rounded.Analytics,
                        label = "Avg Response",
                        value = "62%",
                        modifier = Modifier.weight(1f),
                        subtitle = "Across all zones",
                    )
                    StatCard(
                        icon = Icons.Rounded.Warning,
                        label = "Hotspots",
                        value = "2",
                        modifier = Modifier.weight(1f),
                        subtitle = "Below 50% rate",
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f),
                    )
                }
            }

            item {
                Text(
                    text = "Zone Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            items(mockZones.sortedByDescending { it.responseRate }) { zone ->
                ZoneCard(zone)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ZoneCard(zone: ZoneData) {
    val responsePercent = (zone.responseRate * 100).toInt()
    val isLowResponse = responsePercent < 50

    FairPriceCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLowResponse) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationCity,
                    contentDescription = zone.name,
                    tint = if (isLowResponse) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    StatusBadge(
                        text = if (isLowResponse) "Low" else "Good",
                        type = if (isLowResponse) BadgeType.WARNING else BadgeType.SUCCESS,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { zone.responseRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(ShapeTokens.StatusBadge),
                    color = if (isLowResponse) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${zone.responses}/${zone.totalBeneficiaries} responses ($responsePercent%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${zone.discrepancies} flagged",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (zone.discrepancies > 20) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                    )
                }
            }
        }
    }
}
