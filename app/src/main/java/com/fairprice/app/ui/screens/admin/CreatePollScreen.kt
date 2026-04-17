package com.fairprice.app.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.CreatePollRequest
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing
import kotlinx.coroutines.launch

/**
 * Create Poll Screen — Dynamic custom poll creation.
 *
 * Officers define:
 *  1. Title & description
 *  2. Target level (District/Subdivision/Block/Village)
 *  3. Target code (LGD code within their jurisdiction)
 *  4. 2-5 dynamic poll options (e.g. "Yes"/"No", or multi-choice)
 *
 * Jurisdiction is enforced server-side by the RBAC middleware.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    onPollCreated: () -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetLevel by remember { mutableStateOf("") }
    var targetCode by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") } // Min 2 options
    var levelExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val targetLevels = listOf("DISTRICT", "SUBDIVISION", "BLOCK", "VILLAGE")

    LaunchedEffect(Unit) { isVisible = true }

    val isFormValid = title.isNotBlank() &&
            targetLevel.isNotBlank() &&
            targetCode.isNotBlank() &&
            options.size >= 2 &&
            options.all { it.isNotBlank() }

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
                    text = "Create Custom Poll",
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
                    text = "Create a custom poll with dynamic options. Citizens in the target area will see the poll in their app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Poll Title ──────────────────────────────
                SectionLabel("Poll Title *")
                SoftTrayInput(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = "Title",
                    placeholder = "e.g. Did you receive your wheat ration this month?",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Description ──────────────────────────────
                SectionLabel("Description (optional)")
                SoftTrayInput(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Provide additional context for citizens",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Target Level Dropdown ───────────────────
                SectionLabel("Target Level *")
                Column {
                    SoftTrayInput(
                        value = targetLevel.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        onValueChange = { },
                        label = "Select Level",
                        placeholder = "District / Subdivision / Block / Village",
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
                                        text = level.lowercase()
                                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
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

                // ─── Target Code ─────────────────────────────
                SectionLabel("LGD Target Code *")
                SoftTrayInput(
                    value = targetCode,
                    onValueChange = { targetCode = it; errorMessage = null },
                    label = if (targetLevel.isNotBlank())
                        "${targetLevel.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Code"
                    else "Target Code",
                    placeholder = "Enter the LGD code for the target area",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Dynamic Options Builder ────────────────
                SectionLabel("Poll Options (2–5) *")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Citizens will choose one of these options when voting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    ) {
                        // Option number badge
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                            modifier = Modifier.size(32.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        SoftTrayInput(
                            value = option,
                            onValueChange = { options[index] = it },
                            label = "Option ${index + 1}",
                            placeholder = when (index) {
                                0 -> "e.g. Yes, received full"
                                1 -> "e.g. No, did not receive"
                                2 -> "e.g. Received partial"
                                3 -> "e.g. Shop was closed"
                                else -> "e.g. Other"
                            },
                            imeAction = if (index == options.lastIndex) ImeAction.Done else ImeAction.Next,
                            modifier = Modifier.weight(1f),
                        )

                        // Remove button (only if > 2 options)
                        if (options.size > 2) {
                            IconButton(
                                onClick = { options.removeAt(index) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove option",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                // Add option button
                if (options.size < 5) {
                    TextButton(
                        onClick = { options.add("") },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add option",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Option")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ─── Summary Card ──────────────────────────
                if (isFormValid) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Poll Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"$title\" will be sent to all citizens in ${
                                    targetLevel.lowercase().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    }
                                } with LGD code $targetCode.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Options: ${options.filter { it.isNotBlank() }.joinToString(" • ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ─── Error / Success Messages ────────────
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                successMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ─── Submit Button ──────────────────────────
                GradientButton(
                    text = if (isLoading) "Creating…" else "Create Poll",
                    onClick = {
                        if (!isFormValid || isLoading) return@GradientButton
                        isLoading = true
                        errorMessage = null
                        successMessage = null

                        scope.launch {
                            try {
                                val result = ApiRepository.createPoll(
                                    CreatePollRequest(
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        targetLevel = targetLevel,
                                        targetCode = targetCode.toIntOrNull() ?: 0,
                                        options = options.filter { it.isNotBlank() }.map { it.trim() },
                                    )
                                )
                                when (result) {
                                    is NetworkResult.Success -> {
                                        if (result.data.success) {
                                            successMessage = result.data.message
                                            kotlinx.coroutines.delay(1200)
                                            onPollCreated()
                                        } else {
                                            errorMessage = result.data.message
                                        }
                                    }
                                    is NetworkResult.Error -> {
                                        errorMessage = result.message
                                    }
                                    else -> {}
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = isFormValid && !isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Polls are scoped to your jurisdiction. The backend will reject polls targeting areas outside your assigned LGD region.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))
}
