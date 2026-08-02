package com.spoolsense.shared.presentation.insert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import spoolsense.shared.generated.resources.Res
import spoolsense.shared.generated.resources.add

@Composable
fun InsertSpoolScreen(viewModel: InsertSpoolViewModel, onSuccess: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Spool") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD4EDDA)
                    )
                ) {
                    Text(
                        state.successMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF155724)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8D7DA)
                    )
                ) {
                    Text(
                        state.errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF721C24)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                "Spool Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateName(it)) },
                label = { Text("Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                isError = false
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.vendor,
                onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateVendor(it)) },
                label = { Text("Vendor *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.formErrors.containsKey("vendor"),
                supportingText = {
                    if (state.formErrors.containsKey("vendor")) {
                        Text(state.formErrors["vendor"] ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.material,
                onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateMaterial(it)) },
                label = { Text("Material *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.formErrors.containsKey("material"),
                supportingText = {
                    if (state.formErrors.containsKey("material")) {
                        Text(state.formErrors["material"] ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.colorName,
                onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateColorName(it)) },
                label = { Text("Color Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.formErrors.containsKey("colorName"),
                supportingText = {
                    if (state.formErrors.containsKey("colorName")) {
                        Text(state.formErrors["colorName"] ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.colorHex,
                    onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateColorHex(it)) },
                    label = { Text("Hex Color *") },
                    modifier = Modifier.weight(1f),
                    isError = state.formErrors.containsKey("colorHex"),
                    supportingText = {
                        if (state.formErrors.containsKey("colorHex")) {
                            Text(state.formErrors["colorHex"] ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = try {
                                Color(state.colorHex.toLong(16) or 0xFF000000)
                            } catch (e: Exception) {
                                Color.Gray
                            },
                            shape = CircleShape
                        )
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.totalWeightGrams,
                onValueChange = { viewModel.handleIntent(InsertSpoolIntent.UpdateTotalWeight(it)) },
                label = { Text("Total Weight (grams) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.formErrors.containsKey("totalWeightGrams"),
                supportingText = {
                    if (state.formErrors.containsKey("totalWeightGrams")) {
                        Text(state.formErrors["totalWeightGrams"] ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.handleIntent(InsertSpoolIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isLoading) "Saving..." else "Add Spool")
            }

            TextButton(
                onClick = { viewModel.handleIntent(InsertSpoolIntent.ClearForm) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}
