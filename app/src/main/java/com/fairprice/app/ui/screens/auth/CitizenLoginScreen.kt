package com.fairprice.app.ui.screens.auth

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.DeviceUtils

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
    val context = androidx.compose.ui.platform.LocalContext.current

    // GPS state for login verification
    var gpsLat by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var gpsLng by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var gpsStatus by remember { mutableStateOf("Acquiring GPS…") }

    LaunchedEffect(Unit) { isVisible = true }

    // Fetch GPS coordinates on screen launch
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
        } catch (_: Exception) {
            gpsStatus = "⚠️ GPS permission required"
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
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Citizen Login",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your Ration Card number and registered mobile number to receive an OTP.",
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
                    label = "Ration Card Number",
                    placeholder = "Enter 12-digit number",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    isError = rationCardError != null,
                    errorMessage = rationCardError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Badge,
                            contentDescription = "Ration Card",
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
                    label = "Phone Number",
                    placeholder = "Enter 10-digit mobile number",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Login Button
                GradientButton(
                    text = if (isLoading) "Requesting..." else "Get OTP",
                    onClick = {
                        var hasError = false
                        if (rationCardNo.length != 12) {
                            rationCardError = "Ration card must be 12 digits"
                            hasError = true
                        }
                        if (phoneNumber.length != 10) {
                            phoneError = "Phone number must be 10 digits"
                            hasError = true
                        }
                        if (!hasError) {
                            isLoading = true
                            coroutineScope.launch {
                                val hardwareUuid = DeviceUtils.getHardwareUuid(context)
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
                                    is NetworkResult.Success -> onNavigateToVerification(phoneNumber, rationCardNo)
                                    is NetworkResult.Error -> rationCardError = result.message
                                    else -> {}
                                }
                                isLoading = false
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
