package com.fairprice.app.ui.screens.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairprice.app.R
import com.fairprice.app.network.AnalyticsSummaryResponse
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.ZoneClassificationResponse
import com.fairprice.app.network.ZoneEntry
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.StatCard
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.theme.ShapeTokens
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Zone Analysis & Analytics Dashboard — Phases 5-8
 *
 * Real-time visualization of poll results with:
 *  - Summary stats (total responses, zone distribution)
 *  - Donut-style pie chart for overall positive/negative split
 *  - Zone classification list (GREEN/YELLOW/RED)
 *  - Bar-level comparison with progress indicators
 */

// Zone colors
val ZoneGreen = Color(0xFF22C55E)
val ZoneYellow = Color(0xFFF59E0B)
val ZoneRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneAnalysisScreen(
    onBack: () -> Unit,
) {
    var summaryData by remember { mutableStateOf<AnalyticsSummaryResponse?>(null) }
    var zoneData by remember { mutableStateOf<ZoneClassificationResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Fetch data from backend
    LaunchedEffect(Unit) {
        try {
            coroutineScope {
                val summaryDeferred = async { ApiRepository.getAnalyticsSummary() }
                val zonesDeferred = async { ApiRepository.getZoneClassification() }

                val summaryResult = summaryDeferred.await()
                val zonesResult = zonesDeferred.await()

                if (summaryResult is NetworkResult.Success) summaryData = summaryResult.data
                if (zonesResult is NetworkResult.Success) zoneData = zonesResult.data

                // Show error if both failed
                if (summaryResult is NetworkResult.Error && zonesResult is NetworkResult.Error) {
                    errorMsg = summaryResult.message
                }
            }
        } catch (e: Exception) {
            errorMsg = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

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
                    text = stringResource(R.string.analytics_and_zones),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            ),
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMsg != null -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.failed_to_load_analytics, errorMsg ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                AnalyticsContent(summaryData, zoneData)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    summary: AnalyticsSummaryResponse?,
    zones: ZoneClassificationResponse?,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ─── Overview Stats Row ──────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Rounded.Analytics,
                    label = stringResource(R.string.responses),
                    value = "${summary?.totalResponses ?: 0}",
                    modifier = Modifier.weight(1f),
                    subtitle = stringResource(R.string.total_votes_cast),
                )
                StatCard(
                    icon = Icons.Rounded.Warning,
                    label = stringResource(R.string.red_zones),
                    value = "${zones?.redZones ?: 0}",
                    modifier = Modifier.weight(1f),
                    subtitle = stringResource(R.string.below_40_positive),
                    iconTint = ZoneRed,
                    iconBackground = ZoneRed.copy(alpha = 0.12f),
                )
            }
        }

        // ─── Donut Chart: Overall Positive/Negative Split ─
        if (summary != null && summary.totalResponses > 0) {
            item {
                FairPriceCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.overall_positive_rate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DonutChart(
                            positivePct = summary.avgPositivePct.toFloat(),
                            modifier = Modifier.size(180.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            LegendItem(color = ZoneGreen, label = stringResource(R.string.positive_label), value = "${summary.avgPositivePct}%")
                            LegendItem(
                                color = ZoneRed,
                                label = stringResource(R.string.negative_label),
                                value = stringResource(R.string.percentage_one_decimal, (100.0 - summary.avgPositivePct))
                            )
                        }
                    }
                }
            }
        }

        // ─── Zone Distribution Bar ──────────────────────
        if (zones != null && zones.totalZones > 0) {
            item {
                FairPriceCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(
                            text = stringResource(R.string.zone_distribution),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Stacked bar showing GREEN/YELLOW/RED proportions
                        val totalZones = zones.totalZones.toFloat()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            if (zones.greenZones > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(zones.greenZones / totalZones)
                                        .height(24.dp)
                                        .background(ZoneGreen)
                                )
                            }
                            if (zones.yellowZones > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(zones.yellowZones / totalZones)
                                        .height(24.dp)
                                        .background(ZoneYellow)
                                )
                            }
                            if (zones.redZones > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(zones.redZones / totalZones)
                                        .height(24.dp)
                                        .background(ZoneRed)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ZoneCountBadge("🟢", stringResource(R.string.green), zones.greenZones, ZoneGreen)
                            ZoneCountBadge("🟡", stringResource(R.string.yellow), zones.yellowZones, ZoneYellow)
                            ZoneCountBadge("🔴", stringResource(R.string.red), zones.redZones, ZoneRed)
                        }
                    }
                }
            }
        }

        // ─── Zone Classification List ───────────────────
        item {
            Text(
                text = stringResource(R.string.zone_classification),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        val zoneList = zones?.zones ?: emptyList()
        if (zoneList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_zone_data_detailed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(zoneList.sortedByDescending { it.totalResponses }) { zone ->
                ZoneCard(zone)
            }
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPOSABLES: Custom Charts & Zone Cards
// ═══════════════════════════════════════════════════════════════

/**
 * Animated donut chart showing positive percentage.
 */
@Composable
private fun DonutChart(
    positivePct: Float,
    modifier: Modifier = Modifier,
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val sweepAngle by animateFloatAsState(
        targetValue = if (animationPlayed) positivePct / 100f * 360f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "donut_sweep"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    val zoneColor = when {
        positivePct >= 70f -> ZoneGreen
        positivePct >= 40f -> ZoneYellow
        else -> ZoneRed
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 28f
            val padding = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            // Background track
            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Positive arc
            drawArc(
                color = zoneColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        // Center label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.percentage_one_decimal, positivePct),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = zoneColor,
            )
            Text(
                text = stringResource(R.string.positive_lowercase),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ZoneCountBadge(emoji: String, label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Individual zone card showing area name, zone badge, response count,
 * and an animated progress bar for positive percentage.
 */
@Composable
private fun ZoneCard(zone: ZoneEntry) {
    val zoneColor = when (zone.zone) {
        "GREEN" -> ZoneGreen
        "YELLOW" -> ZoneYellow
        "RED" -> ZoneRed
        else -> Color.Gray
    }

    val badgeType = when (zone.zone) {
        "GREEN" -> BadgeType.SUCCESS
        "YELLOW" -> BadgeType.WARNING
        else -> BadgeType.ERROR
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) (zone.positivePct / 100f).toFloat() else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "zone_progress_${zone.areaCode}"
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    FairPriceCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Zone color indicator circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(zoneColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (zone.zone) {
                        "GREEN" -> Icons.Rounded.Check
                        "RED" -> Icons.Rounded.Warning
                        else -> Icons.Rounded.LocationCity
                    },
                    contentDescription = zone.zone,
                    tint = zoneColor,
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = zone.areaName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (zone.parentName != null) {
                            Text(
                                text = zone.parentName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    StatusBadge(
                        text = zone.zone,
                        type = badgeType,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Animated progress bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(ShapeTokens.StatusBadge),
                    color = zoneColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.responses_count, zone.totalResponses),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.positive_pct_short, zone.positivePct.toFloat()),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = zoneColor,
                    )
                }
            }
        }
    }
}
