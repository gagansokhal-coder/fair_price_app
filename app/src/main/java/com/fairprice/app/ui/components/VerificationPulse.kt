package com.fairprice.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.theme.PULSE_DURATION
import com.fairprice.app.ui.theme.PrimaryFixed
import com.fairprice.app.ui.theme.PrimaryFixedDim
import com.fairprice.app.ui.theme.SteadyPulseEasing

/**
 * "Verification Pulse" — Custom progress indicator.
 *
 * Instead of a standard spinner, uses a pulsing circular ring in primary_fixed
 * that expands and fades, mimicking a heartbeat — representing the "living"
 * nature of the ration system.
 *
 * Design spec:
 * - Pulsing ring in primary_fixed
 * - Expands and fades cyclically
 * - Heartbeat metaphor
 */
@Composable
fun VerificationPulse(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    label: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "verificationPulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_DURATION,
                easing = SteadyPulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_DURATION,
                easing = SteadyPulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_DURATION + 200,
                easing = SteadyPulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerAlpha"
    )

    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_DURATION + 200,
                easing = SteadyPulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size * 1.5f)
        ) {
            // Outer expanding ring (fades out as it expands)
            Canvas(modifier = Modifier.size(size * outerScale)) {
                drawCircle(
                    color = PrimaryFixedDim.copy(alpha = outerAlpha),
                    radius = this.size.minDimension / 2,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Main pulsing ring
            Canvas(modifier = Modifier.size(size * scale)) {
                drawCircle(
                    color = PrimaryFixed.copy(alpha = alpha),
                    radius = this.size.minDimension / 2,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Inner filled dot
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = PrimaryFixed.copy(alpha = alpha),
                )
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
