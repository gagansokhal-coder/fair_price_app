package com.gagan.lokdiksha.ui.screens.citizen

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.R
import com.gagan.lokdiksha.ui.components.FairPriceCard
import com.gagan.lokdiksha.ui.components.GradientButton
import com.gagan.lokdiksha.ui.components.InfoSlab
import com.gagan.lokdiksha.ui.components.OutlinedActionButton
import com.gagan.lokdiksha.ui.components.SoftTrayInput
import com.gagan.lokdiksha.ui.theme.ShapeTokens
import com.gagan.lokdiksha.ui.theme.SteadyPulseEasing
import com.gagan.lokdiksha.utils.DeviceUtils
import com.gagan.lokdiksha.utils.SessionManager
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
                    text = stringResource(R.string.logout_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirm_message),
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
                        text = stringResource(R.string.logout),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
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
                    text = stringResource(R.string.profile),
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
                            contentDescription = stringResource(R.string.edit_profile),
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
                                gpsStatus = context.getString(R.string.gps_acquiring)
                                try {
                                    val location = DeviceUtils.getCurrentLocation(context)
                                    if (location != null) {
                                        if (DeviceUtils.isMockLocation(location)) {
                                            gpsStatus = context.getString(R.string.gps_mock_detected)
                                            gpsVerified = false
                                        } else {
                                            gpsStatus = context.getString(
                                                R.string.gps_verified,
                                                String.format("%.4f", location.latitude),
                                                String.format("%.4f", location.longitude)
                                            )
                                            gpsVerified = true
                                        }
                                    } else {
                                        gpsStatus = context.getString(R.string.gps_unavailable)
                                        gpsVerified = false
                                    }
                                } catch (_: Exception) {
                                    gpsStatus = context.getString(R.string.gps_permission_required)
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

                // ─── Language Selector ────────────────────────────
                Text(
                    text = stringResource(R.string.language_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.language_settings_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val currentLang = com.gagan.lokdiksha.utils.LocaleManager.getLanguage(context)
                val languages = com.gagan.lokdiksha.utils.LocaleManager.getSupportedLanguages()

                languages.forEach { (code, displayName) ->
                    val isSelected = code == currentLang
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(ShapeTokens.Card)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            .then(
                                if (!isSelected) Modifier.then(
                                    Modifier.clip(ShapeTokens.Card)
                                ) else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = displayName,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = stringResource(R.string.selected),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            TextButton(onClick = {
                                com.gagan.lokdiksha.utils.LocaleManager.setLocale(context, code)
                                (context as? android.app.Activity)?.recreate()
                            }) {
                                Text(
                                    text = stringResource(R.string.select),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Info
                FairPriceCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        text = stringResource(R.string.app_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Text(
                        text = stringResource(R.string.pds_monitoring_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                OutlinedActionButton(
                    text = stringResource(R.string.logout),
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
        text = stringResource(R.string.officer_information),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    InfoSlab(
        icon = Icons.Rounded.Person,
        title = stringResource(R.string.full_name),
        value = session.getOfficerName().ifEmpty { stringResource(R.string.officer) },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Shield,
        title = stringResource(R.string.role_label),
        value = session.getOfficerRole().ifEmpty { "—" },
        subtitle = session.getOfficerDesignation().ifEmpty { null },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Work,
        title = stringResource(R.string.designation_label),
        value = session.getOfficerDesignation().ifEmpty { "—" },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.LocationOn,
        title = stringResource(R.string.district_label),
        value = session.getOfficerDistrictName().ifEmpty { "—" },
    )
}

// ─── Citizen Profile View ────────────────────────────────────────
@Composable
private fun CitizenProfileContent(session: SessionManager) {
    // Personal Information
    Text(
        text = stringResource(R.string.personal_information),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    InfoSlab(
        icon = Icons.Rounded.Person,
        title = stringResource(R.string.full_name),
        value = session.getCitizenName().ifEmpty { stringResource(R.string.citizen) },
    )

    Spacer(modifier = Modifier.height(12.dp))

    InfoSlab(
        icon = Icons.Rounded.Badge,
        title = stringResource(R.string.ration_card_no),
        value = session.getCitizenRationCard().ifEmpty { "—" },
        subtitle = stringResource(R.string.nfsa_beneficiary),
    )

    Spacer(modifier = Modifier.height(12.dp))

    val phone = session.getCitizenPhone()
    InfoSlab(
        icon = Icons.Rounded.Phone,
        title = stringResource(R.string.registered_phone),
        value = if (phone.length >= 10) "+91 ${phone.substring(0, 5)} ${phone.substring(5)}"
        else phone.ifEmpty { "—" },
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Address & Location
    Text(
        text = stringResource(R.string.address_location),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(16.dp))

    val address = session.getCitizenAddress()
    if (address.isNotEmpty()) {
        InfoSlab(
            icon = Icons.Rounded.Home,
            title = stringResource(R.string.address),
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
            title = stringResource(R.string.location_label),
            value = village.ifEmpty { "—" },
            subtitle = buildString {
                if (subdistrict.isNotEmpty()) append("${stringResource(R.string.sub_district_prefix, subdistrict)}")
                if (district.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append(stringResource(R.string.district_prefix, district))
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
        text = stringResource(R.string.edit_profile),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.gps_verification_required_msg),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Name Input
    SoftTrayInput(
        value = editName,
        onValueChange = onNameChange,
        label = stringResource(R.string.full_name),
        placeholder = stringResource(R.string.enter_full_name),
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = stringResource(R.string.name_icon),
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
        label = stringResource(R.string.address),
        placeholder = stringResource(R.string.enter_address),
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Home,
                contentDescription = stringResource(R.string.address_icon),
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
            contentDescription = stringResource(R.string.gps_icon),
            tint = if (gpsVerified) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (gpsStatus.isEmpty()) stringResource(R.string.gps_required) else gpsStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onVerifyGps) {
            Text(
                text = if (gpsVerified) stringResource(R.string.reverify_gps) else stringResource(R.string.verify_gps),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Save Button
    GradientButton(
        text = if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_changes),
        onClick = onSave,
        enabled = editName.isNotBlank() && gpsVerified && !isSaving,
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Cancel Button
    OutlinedActionButton(
        text = stringResource(R.string.cancel),
        onClick = onCancel,
    )
}
