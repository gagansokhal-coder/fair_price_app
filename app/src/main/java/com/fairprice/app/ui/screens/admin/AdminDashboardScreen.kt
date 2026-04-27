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
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairprice.app.R
import com.fairprice.app.network.AnalyticsSummaryResponse
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.CustomPoll
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.PollListResponse
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.OutlinedActionButton
import com.fairprice.app.ui.components.StatCard
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.SessionManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.NumberFormat
import java.util.Locale

/**
 * Admin Dashboard — Command center for district/block officers.
 *
 * Shows real-time metrics from the analytics API, action buttons
 * for creating polls and zone analysis, and recent poll list
 * fetched from the backend.
 */

@Composable
fun AdminDashboardScreen(
    onCreatePoll: () -> Unit,
    onManageOfficers: () -> Unit,
    onPollAnalytics: () -> Unit,
    onZoneAnalysis: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var summary by remember { mutableStateOf<AnalyticsSummaryResponse?>(null) }
    var recentPolls by remember { mutableStateOf<List<CustomPoll>>(emptyList()) }

    val context = LocalContext.current
    val session = remember { SessionManager.getInstance(context) }
    val officerName = session.getOfficerName().ifEmpty { stringResource(R.string.officer_name_placeholder) }
    val designation = session.getOfficerDesignation().ifEmpty { stringResource(R.string.admin) }
    val districtName = session.getOfficerDistrictName()

    // Fetch real data from backend
    LaunchedEffect(Unit) {
        isVisible = true
        try {
            coroutineScope {
                val summaryDeferred = async { ApiRepository.getAnalyticsSummary() }
                val pollsDeferred = async { ApiRepository.getPollAnalytics() }

                val summaryResult = summaryDeferred.await()
                val pollsResult = pollsDeferred.await()

                if (summaryResult is NetworkResult.Success) {
                    summary = summaryResult.data
                }
                if (pollsResult is NetworkResult.Success) {
                    recentPolls = pollsResult.data.polls.take(5) // Show latest 5
                }
            }
        } catch (_: Exception) {
            // Graceful degradation — show zeros
        } finally {
            isLoading = false
        }
    }

    val fmt = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    val totalResponses = summary?.totalResponses ?: 0
    val totalZones = summary?.totalZones ?: 0
    val redZones = summary?.redZones ?: 0
    val avgPositive = summary?.avgPositivePct ?: 0.0

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
                        text = stringResource(R.string.welcome_officer_name, officerName),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = if (districtName.isNotEmpty()) "$designation • $districtName" else designation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Stats Grid — REAL DATA
        item {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        icon = Icons.Rounded.HowToVote,
                        label = stringResource(R.string.total_responses),
                        value = fmt.format(totalResponses),
                        modifier = Modifier.weight(1f),
                        subtitle = stringResource(R.string.all_polls_combined),
                    )
                    StatCard(
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        label = stringResource(R.string.positive_rate),
                        value = "${avgPositive}%",
                        modifier = Modifier.weight(1f),
                        subtitle = stringResource(R.string.avg_across_zones),
                        trend = if (avgPositive >= 70) stringResource(R.string.healthy) else "",
                    )
                }
            }
        }

        item {
            if (!isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        icon = Icons.Rounded.Warning,
                        label = stringResource(R.string.red_zones),
                        value = "$redZones",
                        modifier = Modifier.weight(1f),
                        subtitle = stringResource(R.string.below_40_positive),
                        trend = if (redZones > 0) stringResource(R.string.action_needed) else stringResource(R.string.none_label),
                        trendColor = if (redZones > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f),
                    )
                    StatCard(
                        icon = Icons.Rounded.Analytics,
                        label = stringResource(R.string.total_zones),
                        value = "$totalZones",
                        modifier = Modifier.weight(1f),
                        subtitle = stringResource(R.string.monitored_areas),
                    )
                }
            }
        }

        // Action Buttons
        item {
            GradientButton(
                text = stringResource(R.string.create_poll),
                onClick = onCreatePoll,
            )
        }

        item {
            OutlinedActionButton(
                text = stringResource(R.string.poll_analytics),
                onClick = onPollAnalytics,
            )
        }

        item {
            OutlinedActionButton(
                text = stringResource(R.string.manage_officers),
                onClick = onManageOfficers,
            )
        }

        item {
            OutlinedActionButton(
                text = stringResource(R.string.zone_analysis),
                onClick = onZoneAnalysis,
            )
        }

        // Recent Polls — REAL DATA from backend
        item {
            Text(
                text = stringResource(R.string.recent_polls),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            }
        } else if (recentPolls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Poll,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.no_polls_created_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.recent_polls_empty_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(recentPolls) { poll ->
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
                                contentDescription = poll.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = poll.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.poll_target_date, poll.targetLevel, poll.targetCode, poll.createdAt.take(10)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.poll_responses_options, poll.totalResponses, poll.options.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }

                        StatusBadge(
                            text = if (poll.isActive) stringResource(R.string.active) else stringResource(R.string.closed),
                            type = if (poll.isActive) BadgeType.SUCCESS else BadgeType.INFO,
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
