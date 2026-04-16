package com.fairprice.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing

/**
 * Officer Login Screen — Credential-based auth for DM/SDO/BDO.
 *
 * Officer ID + Password + Designation dropdown.
 * Validates against Supabase Auth (mock for now).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var officerId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedDesignation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var designationExpanded by remember { mutableStateOf(false) }
    var officerIdError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var designationError by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    val designations = listOf(
        "District Magistrate (DM)",
        "Sub-Divisional Officer (SDO)",
        "Block Development Officer (BDO)",
    )

    LaunchedEffect(Unit) { isVisible = true }

    val isFormValid = officerId.isNotBlank() && password.length >= 6 && selectedDesignation.isNotBlank()

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
                    text = "Officer Login",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Authenticate with your official credentials to access the admin dashboard.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Officer ID
                SoftTrayInput(
                    value = officerId,
                    onValueChange = {
                        officerId = it
                        officerIdError = null
                    },
                    label = "Officer ID",
                    placeholder = "Enter your officer ID",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    isError = officerIdError != null,
                    errorMessage = officerIdError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = "Officer ID",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password
                SoftTrayInput(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = "Password",
                    placeholder = "Enter your password",
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
                            contentDescription = "Password",
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
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Designation Dropdown
                Column {
                    SoftTrayInput(
                        value = selectedDesignation.ifEmpty { "" },
                        onValueChange = { },
                        label = "Designation",
                        placeholder = "Select designation",
                        enabled = false,
                        isError = designationError != null,
                        errorMessage = designationError,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { designationExpanded = true },
                    )

                    DropdownMenu(
                        expanded = designationExpanded,
                        onDismissRequest = { designationExpanded = false },
                    ) {
                        designations.forEach { designation ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = designation,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                onClick = {
                                    selectedDesignation = designation
                                    designationExpanded = false
                                    designationError = null
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Login Button
                GradientButton(
                    text = "Login",
                    onClick = {
                        var hasError = false
                        if (officerId.isBlank()) {
                            officerIdError = "Officer ID is required"
                            hasError = true
                        }
                        if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters"
                            hasError = true
                        }
                        if (selectedDesignation.isBlank()) {
                            designationError = "Please select a designation"
                            hasError = true
                        }
                        if (!hasError) {
                            // Mock login — direct to admin dashboard
                            onLoginSuccess()
                        }
                    },
                    enabled = isFormValid,
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
