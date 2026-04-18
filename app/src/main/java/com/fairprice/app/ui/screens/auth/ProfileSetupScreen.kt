package com.fairprice.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.network.LgdItem
import com.fairprice.app.network.RegisterProfileRequest
import com.fairprice.app.network.ApiRepository
import com.fairprice.app.network.NetworkResult
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.DeviceUtils
import kotlinx.coroutines.launch

/**
 * Profile Setup Screen — Mandatory after citizen login.
 *
 * Captures:
 * - Full Name
 * - Address
 * - LGD Hierarchy: District → Sub-District → Village (from Punjab CSV data)
 * - GPS location (auto-captured)
 * - Device UUID (auto-captured)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    userId: String,
    onProfileComplete: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form state
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // GPS state
    var gpsLat by remember { mutableDoubleStateOf(0.0) }
    var gpsLng by remember { mutableDoubleStateOf(0.0) }
    var gpsStatus by remember { mutableStateOf("Acquiring GPS…") }

    // Device UUID
    val hardwareUuid = remember { DeviceUtils.getHardwareUuid(context) }

    // LGD Hierarchy state
    var districts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var subdistricts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var villages by remember { mutableStateOf<List<LgdItem>>(emptyList()) }

    var selectedDistrict by remember { mutableStateOf<LgdItem?>(null) }
    var selectedSubdistrict by remember { mutableStateOf<LgdItem?>(null) }
    var selectedVillage by remember { mutableStateOf<LgdItem?>(null) }

    var districtExpanded by remember { mutableStateOf(false) }
    var subdistrictExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    var isLoadingDistricts by remember { mutableStateOf(true) }
    var isLoadingSubdistricts by remember { mutableStateOf(false) }
    var isLoadingVillages by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    // Fetch GPS on launch
    LaunchedEffect(Unit) {
        try {
            val location = DeviceUtils.getCurrentLocation(context)
            if (location != null) {
                if (DeviceUtils.isMockLocation(location)) {
                    gpsStatus = "⚠️ Mock location detected!"
                } else {
                    gpsLat = location.latitude
                    gpsLng = location.longitude
                    gpsStatus = "✅ GPS: ${String.format("%.4f", gpsLat)}, ${String.format("%.4f", gpsLng)}"
                }
            } else {
                gpsStatus = "⚠️ GPS unavailable"
            }
        } catch (e: Exception) {
            gpsStatus = "⚠️ GPS permission required"
        }
    }

    // Fetch districts on launch
    LaunchedEffect(Unit) {
        when (val result = ApiRepository.getDistricts()) {
            is NetworkResult.Success -> districts = result.data.districts
            is NetworkResult.Error -> errorMessage = result.message
            else -> {}
        }
        isLoadingDistricts = false
    }

    // Fetch sub-districts when district changes
    LaunchedEffect(selectedDistrict) {
        selectedDistrict?.let { district ->
            isLoadingSubdistricts = true
            selectedSubdistrict = null
            selectedVillage = null
            villages = emptyList()
            when (val result = ApiRepository.getSubdistricts(district.code)) {
                is NetworkResult.Success -> subdistricts = result.data.subdistricts
                is NetworkResult.Error -> errorMessage = result.message
                else -> {}
            }
            isLoadingSubdistricts = false
        }
    }

    // Fetch villages when sub-district changes
    LaunchedEffect(selectedSubdistrict) {
        selectedSubdistrict?.let { subdistrict ->
            isLoadingVillages = true
            selectedVillage = null
            when (val result = ApiRepository.getVillages(subdistrict.code)) {
                is NetworkResult.Success -> villages = result.data.villages
                is NetworkResult.Error -> errorMessage = result.message
                else -> {}
            }
            isLoadingVillages = false
        }
    }

    val isFormValid = fullName.isNotBlank() && address.isNotBlank() &&
            selectedDistrict != null && selectedSubdistrict != null &&
            selectedVillage != null && gpsLat != 0.0

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
                    "Complete Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
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
                Text(
                    text = "Please fill in your details to complete registration. This is mandatory for PDS verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ─── GPS & Device Status ────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeTokens.Card)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GpsFixed,
                        contentDescription = "GPS",
                        tint = if (gpsLat != 0.0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = gpsStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeTokens.Card)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = "Device",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Device: ${hardwareUuid.take(8)}…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Bound",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Full Name ──────────────────────────────────
                SoftTrayInput(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Name",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Address ────────────────────────────────────
                SoftTrayInput(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    placeholder = "Enter your full address",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Address",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ─── LGD Hierarchy Section ──────────────────────
                Text(
                    text = "Location Hierarchy (Punjab)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Select your District → Sub-District → Village",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // District Dropdown
                LgdDropdown(
                    label = "District",
                    items = districts,
                    selectedItem = selectedDistrict,
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = it },
                    onItemSelected = {
                        selectedDistrict = it
                        districtExpanded = false
                    },
                    isLoading = isLoadingDistricts,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-District Dropdown
                LgdDropdown(
                    label = "Sub-District (Tehsil/Block)",
                    items = subdistricts,
                    selectedItem = selectedSubdistrict,
                    expanded = subdistrictExpanded,
                    onExpandedChange = {
                        if (selectedDistrict != null) subdistrictExpanded = it
                    },
                    onItemSelected = {
                        selectedSubdistrict = it
                        subdistrictExpanded = false
                    },
                    isLoading = isLoadingSubdistricts,
                    enabled = selectedDistrict != null,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Village Dropdown
                LgdDropdown(
                    label = "Village",
                    items = villages,
                    selectedItem = selectedVillage,
                    expanded = villageExpanded,
                    onExpandedChange = {
                        if (selectedSubdistrict != null) villageExpanded = it
                    },
                    onItemSelected = {
                        selectedVillage = it
                        villageExpanded = false
                    },
                    isLoading = isLoadingVillages,
                    enabled = selectedSubdistrict != null,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                // Submit Button
                GradientButton(
                    text = if (isSubmitting) "Registering…" else "Complete Registration",
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            errorMessage = null
                            val result = ApiRepository.registerProfile(
                                RegisterProfileRequest(
                                    userId = userId,
                                    fullName = fullName,
                                    address = address,
                                    districtCode = selectedDistrict!!.code,
                                    subdistrictCode = selectedSubdistrict!!.code,
                                    villageCode = selectedVillage!!.code,
                                    hardwareUuid = hardwareUuid,
                                    gpsLat = gpsLat,
                                    gpsLng = gpsLng,
                                )
                            )
                            when (result) {
                                is NetworkResult.Success -> {
                                    if (result.data.success) {
                                        // Save citizen name to session for dashboard greeting
                                        val session = com.fairprice.app.utils.SessionManager.getInstance(context)
                                        session.saveCitizenProfile(fullName)
                                        onProfileComplete()
                                    }
                                    else errorMessage = result.data.message
                                }
                                is NetworkResult.Error -> errorMessage = result.message
                                else -> {}
                            }
                            isSubmitting = false
                        }
                    },
                    enabled = isFormValid && !isSubmitting,
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Reusable LGD hierarchy dropdown composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LgdDropdown(
    label: String,
    items: List<LgdItem>,
    selectedItem: LgdItem?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemSelected: (LgdItem) -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) onExpandedChange(it) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .clip(ShapeTokens.InputField)
                .background(
                    if (enabled) MaterialTheme.colorScheme.surfaceContainerHighest
                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
                )
                .clickable(enabled = enabled) { onExpandedChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 0.7f else 0.4f
                    ),
                )
                Text(
                    text = selectedItem?.name ?: "Select $label",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedItem != null && enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.4f
                    ),
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = { onItemSelected(item) },
                )
            }
        }
    }
}
