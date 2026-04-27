package com.fairprice.app.ui.screens.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Phone
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.fairprice.app.network.LoginRequest
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.R
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.DeviceUtils

private const val TAG = "CitizenLoginScreen"

/**
 * Citizen Login Screen — Ration Card + Phone authentication.
 *
 * Validates 12-digit ration card and 10-digit phone number
 * before requesting OTP via the Go backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenLoginScreen(
    onNavigateToVerification: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var rationCardNo by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var rationCardError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // GPS state for login verification — optional, never blocks login
    var gpsLat by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var gpsLng by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var gpsStatus by remember { mutableStateOf("Acquiring GPS…") }

    LaunchedEffect(Unit) { isVisible = true }

    // Fetch GPS coordinates on screen launch — fully guarded
    LaunchedEffect(Unit) {
        try {
            val location = DeviceUtils.getCurrentLocation(context)
            if (location != null) {
                if (DeviceUtils.isMockLocation(location)) {
                    gpsStatus = "⚠️ Mock location detected"
                } else {
                    gpsLat = location.latitude
                    gpsLng = location.longitude
                    gpsStatus = "✅ GPS: ${String.format("%.4f", gpsLat)}, ${String.format("%.4f", gpsLng)}"
                }
            } else {
                gpsStatus = "⚠️ GPS unavailable"
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "GPS permission not granted", e)
            gpsStatus = "⚠️ GPS permission required"
        } catch (e: Exception) {
            Log.w(TAG, "GPS fetch failed", e)
            gpsStatus = "⚠️ GPS unavailable"
        }
    }

    val isFormValid = rationCardNo.length == 12 && phoneNumber.length == 10

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
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
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
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.citizen_login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.enter_ration_card),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Ration Card Input
                SoftTrayInput(
                    value = rationCardNo,
                    onValueChange = { input ->
                        if (input.length <= 12 && input.all { it.isDigit() }) {
                            rationCardNo = input
                            rationCardError = null
                        }
                    },
                    label = stringResource(R.string.ration_card_no),
                    placeholder = stringResource(R.string.ration_card_hint),
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    isError = rationCardError != null,
                    errorMessage = rationCardError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Badge,
                            contentDescription = stringResource(R.string.ration_card_no),
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Phone Number Input
                SoftTrayInput(
                    value = phoneNumber,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            phoneNumber = input
                            phoneError = null
                        }
                    },
                    label = stringResource(R.string.phone_number),
                    placeholder = stringResource(R.string.enter_phone_hint),
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = stringResource(R.string.phone_number),
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Login Button
                GradientButton(
                    text = if (isLoading) stringResource(R.string.sending_otp) else stringResource(R.string.get_otp),
                    onClick = {
                        var hasError = false
                        if (rationCardNo.length != 12) {
                            rationCardError = context.getString(R.string.ration_card_error)
                            hasError = true
                        }
                        if (phoneNumber.length != 10) {
                            phoneError = context.getString(R.string.phone_error)
                            hasError = true
                        }
                        if (!hasError) {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val hardwareUuid = try {
                                        DeviceUtils.getHardwareUuid(context)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed to get hardware UUID", e)
                                        "unknown-device"
                                    }

                                    val result = ApiRepository.login(
                                        LoginRequest(
                                            rationCardNo = rationCardNo,
                                            phoneNo = phoneNumber,
                                            hardwareUuid = hardwareUuid,
                                            gpsLat = gpsLat,
                                            gpsLng = gpsLng,
                                        )
                                    )
                                    when (result) {
                                        is NetworkResult.Success -> {
                                            try {
                                                onNavigateToVerification(phoneNumber, rationCardNo)
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Navigation to verification failed", e)
                                                rationCardError = "Navigation error. Please try again."
                                            }
                                        }
                                        is NetworkResult.Error -> {
                                            rationCardError = result.message
                                        }
                                        else -> {}
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Login request failed", e)
                                    rationCardError = "Something went wrong. Please try again."
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = isFormValid && !isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "By continuing, you agree to the PDS verification process.\nYour location will be verified during poll submission.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
