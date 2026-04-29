package com.gagan.lokdiksha.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.R
import com.gagan.lokdiksha.network.CreateOfficerRequest
import com.gagan.lokdiksha.network.LgdItem
import com.gagan.lokdiksha.network.ApiRepository
import com.gagan.lokdiksha.network.NetworkResult
import com.gagan.lokdiksha.ui.components.GradientButton
import com.gagan.lokdiksha.ui.components.SoftTrayInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfficerScreen(
    onOfficerCreated: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }

    // Role Selection
    val roles = listOf(
        "ADMIN_DISTRICT" to stringResource(R.string.district_admin_role),
        "ADMIN_SUBDIVISION" to stringResource(R.string.subdivision_admin_role),
        "ADMIN_BLOCK" to stringResource(R.string.block_admin_role)
    )
    var expandedRole by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<Pair<String, String>?>(null) }

    // LGD Data
    var districts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }
    var subdistricts by remember { mutableStateOf<List<LgdItem>>(emptyList()) }

    var expandedDistrict by remember { mutableStateOf(false) }
    var selectedDistrict by remember { mutableStateOf<LgdItem?>(null) }

    var expandedSubdistrict by remember { mutableStateOf(false) }
    var selectedSubdistrict by remember { mutableStateOf<LgdItem?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch initial districts
    LaunchedEffect(Unit) {
        isLoading = true
        when (val result = ApiRepository.getDistricts()) {
            is NetworkResult.Success -> districts = result.data.districts
            is NetworkResult.Error -> errorMessage = result.message
            else -> {}
        }
        isLoading = false
    }

    // Fetch subdistricts when district changes
    LaunchedEffect(selectedDistrict) {
        if (selectedDistrict != null) {
            when (val result = ApiRepository.getSubdistricts(selectedDistrict!!.code)) {
                is NetworkResult.Success -> {
                    subdistricts = result.data.subdistricts
                    selectedSubdistrict = null // Reset
                }
                is NetworkResult.Error -> errorMessage = result.message
                else -> {}
            }
        } else {
            subdistricts = emptyList()
            selectedSubdistrict = null
        }
    }

    val isFormValid = name.isNotBlank() &&
            phoneNo.length == 10 &&
            selectedRole != null &&
            (selectedRole?.first == "ADMIN_DISTRICT" && selectedDistrict != null ||
            (selectedRole?.first == "ADMIN_SUBDIVISION" || selectedRole?.first == "ADMIN_BLOCK") && selectedDistrict != null && selectedSubdistrict != null)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.appoint_officer),
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
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.officer_details),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.full_name),
                placeholder = stringResource(R.string.enter_officer_full_name),
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = phoneNo,
                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNo = it },
                label = stringResource(R.string.phone_number),
                placeholder = stringResource(R.string.enter_phone_hint),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email_address_optional),
                placeholder = stringResource(R.string.email_hint),
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = designation,
                onValueChange = { designation = it },
                label = stringResource(R.string.designation_title),
                placeholder = stringResource(R.string.designation_example),
                imeAction = ImeAction.Done,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.role_and_jurisdiction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Role Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                SoftTrayInput(
                    value = selectedRole?.second ?: "",
                    onValueChange = {},
                    label = stringResource(R.string.select_role_level),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                )
                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.second) },
                            onClick = {
                                selectedRole = role
                                expandedRole = false
                                // Reset LGD if switching roles requires it
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // District Dropdown
            AnimatedVisibility(visible = selectedRole != null) {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expandedDistrict,
                        onExpandedChange = { expandedDistrict = !expandedDistrict }
                    ) {
                        SoftTrayInput(
                            value = selectedDistrict?.name ?: "",
                            onValueChange = {},
                            label = stringResource(R.string.select_district),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict) },
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDistrict,
                            onDismissRequest = { expandedDistrict = false }
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist.name) },
                                    onClick = {
                                        selectedDistrict = dist
                                        expandedDistrict = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Subdistrict/Block Dropdown
            AnimatedVisibility(visible = selectedRole?.first == "ADMIN_SUBDIVISION" || selectedRole?.first == "ADMIN_BLOCK") {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubdistrict,
                        onExpandedChange = { expandedSubdistrict = !expandedSubdistrict }
                    ) {
                        SoftTrayInput(
                            value = selectedSubdistrict?.name ?: "",
                            onValueChange = {},
                            label = stringResource(R.string.select_block_subdivision),
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubdistrict) },
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubdistrict,
                            onDismissRequest = { expandedSubdistrict = false }
                        ) {
                            subdistricts.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.name) },
                                    onClick = {
                                        selectedSubdistrict = sub
                                        expandedSubdistrict = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (isSubmitting) stringResource(R.string.creating_officer) else stringResource(R.string.create_officer_title),
                enabled = isFormValid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val req = CreateOfficerRequest(
                                name = name,
                                phoneNo = phoneNo,
                                email = email.takeIf { it.isNotBlank() },
                                role = selectedRole!!.first,
                                districtCode = selectedDistrict?.code,
                                subdistrictCode = selectedSubdistrict?.code,
                                blockCode = selectedSubdistrict?.code, // Block and subdistrict codes are mapped 1:1 in this hierarchy view
                                designation = designation.takeIf { it.isNotBlank() }
                            )
                            when (val result = ApiRepository.createOfficer(req)) {
                                is NetworkResult.Success -> {
                                    if (result.data.success) onOfficerCreated()
                                    else errorMessage = result.data.message
                                }
                                is NetworkResult.Error -> errorMessage = result.message
                                else -> {}
                            }
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
