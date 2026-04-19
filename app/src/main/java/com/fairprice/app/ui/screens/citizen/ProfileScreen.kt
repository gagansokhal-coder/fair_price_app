package com.fairprice.app.ui.screens.citizen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import com.fairprice.app.ui.components.FairPriceCard
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.InfoSlab
import com.fairprice.app.ui.components.OutlinedActionButton
import com.fairprice.app.ui.components.SoftTrayInput
import com.fairprice.app.ui.theme.ShapeTokens
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.DeviceUtils
import com.fairprice.app.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Profile Screen — Role-aware user info and logout.
 *
 * Dynamically displays:
 *   - **Officer**: Name, Role, Designation, District
 *   - **Citizen**: Name, Ration Card, Phone, Address, LGD Hierarchy
 *
 * Includes "Edit Profile" mode for citizens with GPS re-verification.
 * Logout requires confirmation and clears the session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val session = remember { SessionManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // Edit mode state (citizen only)
    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(session.getCitizenName()) }
    var editAddress by remember { mutableStateOf(session.getCitizenAddress()) }
    var isSaving by remember { mutableStateOf(false) }
    var gpsStatus by remember { mutableStateOf("") }
    var gpsVerified by remember { mutableStateOf(false) }

    val isOfficer = session.isOfficer()

    LaunchedEffect(Unit) { isVisible = true }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? You will need to verify your identity again to access the app.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Logout",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
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
            title = {
                Text(
                    text = "Profile",
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
            actions = {
                // Edit button for citizens only
                if (!isOfficer && !isEditMode) {
                    IconButton(onClick = {
                        isEditMode = true
                        editName = session.getCitizenName()
                        editAddress = session.getCitizenAddress()
                        gpsVerified = false
                        gpsStatus = ""
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
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

                if (isOfficer) {
                    // ═══ OFFICER PROFILE ═══════════════════════════
                    OfficerProfileContent(session)
                } else if (isEditMode) {
                    // ═══ CITIZEN EDIT MODE ═════════════════════════
                    CitizenEditContent(
                        editName = editName,
                        onNameChange = { editName = it },
                        editAddress = editAddress,
                        onAddressChange = { editAddress = it },
                        gpsStatus = gpsStatus,
                        gpsVerified = gpsVerified,
                        isSaving = isSaving,
                        onVerifyGps = {
                            scope.launch {
                                gpsStatus = "Acquiring GPS…"
                                try {
                                    val location = DeviceUtils.getCurrentLocation(context)
                                    if (location != null) {
                                        if (DeviceUtils.isMockLocation(location)) {
                                            gpsStatus = "⚠️ Mock location detected!"
                                            gpsVerified = false
                                        } else {
                                            gpsStatus = "✅ GPS Verified: ${
                                                String.format("%.4f", location.latitude)
                                            }, ${String.format("%.4f", location.longitude)}"
                                            gpsVerified = true
                                        }
                                    } else {
                                        gpsStatus = "⚠️ GPS unavailable"
                                        gpsVerified = false
                                    }
                                } catch (_: Exception) {
                                    gpsStatus = "⚠️ GPS permission required"
                                    gpsVerified = false
                                }
                            }
                        },
                        onSave = {
                            scope.launch {
                                isSaving = true
                                session.updateCitizenEditableFields(
                                    name = editName,
                                    address = editAddress,
                                )
                                isSaving = false
                                isEditMode = false
                            }
                        },
                        onCancel = { isEditMode = false },
                    )
                } else {
                    // ═══ CITIZEN VIEW MODE ═════════════════════════
                    CitizenProfileContent(session)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Info
                FairPriceCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        text = "Ration Prahari v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "PDS Monitoring • Government of India",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                OutlinedActionButton(
                    text = "Logout",
                    onClick = { showLogoutDialog = true },
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ─── Officer Profile View ────────────────────────────────────────
@Composable
private fun OfficerProfileContent(session: SessionManager) {
    Text(
        text = "Officer Information",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    InfoSlab(
        icon = Icons.Rounded.Person,
        title = "Full Name",
        value = session.getOfficerName().ifEmpty { "Officer" },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Shield,
        title = "Role",
        value = session.getOfficerRole().ifEmpty { "—" },
        subtitle = session.getOfficerDesignation().ifEmpty { null },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Work,
        title = "Designation",
        value = session.getOfficerDesignation().ifEmpty { "—" },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.LocationOn,
        title = "District",
        value = session.getOfficerDistrictName().ifEmpty { "—" },
    )
}

// ─── Citizen Profile View ────────────────────────────────────────
@Composable
private fun CitizenProfileContent(session: SessionManager) {
    // Personal Information
    Text(
        text = "Personal Information",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    InfoSlab(
        icon = Icons.Rounded.Person,
        title = "Full Name",
        value = session.getCitizenName().ifEmpty { "Citizen" },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Badge,
        title = "Ration Card Number",
        value = session.getCitizenRationCard().ifEmpty { "—" },
        subtitle = "NFSA Beneficiary",
    )

    Spacer(modifier = Modifier.height(12.dp))

    val phone = session.getCitizenPhone()
    InfoSlab(
        icon = Icons.Rounded.Phone,
        title = "Registered Phone",
        value = if (phone.length >= 10) "+91 ${phone.substring(0, 5)} ${phone.substring(5)}"
        else phone.ifEmpty { "—" },
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Address & Location
    Text(
        text = "Address & Location",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    val address = session.getCitizenAddress()
    if (address.isNotEmpty()) {
        InfoSlab(
            icon = Icons.Rounded.Home,
            title = "Address",
            value = address,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    val district = session.getCitizenDistrict()
    val subdistrict = session.getCitizenSubdistrict()
    val village = session.getCitizenVillage()

    if (district.isNotEmpty() || village.isNotEmpty()) {
        InfoSlab(
            icon = Icons.Rounded.LocationOn,
            title = "Location",
            value = village.ifEmpty { "—" },
            subtitle = buildString {
                if (subdistrict.isNotEmpty()) append("Sub-District: $subdistrict")
                if (district.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append("District: $district")
                }
            }.ifEmpty { null },
        )
    }
}

// ─── Citizen Edit Mode ───────────────────────────────────────────
@Composable
private fun CitizenEditContent(
    editName: String,
    onNameChange: (String) -> Unit,
    editAddress: String,
    onAddressChange: (String) -> Unit,
    gpsStatus: String,
    gpsVerified: Boolean,
    isSaving: Boolean,
    onVerifyGps: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(
        text = "Edit Profile",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "GPS verification is required when updating your address.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Name Input
    SoftTrayInput(
        value = editName,
        onValueChange = onNameChange,
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

    // Address Input
    SoftTrayInput(
        value = editAddress,
        onValueChange = onAddressChange,
        label = "Address",
        placeholder = "Enter your address",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
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

    Spacer(modifier = Modifier.height(24.dp))

    // GPS Verification Section
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
            tint = if (gpsVerified) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (gpsStatus.isEmpty()) "GPS verification required" else gpsStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onVerifyGps) {
            Text(
                text = if (gpsVerified) "Re-verify" else "Verify GPS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Save Button
    GradientButton(
        text = if (isSaving) "Saving…" else "Save Changes",
        onClick = onSave,
        enabled = editName.isNotBlank() && gpsVerified && !isSaving,
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Cancel Button
    OutlinedActionButton(
        text = "Cancel",
        onClick = onCancel,
    )
}
