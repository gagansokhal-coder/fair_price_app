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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairprice.app.R
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.OfficerLoginRequest
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Officer Login Screen — Phone + Password authentication for DM/SDO/BDO.
 *
 * Authenticates against the Go backend which verifies the officer's
 * phone number exists in the officers table and returns a JWT with
 * their role and jurisdiction information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) { isVisible = true }

    val isFormValid = phoneNumber.length == 10 && password.length >= 6

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
                    text = stringResource(R.string.officer_login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.enter_officer_credentials),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Phone Number Input
                SoftTrayInput(
                    value = phoneNumber,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            phoneNumber = input
                            phoneError = null
                            loginError = null
                        }
                    },
                    label = stringResource(R.string.phone_number),
                    placeholder = stringResource(R.string.enter_phone_hint),
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
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

                Spacer(modifier = Modifier.height(20.dp))

                // Password Input
                SoftTrayInput(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        loginError = null
                    },
                    label = stringResource(R.string.password),
                    placeholder = stringResource(R.string.enter_password_hint),
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.password),
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password),
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Login error message
                if (loginError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loginError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Login Button
                GradientButton(
                    text = if (isLoading) stringResource(R.string.authenticating) else stringResource(R.string.login),
                    onClick = {
                        var hasError = false
                        if (phoneNumber.length != 10) {
                            phoneError = context.getString(R.string.phone_error)
                            hasError = true
                        }
                        if (password.length < 6) {
                            passwordError = context.getString(R.string.password_error)
                            hasError = true
                        }
                        if (!hasError) {
                            isLoading = true
                            loginError = null
                            coroutineScope.launch {
                                val result = ApiRepository.officerLogin(
                                    OfficerLoginRequest(
                                        phoneNo = phoneNumber,
                                        password = password,
                                    )
                                )
                                when (result) {
                                    is NetworkResult.Success -> {
                                        val body = result.data
                                        if (body.success) {
                                            // Save officer auth data to session
                                            SessionManager.getInstance(context).saveOfficerAuthData(
                                                accessToken = body.accessToken,
                                                officerId = body.officerId,
                                                name = body.name,
                                                role = body.role,
                                                designation = body.designation,
                                                districtName = body.districtName,
                                            )
                                            onLoginSuccess()
                                        } else {
                                            loginError = body.message
                                        }
                                    }
                                    is NetworkResult.Error -> {
                                        loginError = result.message
                                    }
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
                    text = stringResource(R.string.officer_login_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
