package com.gagan.lokdiksha.ui.screens.auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.R
import com.gagan.lokdiksha.ui.components.VerificationPulse
import com.gagan.lokdiksha.ui.theme.FairPriceColors
import com.gagan.lokdiksha.ui.theme.SPLASH_DURATION
import com.gagan.lokdiksha.ui.theme.SteadyPulseEasing
import android.util.Log
import com.gagan.lokdiksha.utils.SessionManager
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"

/**
 * Splash Screen — "The Dignified Anchor" entrance.
 *
 * Animated branding with verification pulse ring.
 * Checks for existing session:
 *   - If session exists → auto-navigate to CitizenHome or AdminDashboard
 *   - If no session → navigate to RoleSelection
 *
 * CRASH FIX (v2):
 * - Wrapped entire session check in try-catch to survive corrupted session data
 * - If userId is empty/null with incomplete profile, clears session to prevent
 *   infinite "app won't open" loop
 */
@Composable
fun SplashScreen(
    onNavigateToRoleSelection: () -> Unit,
    onNavigateToCitizenHome: (() -> Unit)? = null,
    onNavigateToAdminDashboard: (() -> Unit)? = null,
    onNavigateToProfileSetup: ((userId: String) -> Unit)? = null,
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

        try {
            val session = SessionManager.getInstance(context)
            if (session.hasSession()) {
                Log.d(TAG, "Session found: isOfficer=${session.isOfficer()}, profileComplete=${session.isProfileComplete()}, userId=${session.getUserId()?.take(8)}")
                // Persistent session exists — navigate directly to the correct dashboard
                if (session.isOfficer()) {
                    onNavigateToAdminDashboard?.invoke() ?: onNavigateToRoleSelection()
                } else {
                    // ── Profile Gate: Citizen must complete profile before entering app ──
                    val userId = session.getUserId()
                    if (userId.isNullOrBlank()) {
                        // Corrupted session — no userId saved. Clear and restart.
                        Log.w(TAG, "Corrupted session: hasSession=true but userId is null/blank. Clearing session.")
                        session.clearSession()
                        onNavigateToRoleSelection()
                    } else if (!session.isProfileComplete()) {
                        // Profile NOT complete — redirect to profile setup
                        Log.d(TAG, "Profile incomplete, redirecting to setup for userId=$userId")
                        onNavigateToProfileSetup?.invoke(userId) ?: onNavigateToRoleSelection()
                    } else {
                        Log.d(TAG, "Profile complete, navigating to citizen home")
                        onNavigateToCitizenHome?.invoke() ?: onNavigateToRoleSelection()
                    }
                }
            } else {
                // No session — fresh start
                Log.d(TAG, "No session found, navigating to role selection")
                onNavigateToRoleSelection()
            }
        } catch (e: Exception) {
            // If anything goes wrong reading session, clear it and start fresh
            Log.e(TAG, "Session check crashed, clearing session", e)
            try {
                SessionManager.getInstance(context).clearSession()
            } catch (_: Exception) { }
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
                    contentDescription = stringResource(R.string.shield),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // App name
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(titleAlpha),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(taglineAlpha),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.pds_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.alpha(taglineAlpha),
            )
        }

        // Bottom branding
        Text(
            text = stringResource(R.string.govt_initiative),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha),
        )
    }
}
