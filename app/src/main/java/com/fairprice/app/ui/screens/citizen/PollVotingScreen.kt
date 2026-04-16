package com.fairprice.app.ui.screens.citizen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.InfoSlab
import com.fairprice.app.ui.components.StatusBadge
import com.fairprice.app.ui.components.BadgeType
import com.fairprice.app.ui.components.VerificationPulse
import com.fairprice.app.ui.theme.FairPriceColors
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.ui.theme.steadyPulseSpec
import kotlinx.coroutines.delay

/**
 * Poll Voting Screen — Core anti-corruption mechanism.
 *
 * Shows poll details, yes/no option for ration receipt,
 * location verification with geofence check, and submit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollVotingScreen(
    pollId: String,
    onSubmitSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var receivedRation by remember { mutableStateOf<Boolean?>(null) }
    var isLocationVerified by remember { mutableStateOf(false) }
    var isLocationChecking by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    // Simulate location verification
    LaunchedEffect(Unit) {
        delay(2500)
        isLocationChecking = false
        isLocationVerified = true // Mock: pretend location is valid
    }

    // Handle submission
    LaunchedEffect(isSubmitting) {
        if (isSubmitting) {
            delay(1500) // Simulate API call
            onSubmitSuccess()
        }
    }

    val canSubmit = receivedRation != null && isLocationVerified && !isSubmitting

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
                    text = "Verify Ration",
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

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400, easing = SteadyPulseEasing)) +
                    slideInVertically(tween(400, easing = SteadyPulseEasing)) { it / 3 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                // Poll Details Card
                FairPriceCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                contentDescription = "Commodity",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Wheat (5 kg)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "April 2026 Distribution",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoSlab(
                        icon = Icons.Rounded.Store,
                        title = "Fair Price Shop",
                        value = "Rampur FPS #127",
                        subtitle = "Dealer: Suresh Verma",
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location Verification
                FairPriceCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = if (isLocationVerified) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Location Check",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (isLocationChecking) {
                                    "Verifying your location…"
                                } else if (isLocationVerified) {
                                    "Within 100m of FPS"
                                } else {
                                    "You must be at the FPS"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (isLocationChecking) {
                            VerificationPulse(size = 32.dp)
                        } else {
                            StatusBadge(
                                text = if (isLocationVerified) "Verified" else "Failed",
                                type = if (isLocationVerified) BadgeType.SUCCESS else BadgeType.ERROR,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ration Receipt Question
                Text(
                    text = "Did you receive your ration?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReceiptOption(
                        icon = Icons.Rounded.CheckCircle,
                        label = "Yes, Received",
                        isSelected = receivedRation == true,
                        onClick = { receivedRation = true },
                        selectedColor = MaterialTheme.colorScheme.primary,
                        selectedBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f),
                    )
                    ReceiptOption(
                        icon = Icons.Rounded.Cancel,
                        label = "No, Not Received",
                        isSelected = receivedRation == false,
                        onClick = { receivedRation = false },
                        selectedColor = MaterialTheme.colorScheme.error,
                        selectedBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Warning when ration not received
                if (receivedRation == false) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FairPriceCard(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your response will be flagged for investigation by the district administration.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                if (isSubmitting) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        VerificationPulse(
                            size = 60.dp,
                            label = "Submitting response…",
                        )
                    }
                } else {
                    GradientButton(
                        text = "Submit Response",
                        onClick = { isSubmitting = true },
                        enabled = canSubmit,
                    )

                    if (!isLocationVerified && !isLocationChecking) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⚠ You must be physically present at the Fair Price Shop to submit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ReceiptOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: androidx.compose.ui.graphics.Color,
    selectedBg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else MaterialTheme.colorScheme.surfaceContainerLowest,
        animationSpec = steadyPulseSpec(),
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        animationSpec = steadyPulseSpec(),
        label = "optionBorder"
    )

    Box(
        modifier = modifier
            .clip(ShapeTokens.Card)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = ShapeTokens.Card,
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
