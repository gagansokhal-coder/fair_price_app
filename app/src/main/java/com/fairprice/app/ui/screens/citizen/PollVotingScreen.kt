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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.network.ApiErrorResponse
import com.fairprice.app.network.RetrofitClient
import com.fairprice.app.network.CustomPoll
import com.fairprice.app.network.PollSubmitRequest
import com.fairprice.app.ui.components.GradientButton
import com.google.gson.Gson
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.DeviceUtils
import kotlinx.coroutines.launch

/**
 * Poll Voting Screen — Dynamic custom poll with selectable options.
 *
 * Shows poll details (title, description, target info),
 * renders 2-5 dynamic radio-style option cards,
 * performs location verification, and submits to backend.
 *
 * Anti-fraud checks:
 * 1. Android-side: Mock location detection (isMockProvider).
 * 2. Server-side: PostGIS ST_DistanceSphere geofence (optional for custom polls).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollVotingScreen(
    pollId: String,
    onSubmitSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var poll by remember { mutableStateOf<CustomPoll?>(null) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var isLocationVerified by remember { mutableStateOf(false) }
    var isLocationChecking by remember { mutableStateOf(true) }
    var isMockDetected by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasAlreadyVoted by remember { mutableStateOf(false) }
    var alreadyVotedText by remember { mutableStateOf<String?>(null) }
    var isPollLoading by remember { mutableStateOf(true) }

    // GPS coordinates
    var gpsLat by remember { mutableDoubleStateOf(0.0) }
    var gpsLng by remember { mutableDoubleStateOf(0.0) }
    var locationText by remember { mutableStateOf("Verifying your location…") }

    LaunchedEffect(Unit) { isVisible = true }

    // Load poll details
    LaunchedEffect(pollId) {
        try {
            val resp = RetrofitClient.apiService.getActivePolls()
            if (resp.isSuccessful) {
                poll = resp.body()?.polls?.find { it.pollId == pollId }
            }
        } catch (_: Exception) {
            // Fallback mock for offline dev
            poll = CustomPoll(
                pollId = pollId,
                title = "Did you receive wheat ration this month?",
                description = "Verify your FPS distributed the correct amount.",
                targetLevel = "BLOCK",
                targetCode = 280,
                options = listOf("Yes, full quantity", "Partial quantity", "Not received", "Shop was closed"),
                isActive = true,
                createdAt = "2026-04-16",
                createdBy = null,
            )
        } finally {
            isPollLoading = false
        }
    }

    // REAL location verification using FusedLocationProviderClient
    LaunchedEffect(Unit) {
        try {
            val location = DeviceUtils.getCurrentLocation(context)
            if (location != null) {
                // Anti-spoofing: Check for mock location
                if (DeviceUtils.isMockLocation(location)) {
                    isMockDetected = true
                    isLocationChecking = false
                    isLocationVerified = false
                    locationText = "⛔ Mock location detected — submission blocked"
                } else {
                    gpsLat = location.latitude
                    gpsLng = location.longitude
                    isLocationChecking = false
                    isLocationVerified = true
                    locationText = "GPS: ${String.format("%.4f", gpsLat)}, ${String.format("%.4f", gpsLng)}"
                }
            } else {
                isLocationChecking = false
                isLocationVerified = false
                locationText = "⚠️ GPS unavailable — enable location services"
            }
        } catch (e: Exception) {
            isLocationChecking = false
            isLocationVerified = false
            locationText = "⚠️ Location permission required"
        }
    }

    val canSubmit = selectedOptionIndex >= 0 && isLocationVerified && !isSubmitting && !isMockDetected

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
                    text = "Vote",
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

        if (isPollLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (poll == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Poll not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400, easing = SteadyPulseEasing)) +
                        slideInVertically(tween(400, easing = SteadyPulseEasing)) { it / 4 },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ─── Poll Title Card ──────────────────────
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = poll!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            if (!poll!!.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = poll!!.description!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${poll!!.targetLevel} • Code: ${poll!!.targetCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ─── Dynamic Options ──────────────────────
                    Text(
                        text = "Select your response",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    poll!!.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            animationSpec = tween(200),
                            label = "option_bg_$index",
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            animationSpec = tween(200),
                            label = "option_border_$index",
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { selectedOptionIndex = index },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Radio icon
                                Icon(
                                    imageVector = if (isSelected) Icons.Rounded.CheckCircle
                                    else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Not selected",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                // Option label
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Location Status ─────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMockDetected -> MaterialTheme.colorScheme.errorContainer
                                isLocationVerified -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isLocationChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = if (isMockDetected) Icons.Rounded.Warning
                                    else if (isLocationVerified) Icons.Rounded.LocationOn
                                    else Icons.Rounded.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isMockDetected) MaterialTheme.colorScheme.error
                                    else if (isLocationVerified) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isLocationChecking) "Verifying location…"
                                    else if (isMockDetected) "Mock Location Detected"
                                    else if (isLocationVerified) "Location Verified"
                                    else "Location Unavailable",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = locationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Error Message ────────────────────────
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ─── Already Voted Banner ─────────────────
                    if (hasAlreadyVoted) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "You already voted: \"${alreadyVotedText ?: "—"}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ─── Submit Button ────────────────────────
                    GradientButton(
                        text = when {
                            isSubmitting -> "Submitting…"
                            hasAlreadyVoted -> "Already Voted"
                            else -> "Submit Vote"
                        },
                        onClick = {
                            if (!canSubmit || hasAlreadyVoted) return@GradientButton
                            isSubmitting = true
                            errorMessage = null

                            scope.launch {
                                try {
                                    val resp = RetrofitClient.apiService.submitPoll(
                                        PollSubmitRequest(
                                            pollId = pollId,
                                            selectedOptionIndex = selectedOptionIndex,
                                            gpsLat = gpsLat,
                                            gpsLng = gpsLng,
                                        )
                                    )
                                    if (resp.isSuccessful && resp.body()?.success == true) {
                                        onSubmitSuccess()
                                    } else {
                                        // Parse structured error from server
                                        val errBody = resp.errorBody()?.string()
                                        val apiErr = try {
                                            Gson().fromJson(errBody, ApiErrorResponse::class.java)
                                        } catch (_: Exception) { null }

                                        when (apiErr?.error) {
                                            "ALREADY_VOTED" -> {
                                                hasAlreadyVoted = true
                                                alreadyVotedText = apiErr.message
                                                    .substringAfter("voted '")
                                                    .substringBefore("' on")
                                                    .ifBlank { null }
                                                errorMessage = "You have already voted on this poll."
                                            }
                                            "RATE_LIMITED" -> {
                                                errorMessage = "⏳ Too many attempts. Wait 60 seconds before trying again."
                                            }
                                            "NOT_IN_TARGET_AREA" -> {
                                                errorMessage = "🚫 This poll is not targeted at your area."
                                            }
                                            "POLL_EXPIRED" -> {
                                                errorMessage = "⏰ This poll has expired."
                                            }
                                            "POLL_CLOSED" -> {
                                                errorMessage = "This poll is no longer active."
                                            }
                                            else -> {
                                                errorMessage = apiErr?.message
                                                    ?: "Failed to submit vote. Please try again."
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network error: ${e.localizedMessage}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = canSubmit && !hasAlreadyVoted,
                    )

                    if (isMockDetected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⛔ Voting is blocked because a mock location provider was detected. Disable any GPS spoofing apps and try again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
