package com.fairprice.app.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.VerificationPulse
import com.fairprice.app.ui.theme.FairPriceColors
import com.fairprice.app.ui.theme.SPLASH_DURATION
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.SessionManager
import kotlinx.coroutines.delay

/**
 * Splash Screen — "The Dignified Anchor" entrance.
 *
 * Animated branding with verification pulse ring.
 * Checks for existing session:
 *   - If session exists → auto-navigate to CitizenHome or AdminDashboard
 *   - If no session → navigate to RoleSelection
 */
@Composable
fun SplashScreen(
    onNavigateToRoleSelection: () -> Unit,
    onNavigateToCitizenHome: (() -> Unit)? = null,
    onNavigateToAdminDashboard: (() -> Unit)? = null,
) {
    var isVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val titleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = SteadyPulseEasing),
        label = "titleAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400, easing = SteadyPulseEasing),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(SPLASH_DURATION.toLong())

        val session = SessionManager.getInstance(context)
        if (session.hasSession()) {
            // Persistent session exists — navigate directly to the correct dashboard
            if (session.isOfficer()) {
                onNavigateToAdminDashboard?.invoke() ?: onNavigateToRoleSelection()
            } else {
                onNavigateToCitizenHome?.invoke() ?: onNavigateToRoleSelection()
            }
        } else {
            // No session — fresh start
            onNavigateToRoleSelection()
        }
    }

    Box(
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
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Pulsing verification ring with shield icon
            Box(contentAlignment = Alignment.Center) {
                VerificationPulse(size = 100.dp)
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "Shield",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // App name
            Text(
                text = "Ration Prahari",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(titleAlpha),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Protecting Your Entitlements",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(taglineAlpha),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Public Distribution System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.alpha(taglineAlpha),
            )
        }

        // Bottom branding
        Text(
            text = "Government of India Initiative",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha),
        )
    }
}
