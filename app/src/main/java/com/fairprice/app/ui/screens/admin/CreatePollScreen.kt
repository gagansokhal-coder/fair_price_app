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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fairprice.app.R
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.LgdItem
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.network.CreatePollRequest
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.SteadyPulseEasing
import kotlinx.coroutines.launch

/**
 * Create Poll Screen — Dynamic custom poll creation with cascading LGD dropdowns.
 *
 * Officers define:
 *  1. Title & description
 *  2. Target level (District/Subdivision/Block/Village)
 *  3. Target area selected via cascading dropdowns (fetched from LGD hierarchy)
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
    val options = remember { mutableStateListOf("", "") } // Min 2 options
    var levelExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // ── Cascading LGD Dropdown State ──────────────────────
    var districts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var subdistricts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var blocks by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var villages by remember { mutableStateOf<List<LgdItem>>(emptyList()) }

    var selectedDistrict by remember { mutableStateOf<LgdItem?>(null) }
    var selectedSubdistrict by remember { mutableStateOf<LgdItem?>(null) }
    var selectedBlock by remember { mutableStateOf<LgdItem?>(null) }
    var selectedVillage by remember { mutableStateOf<LgdItem?>(null) }

    var districtExpanded by remember { mutableStateOf(false) }
    var subdistrictExpanded by remember { mutableStateOf(false) }
    var blockExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    var lgdLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val targetLevels = listOf("DISTRICT", "SUBDIVISION", "BLOCK", "VILLAGE")

    // Derive the final target code based on selected level
    val resolvedTargetCode: Int? = when (targetLevel) {
        "DISTRICT" -> selectedDistrict?.code
        "SUBDIVISION" -> selectedSubdistrict?.code
        "BLOCK" -> selectedBlock?.code
        "VILLAGE" -> selectedVillage?.code
        else -> null
    }

    // Derive display name for summary
    val resolvedTargetName: String = when (targetLevel) {
        "DISTRICT" -> selectedDistrict?.name ?: ""
        "SUBDIVISION" -> selectedSubdistrict?.name ?: ""
        "BLOCK" -> selectedBlock?.name ?: ""
        "VILLAGE" -> selectedVillage?.name ?: ""
        else -> ""
    }

    // ── Load Districts on Mount ─────────────────────────
    LaunchedEffect(Unit) {
        isVisible = true
        scope.launch {
            when (val result = ApiRepository.getDistricts()) {
                is NetworkResult.Success -> {
                    districts = result.data.districts
                }
                else -> { /* Silently handle — officer can still type manually */ }
            }
        }
    }

    // ── Load Subdistricts when District changes ─────────
    LaunchedEffect(selectedDistrict) {
        selectedDistrict?.let { district ->
            lgdLoading = true
            subdistricts = emptyList()
            blocks = emptyList()
            villages = emptyList()
            selectedSubdistrict = null
            selectedBlock = null
            selectedVillage = null
            when (val result = ApiRepository.getSubdistricts(district.code)) {
                is NetworkResult.Success -> {
                    subdistricts = result.data.subdistricts
                }
                else -> {}
            }
            lgdLoading = false
        }
    }

    // ── Load Villages when Subdistrict changes ──────────
    LaunchedEffect(selectedSubdistrict) {
        selectedSubdistrict?.let { sub ->
            lgdLoading = true
            villages = emptyList()
            selectedBlock = null
            selectedVillage = null
            when (val result = ApiRepository.getVillages(sub.code)) {
                is NetworkResult.Success -> {
                    villages = result.data.villages
                }
                else -> {}
            }
            lgdLoading = false
        }
    }

    val isFormValid = title.isNotBlank() &&
            targetLevel.isNotBlank() &&
            resolvedTargetCode != null &&
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
                    text = stringResource(R.string.create_custom_poll),
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

                Text(
                    text = stringResource(R.string.create_poll_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Poll Title ──────────────────────────────
                SectionLabel(stringResource(R.string.poll_title_asterisk))
                SoftTrayInput(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = stringResource(R.string.title_label),
                    placeholder = stringResource(R.string.poll_title_hint),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Description ──────────────────────────────
                SectionLabel(stringResource(R.string.description_optional))
                SoftTrayInput(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.description_label),
                    placeholder = stringResource(R.string.poll_desc_hint),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Target Level Dropdown ───────────────────
                SectionLabel(stringResource(R.string.target_level_asterisk))
                Column {
                    SoftTrayInput(
                        value = targetLevel.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        onValueChange = { },
                        label = stringResource(R.string.select_level),
                        placeholder = stringResource(R.string.level_options),
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
                            .clickable {
                                levelExpanded = true
                            },
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
                                    // Reset downstream selections when level changes
                                    selectedDistrict = null
                                    selectedSubdistrict = null
                                    selectedBlock = null
                                    selectedVillage = null
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Cascading LGD Area Selectors ────────────
                if (targetLevel.isNotBlank()) {
                    SectionLabel(stringResource(R.string.select_target_area))

                    if (lgdLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.loading_area_data),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // ── District Dropdown (always shown when level is selected) ──
                    LgdDropdown(
                        label = stringResource(R.string.district),
                        items = districts,
                        selectedItem = selectedDistrict,
                        expanded = districtExpanded,
                        onExpandedChange = { districtExpanded = it },
                        onItemSelected = { item ->
                            selectedDistrict = item
                            selectedSubdistrict = null
                            selectedBlock = null
                            selectedVillage = null
                            districtExpanded = false
                        },
                    )

                    // ── Subdivision Dropdown (shown for SUBDIVISION, BLOCK, VILLAGE) ──
                    if (targetLevel in listOf("SUBDIVISION", "BLOCK", "VILLAGE") && selectedDistrict != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LgdDropdown(
                            label = stringResource(R.string.subdivision),
                            items = subdistricts,
                            selectedItem = selectedSubdistrict,
                            expanded = subdistrictExpanded,
                            onExpandedChange = { subdistrictExpanded = it },
                            onItemSelected = { item ->
                                selectedSubdistrict = item
                                selectedBlock = null
                                selectedVillage = null
                                subdistrictExpanded = false
                            },
                        )
                    }

                    // ── Village Dropdown (shown for BLOCK, VILLAGE) ──
                    if (targetLevel in listOf("BLOCK", "VILLAGE") && selectedSubdistrict != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LgdDropdown(
                            label = stringResource(R.string.village_area),
                            items = villages,
                            selectedItem = selectedVillage,
                            expanded = villageExpanded,
                            onExpandedChange = { villageExpanded = it },
                            onItemSelected = { item ->
                                selectedVillage = item
                                villageExpanded = false
                            },
                        )
                    }

                    // Show resolved target code
                    resolvedTargetCode?.let { code ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.target_resolved_format, resolvedTargetName, code),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Dynamic Options Builder ────────────────
                SectionLabel(stringResource(R.string.poll_options_header))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.poll_options_desc),
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
                            label = stringResource(R.string.option_hint, index + 1),
                            placeholder = when (index) {
                                0 -> stringResource(R.string.option_eg_1)
                                1 -> stringResource(R.string.option_eg_2)
                                2 -> stringResource(R.string.option_eg_3)
                                3 -> stringResource(R.string.option_eg_4)
                                else -> stringResource(R.string.option_eg_other)
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
                                    contentDescription = stringResource(R.string.remove_option),
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
                            contentDescription = stringResource(R.string.add_option),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_option))
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
                                text = stringResource(R.string.poll_summary),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.poll_summary_target, title, resolvedTargetName.ifEmpty { targetLevel.lowercase() }, targetLevel, resolvedTargetCode ?: "?"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.poll_summary_options, options.filter { it.isNotBlank() }.joinToString(" • ")),
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
                    text = if (isLoading) stringResource(R.string.creating_poll) else stringResource(R.string.create_poll_title),
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
                                        targetCode = resolvedTargetCode ?: 0,
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
                    text = stringResource(R.string.jurisdiction_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Reusable LGD dropdown composable for selecting hierarchy items.
 */
@Composable
private fun LgdDropdown(
    label: String,
    items: List<LgdItem>,
    selectedItem: LgdItem?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemSelected: (LgdItem) -> Unit,
) {
    Column {
        SoftTrayInput(
            value = selectedItem?.let { "${it.name} (${it.code})" } ?: "",
            onValueChange = { },
            label = label,
            placeholder = stringResource(R.string.select_dropdown_item, label),
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
                .clickable(enabled = items.isNotEmpty()) { onExpandedChange(true) },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            if (items.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.no_data_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    },
                    onClick = { onExpandedChange(false) },
                )
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${item.name} (${item.code})",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        onClick = { onItemSelected(item) },
                    )
                }
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
