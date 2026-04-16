package com.fairprice.app.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.OutlinedActionButton
import com.fairprice.app.ui.components.StatCard
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.theme.SteadyPulseEasing

/**
 * Admin Dashboard — Command center for district/block officers.
 *
 * Shows key metrics (response rate, discrepancies), action buttons
 * for creating polls and zone analysis, and recent poll list.
 */

private data class RecentPoll(
    val commodity: String,
    val targetLevel: String,
    val targetName: String,
    val date: String,
    val responses: Int,
    val totalBeneficiaries: Int,
    val discrepancies: Int,
)

private val mockRecentPolls = listOf(
    RecentPoll("Wheat", "Block", "Sadar", "12 Apr 2026", 342, 500, 18),
    RecentPoll("Rice", "District", "Lucknow", "10 Apr 2026", 1240, 2000, 45),
    RecentPoll("Sugar", "Panchayat", "Rampur", "08 Apr 2026", 87, 120, 3),
)

@Composable
fun AdminDashboardScreen(
    onCreatePoll: () -> Unit,
    onManageOfficers: () -> Unit,
    onPollAnalytics: () -> Unit,
    onZoneAnalysis: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                )
            ),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400, easing = SteadyPulseEasing)) +
                        slideInVertically(tween(400, easing = SteadyPulseEasing)) { it / 3 },
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Admin Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "District Magistrate • Lucknow",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Rounded.HowToVote,
                    label = "Total Responses",
                    value = "1,669",
                    modifier = Modifier.weight(1f),
                    trend = "↑ 12%",
                )
                StatCard(
                    icon = Icons.Rounded.TrendingUp,
                    label = "Response Rate",
                    value = "68%",
                    modifier = Modifier.weight(1f),
                    subtitle = "Last 30 days",
                    trend = "↑ 5%",
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Rounded.Warning,
                    label = "Discrepancies",
                    value = "66",
                    modifier = Modifier.weight(1f),
                    subtitle = "Needs attention",
                    trend = "↓ 8%",
                    trendColor = MaterialTheme.colorScheme.primary,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f),
                )
                StatCard(
                    icon = Icons.Rounded.People,
                    label = "Beneficiaries",
                    value = "2,620",
                    modifier = Modifier.weight(1f),
                    subtitle = "Active NFSA",
                )
            }
        }

        // Action Buttons
        item {
            GradientButton(
                text = "Create New Poll",
                onClick = onCreatePoll,
            )
        }

        item {
            OutlinedActionButton(
                text = "Poll Analytics",
                onClick = onPollAnalytics,
            )
        }

        item {
            OutlinedActionButton(
                text = "Manage Officers",
                onClick = onManageOfficers,
            )
        }

        item {
            OutlinedActionButton(
                text = "Zone Analysis",
                onClick = onZoneAnalysis,
            )
        }

        // Recent Polls
        item {
            Text(
                text = "Recent Polls",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        items(mockRecentPolls) { poll ->
            FairPriceCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Grain,
                            contentDescription = poll.commodity,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = poll.commodity,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${poll.targetLevel}: ${poll.targetName} • ${poll.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${poll.responses}/${poll.totalBeneficiaries} responses • ${poll.discrepancies} flagged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }

                    StatusBadge(
                        text = if (poll.discrepancies > 10) "Alert" else "Normal",
                        type = if (poll.discrepancies > 10) BadgeType.WARNING else BadgeType.SUCCESS,
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
