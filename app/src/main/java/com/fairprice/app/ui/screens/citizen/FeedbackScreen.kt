package com.fairprice.app.ui.screens.citizen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.FairPriceColors
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.ui.theme.steadyPulseSpec

/**
 * Feedback Screen — Star rating + optional comment.
 *
 * Citizens rate their FPS experience (1-5 stars) and can
 * add an optional text comment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onSubmitSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    val ratingLabels = listOf("", "Very Poor", "Poor", "Average", "Good", "Excellent")

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
                    text = "Give Feedback",
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
                    slideInVertically(tween(400, easing = SteadyPulseEasing)) { it / 4 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Rate Your Experience",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "How was your last visit to\nRampur FPS #127?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (i in 1..5) {
                        val starColor by animateColorAsState(
                            targetValue = if (i <= rating) {
                                Color(0xFFFFC107) // Amber gold
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            },
                            animationSpec = steadyPulseSpec(),
                            label = "star$i"
                        )

                        Icon(
                            imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Star $i",
                            tint = starColor,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .clickable { rating = i }
                                .padding(4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (rating > 0) {
                    Text(
                        text = ratingLabels[rating],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Optional Comment
                SoftTrayInput(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "Comment (optional)",
                    placeholder = "Share additional feedback…",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                    singleLine = false,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )

                Spacer(modifier = Modifier.height(40.dp))

                GradientButton(
                    text = "Submit Feedback",
                    onClick = onSubmitSuccess,
                    enabled = rating > 0,
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
