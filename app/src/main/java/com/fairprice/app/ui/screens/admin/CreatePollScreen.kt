package com.fairprice.app.ui.screens.admin

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
import androidx.compose.material.icons.rounded.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing

/**
 * Create Poll Screen — Admin creates targeted polls.
 *
 * Select target level (State/District/Block/Panchayat/FPS),
 * target code, commodity, and dispatch the poll + FCM notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    onPollCreated: () -> Unit,
    onBack: () -> Unit,
) {
    var targetLevel by remember { mutableStateOf("") }
    var targetCode by remember { mutableStateOf("") }
    var commodity by remember { mutableStateOf("") }
    var levelExpanded by remember { mutableStateOf(false) }
    var commodityExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val targetLevels = listOf("State", "District", "Block", "Panchayat", "FPS")
    val commodities = listOf("Wheat", "Rice", "Sugar", "Kerosene", "Pulses")

    LaunchedEffect(Unit) { isVisible = true }

    val isFormValid = targetLevel.isNotBlank() && targetCode.isNotBlank() && commodity.isNotBlank()

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
                    text = "Create New Poll",
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
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Configure the poll parameters. All beneficiaries in the target area will receive push notifications.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Target Level Dropdown
                Text(
                    text = "Target Level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    SoftTrayInput(
                        value = targetLevel,
                        onValueChange = { },
                        label = "Select Level",
                        placeholder = "State / District / Block / Panchayat / FPS",
                        enabled = false,
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
                            .clickable { levelExpanded = true },
                    )

                    DropdownMenu(
                        expanded = levelExpanded,
                        onDismissRequest = { levelExpanded = false },
                    ) {
                        targetLevels.forEach { level ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = level,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                onClick = {
                                    targetLevel = level
                                    levelExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Target Code
                Text(
                    text = "Target Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                SoftTrayInput(
                    value = targetCode,
                    onValueChange = { targetCode = it },
                    label = if (targetLevel.isNotBlank()) "$targetLevel Code" else "Target Code",
                    placeholder = "Enter the LGD code for the target area",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Commodity Dropdown
                Text(
                    text = "Commodity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    SoftTrayInput(
                        value = commodity,
                        onValueChange = { },
                        label = "Select Commodity",
                        placeholder = "Wheat / Rice / Sugar / Kerosene",
                        enabled = false,
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
                            .clickable { commodityExpanded = true },
                    )

                    DropdownMenu(
                        expanded = commodityExpanded,
                        onDismissRequest = { commodityExpanded = false },
                    ) {
                        commodities.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                onClick = {
                                    commodity = item
                                    commodityExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Summary
                if (isFormValid) {
                    Text(
                        text = "Summary: A poll for $commodity will be sent to all beneficiaries in $targetLevel: $targetCode via push notification.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                GradientButton(
                    text = "Create & Notify",
                    onClick = onPollCreated,
                    enabled = isFormValid,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Push notifications will be sent via FCM to all registered citizens in the target area.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
