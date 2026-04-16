package com.fairprice.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * PDS Fair Price App — "Steady Pulse" Animation System
 *
 * All state transitions use 300ms with CubicBezier(0.4, 0, 0.2, 1).
 * This reflects the calm, reliable nature of a government monitor.
 */

// Core easing — "The Steady Pulse"
val SteadyPulseEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

// Standard transition duration
const val TRANSITION_DURATION = 300
const val ENTER_DURATION = 400
const val EXIT_DURATION = 250
const val SPLASH_DURATION = 2000
const val PULSE_DURATION = 1200

// Animation spec factories
fun <T> steadyPulseSpec() = tween<T>(
    durationMillis = TRANSITION_DURATION,
    easing = SteadyPulseEasing
)

fun <T> enterSpec() = tween<T>(
    durationMillis = ENTER_DURATION,
    easing = SteadyPulseEasing
)

fun <T> exitSpec() = tween<T>(
    durationMillis = EXIT_DURATION,
    easing = SteadyPulseEasing
)

// Spring spec for bouncy interactions
fun <T> bouncySpec() = spring<T>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Pulsing alpha animation for the "Verification Pulse" loader.
 * Mimics a heartbeat — representing the "living" nature of the ration system.
 */
@Composable
fun rememberPulseAlpha(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 0.3f,
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
}

/**
 * Pulsing scale animation for the "Verification Pulse" ring.
 */
@Composable
fun rememberPulseScale(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseScale")
    return infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PULSE_DURATION,
                easing = SteadyPulseEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
}
