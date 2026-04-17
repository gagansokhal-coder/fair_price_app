package com.fairprice.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.VerificationPulse
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.VerifyOtpRequest
import com.fairprice.app.utils.SessionManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/**
 * OTP Verification Screen — 6-digit code entry.
 *
 * Features:
 * - Individual digit boxes for each OTP digit
 * - Auto-submit when 6th digit is entered
 * - 30s countdown timer for resend
 * - VerificationPulse during verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    phone: String,
    onVerificationSuccess: (String) -> Unit,
    onBack: () -> Unit,
) {
    var otpValue by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { isVisible = true }

    // Countdown timer
    LaunchedEffect(canResend) {
        if (!canResend) {
            countdown = 30
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            canResend = true
        }
    }

    // Auto-submit when OTP is 6 digits
    LaunchedEffect(otpValue) {
        if (otpValue.length == 6 && !isVerifying) {
            isVerifying = true
            errorMessage = null
            val result = ApiRepository.verifyOtp(VerifyOtpRequest(phone, otpValue))
            when (result) {
                is NetworkResult.Success -> {
                    val body = result.data
                    if (body.verified) {
                        // Save JWT securely
                        SessionManager.getInstance(context).saveAuthData(
                            accessToken = body.accessToken,
                            userId = body.userId
                        )
                        onVerificationSuccess(body.userId)
                    } else {
                        errorMessage = "Invalid OTP or expired."
                        otpValue = ""
                        isVerifying = false
                    }
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message
                    otpValue = ""
                    isVerifying = false
                }
                else -> {}
            }
        }
    }

    val maskedPhone = if (phone.length >= 10) {
        "XXXXXX${phone.takeLast(4)}"
    } else {
        phone
    }

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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verify OTP",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the 6-digit code sent to $maskedPhone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isVerifying) {
                    // Show verification pulse
                    VerificationPulse(
                        size = 80.dp,
                        label = "Verifying…",
                    )
                } else {
                    // OTP Input — 6 individual digit boxes
                    BasicTextField(
                        value = otpValue,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                otpValue = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                repeat(6) { index ->
                                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                                    val isFocused = index == otpValue.length

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(ShapeTokens.InputField)
                                            .background(
                                                if (isFocused) {
                                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                                }
                                            )
                                            .then(
                                                if (isFocused) {
                                                    Modifier.border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = ShapeTokens.InputField,
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = char,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                        )
                                    }

                                    if (index < 5) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Resend timer
                    if (canResend) {
                        TextButton(
                            onClick = {
                                canResend = false
                                otpValue = ""
                            }
                        ) {
                            Text(
                                text = "Resend OTP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        Text(
                            text = "Resend OTP in ${countdown}s",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
