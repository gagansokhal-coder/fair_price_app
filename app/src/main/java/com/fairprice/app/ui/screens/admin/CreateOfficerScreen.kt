package com.fairprice.app.ui.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fairprice.app.network.CreateOfficerRequest
import com.fairprice.app.network.LgdItem
import com.fairprice.app.network.RetrofitClient
import com.fairprice.app.ui.components.GradientButton
import com.fairprice.app.ui.components.SoftTrayInput
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
    val roles = listOf("ADMIN_DISTRICT" to "District Admin", "ADMIN_SUBDIVISION" to "Subdivision Admin", "ADMIN_BLOCK" to "Block Admin")
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
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getDistricts()
            }
            if (response.isSuccessful) {
                districts = response.body()?.districts ?: emptyList()
            }
        } catch (_: Exception) {
            errorMessage = "Failed to load districts"
        } finally {
            isLoading = false
        }
    }

    // Fetch subdistricts when district changes
    LaunchedEffect(selectedDistrict) {
        if (selectedDistrict != null) {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getSubdistricts(selectedDistrict!!.code)
                }
                if (response.isSuccessful) {
                    subdistricts = response.body()?.subdistricts ?: emptyList()
                    selectedSubdistrict = null // Reset
                }
            } catch (_: Exception) {
                errorMessage = "Failed to load subdistricts/blocks"
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
                    text = "Appoint Officer",
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
                text = "Officer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                placeholder = "Enter officer's full name",
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = phoneNo,
                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNo = it },
                label = "Phone Number",
                placeholder = "10-digit mobile number",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = email,
                onValueChange = { email = it },
                label = "Email Address (Optional)",
                placeholder = "officer@example.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(16.dp))

            SoftTrayInput(
                value = designation,
                onValueChange = { designation = it },
                label = "Designation / Title",
                placeholder = "e.g., Block Development Officer",
                imeAction = ImeAction.Done,
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Role & Jurisdiction",
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
                    label = "Select Role Level",
                    modifier = Modifier.menuAnchor(),
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
                            label = "Select District",
                            modifier = Modifier.menuAnchor(),
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
                            label = "Select Block / Subdivision",
                            modifier = Modifier.menuAnchor(),
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
                text = if (isSubmitting) "Creating..." else "Create Officer",
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
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.apiService.createOfficer(req)
                            }
                            if (response.isSuccessful && response.body()?.success == true) {
                                onOfficerCreated()
                            } else {
                                errorMessage = response.body()?.message ?: "Access Denied: You may not have jurisdiction."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Network error: ${e.message}"
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
