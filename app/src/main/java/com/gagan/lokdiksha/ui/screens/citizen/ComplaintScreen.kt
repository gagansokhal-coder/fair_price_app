package com.gagan.lokdiksha.ui.screens.citizen

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
import androidx.compose.material.icons.rounded.Store
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.R
import com.gagan.lokdiksha.ui.components.GradientButton
import com.gagan.lokdiksha.ui.components.SoftTrayInput
import com.gagan.lokdiksha.ui.theme.SteadyPulseEasing

/**
 * Complaint Screen — File complaints against FPS irregularities.
 *
 * Pre-filled FPS from user profile, complaint type dropdown,
 * and description text area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintScreen(
    onSubmitSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    var complaintType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val complaintTypes = listOf(
        stringResource(R.string.complaint_ration_not_received),
        stringResource(R.string.complaint_short_quantity),
        stringResource(R.string.complaint_poor_quality),
        stringResource(R.string.complaint_overcharging),
        stringResource(R.string.complaint_shop_not_open),
        stringResource(R.string.complaint_dealer_misbehavior),
        stringResource(R.string.complaint_other),
    )

    LaunchedEffect(Unit) { isVisible = true }

    val isFormValid = complaintType.isNotBlank() && description.length >= 10

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
                    text = stringResource(R.string.complaint_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            },
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
                Spacer(modifier = Modifier.height(8.dp))

                // FPS Info (pre-filled)
                SoftTrayInput(
                    value = "Rampur FPS #127",
                    onValueChange = { },
                    label = stringResource(R.string.fair_price_shop),
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Store,
                            contentDescription = stringResource(R.string.fps_icon),
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Complaint Type Dropdown
                Column {
                    SoftTrayInput(
                        value = complaintType,
                        onValueChange = { },
                        label = stringResource(R.string.complaint_type),
                        placeholder = stringResource(R.string.select_complaint_type),
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = stringResource(R.string.dropdown_icon),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = true },
                    )

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        complaintTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                onClick = {
                                    complaintType = type
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                SoftTrayInput(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.complaint_description),
                    placeholder = stringResource(R.string.complaint_desc_detail_hint),
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                    singleLine = false,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.characters_count, description.length),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = stringResource(R.string.submit_complaint),
                    onClick = onSubmitSuccess,
                    enabled = isFormValid,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.complaint_review_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
