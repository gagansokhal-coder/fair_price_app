package com.gagan.lokdiksha.ui.screens.citizen

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
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.R
import com.gagan.lokdiksha.network.ApiRepository
import com.gagan.lokdiksha.network.CustomPoll
import com.gagan.lokdiksha.network.MyPollResponse
import com.gagan.lokdiksha.network.NetworkResult
import com.gagan.lokdiksha.ui.components.FairPriceCard
import com.gagan.lokdiksha.ui.components.InfoSlab
import com.gagan.lokdiksha.ui.components.StatCard
import com.gagan.lokdiksha.ui.components.StatusBadge
import com.gagan.lokdiksha.ui.components.BadgeType
import com.gagan.lokdiksha.ui.theme.ShapeTokens
import com.gagan.lokdiksha.ui.theme.SteadyPulseEasing
import com.gagan.lokdiksha.utils.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.Calendar

/**
 * Citizen Home Dashboard — Main landing after login.
 *
 * Shows greeting, active polls from backend, stats from
 * real API data, and quick actions.
 */

@Composable
fun HomeDashboardScreen(
    onPollClick: (String) -> Unit,
    onComplaintClick: () -> Unit,
    onFeedbackClick: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var activePolls by remember { mutableStateOf<List<CustomPoll>>(emptyList()) }
    var myResponses by remember { mutableStateOf<List<MyPollResponse>>(emptyList()) }

    val context = LocalContext.current
    val session = remember { SessionManager.getInstance(context) }
    val citizenName = session.getCitizenName().ifEmpty { stringResource(R.string.citizen) }

    // Fetch real data from backend
    LaunchedEffect(Unit) {
        isVisible = true
        try {
            coroutineScope {
                val pollsDeferred = async { ApiRepository.getActivePolls() }
                val historyDeferred = async { ApiRepository.getMyResponses() }

                val pollsResult = pollsDeferred.await()
                val historyResult = historyDeferred.await()

                if (pollsResult is NetworkResult.Success) {
                    activePolls = pollsResult.data.polls
                }
                if (historyResult is NetworkResult.Success) {
                    myResponses = historyResult.data.responses
                }
            }
        } catch (_: Exception) {
            // Graceful degradation
        } finally {
            isLoading = false
        }
    }

    val greetingMorning = stringResource(R.string.good_morning)
    val greetingAfternoon = stringResource(R.string.good_afternoon)
    val greetingEvening = stringResource(R.string.good_evening)
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> greetingMorning
            hour < 17 -> greetingAfternoon
            else -> greetingEvening
        }
    }

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
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = citizenName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Stats Row — real data
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Rounded.HowToVote,
                    label = stringResource(R.string.polls_responded),
                    value = if (isLoading) "…" else "${myResponses.size}",
                    modifier = Modifier.weight(1f),
                    subtitle = stringResource(R.string.total_votes_cast),
                )
                StatCard(
                    icon = Icons.Rounded.Campaign,
                    label = stringResource(R.string.pending),
                    value = if (isLoading) "…" else "${activePolls.size}",
                    modifier = Modifier.weight(1f),
                    subtitle = stringResource(R.string.active_polls_label),
                    trendColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        // Active Polls Section
        item {
            Text(
                text = stringResource(R.string.active_polls),
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
        } else if (activePolls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Poll,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_active_polls),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.check_back_polls),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(activePolls) { poll ->
                FairPriceCard(
                    onClick = { onPollClick(poll.pollId) },
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
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.options_format, poll.options.size, poll.targetLevel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        StatusBadge(text = stringResource(R.string.active), type = BadgeType.SUCCESS)
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = stringResource(R.string.quick_actions),
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
                    title = stringResource(R.string.file_complaint),
                    onClick = onComplaintClick,
                    modifier = Modifier.weight(1f),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                )
                QuickActionCard(
                    icon = Icons.Rounded.Feedback,
                    title = stringResource(R.string.give_feedback),
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
