package com.fairprice.app.ui.screens.citizen

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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.InfoSlab
import com.fairprice.app.ui.components.StatCard
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.SteadyPulseEasing

/**
 * Citizen Home Dashboard — Main landing after login.
 *
 * Shows greeting, FPS info, active polls, and quick actions.
 * Uses mock data — ready for Supabase/Retrofit integration.
 */

// Mock data
private data class MockPoll(
    val id: String,
    val commodity: String,
    val fpsName: String,
    val date: String,
    val isActive: Boolean,
)

private val mockPolls = listOf(
    MockPoll("poll_1", "Wheat (5 kg)", "Rampur FPS #127", "12 Apr 2026", true),
    MockPoll("poll_2", "Rice (3 kg)", "Rampur FPS #127", "10 Apr 2026", true),
    MockPoll("poll_3", "Sugar (1 kg)", "Rampur FPS #127", "08 Apr 2026", false),
)

@Composable
fun HomeDashboardScreen(
    onPollClick: (String) -> Unit,
    onComplaintClick: () -> Unit,
    onFeedbackClick: () -> Unit,
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
        // Greeting Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400, easing = SteadyPulseEasing)) +
                        slideInVertically(tween(400, easing = SteadyPulseEasing)) { it / 3 },
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Good Afternoon",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = "Rajesh Kumar",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Your FPS Info
        item {
            InfoSlab(
                icon = Icons.Rounded.Store,
                title = "Your Fair Price Shop",
                value = "Rampur FPS #127",
                subtitle = "Dealer: Suresh Verma • Panchayat: Rampur",
            )
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Rounded.HowToVote,
                    label = "Polls Responded",
                    value = "7",
                    modifier = Modifier.weight(1f),
                    subtitle = "This month",
                    trend = "↑ 3",
                )
                StatCard(
                    icon = Icons.Rounded.Campaign,
                    label = "Pending",
                    value = "2",
                    modifier = Modifier.weight(1f),
                    subtitle = "Active polls",
                    trendColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        // Active Polls Section
        item {
            Text(
                text = "Active Polls",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        items(mockPolls.filter { it.isActive }) { poll ->
            FairPriceCard(
                onClick = { onPollClick(poll.id) },
            ) {
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${poll.fpsName} • ${poll.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    StatusBadge(text = "Active", type = BadgeType.SUCCESS)
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionCard(
                    icon = Icons.Rounded.ReportProblem,
                    title = "File Complaint",
                    onClick = onComplaintClick,
                    modifier = Modifier.weight(1f),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                )
                QuickActionCard(
                    icon = Icons.Rounded.Feedback,
                    title = "Give Feedback",
                    onClick = onFeedbackClick,
                    modifier = Modifier.weight(1f),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    iconBg: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ShapeTokens.Card,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
