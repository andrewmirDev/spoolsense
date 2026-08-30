package com.spoolsense.shared.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PrinterSettingsScreen(viewModel: PrinterSettingsViewModel, onSaved: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Printer Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.hostText,
                onValueChange = { viewModel.handleIntent(PrinterSettingsIntent.UpdateHost(it)) },
                label = { Text("Moonraker host / IP") },
                placeholder = { Text("192.168.1.100") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.portText,
                onValueChange = { viewModel.handleIntent(PrinterSettingsIntent.UpdatePort(it)) },
                label = { Text("Port") },
                placeholder = { Text("7125") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.apiKeyText,
                onValueChange = { viewModel.handleIntent(PrinterSettingsIntent.UpdateApiKey(it)) },
                label = { Text("API key (optional)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.connectionTestMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.connectionTestMessage.orEmpty(),
                    color = when (state.connectionTestOk) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { viewModel.handleIntent(PrinterSettingsIntent.TestConnection) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isTesting
            ) {
                Text(if (state.isTesting) "Testing..." else "Test connection")
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.handleIntent(PrinterSettingsIntent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && !state.isTesting
            ) {
                Text(if (state.isLoading) "Saving..." else "Save")
            }
        }
    }
}
