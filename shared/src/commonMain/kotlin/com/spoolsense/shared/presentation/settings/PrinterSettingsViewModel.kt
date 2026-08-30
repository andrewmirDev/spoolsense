package com.spoolsense.shared.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolsense.shared.domain.model.PrinterSettings
import com.spoolsense.shared.domain.repository.ConnectionTestResult
import com.spoolsense.shared.domain.repository.PrinterRepository
import com.spoolsense.shared.domain.repository.PrinterSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrinterSettingsViewModel(
    private val repository: PrinterSettingsRepository,
    private val printerRepository: PrinterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrinterSettingsState())
    val state: StateFlow<PrinterSettingsState> = _state.asStateFlow()

    init {
        load()
    }

    fun handleIntent(intent: PrinterSettingsIntent) {
        when (intent) {
            is PrinterSettingsIntent.UpdateHost -> _state.update { it.copy(hostText = intent.value) }
            is PrinterSettingsIntent.UpdatePort -> _state.update { it.copy(portText = intent.value) }
            is PrinterSettingsIntent.UpdateApiKey -> _state.update { it.copy(apiKeyText = intent.value) }
            PrinterSettingsIntent.Load -> load()
            PrinterSettingsIntent.TestConnection -> testConnection()
            PrinterSettingsIntent.Save -> save()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val settings = repository.loadSettings()
            _state.update {
                it.copy(
                    settings = settings,
                    hostText = settings.host,
                    portText = settings.port.toString(),
                    apiKeyText = settings.apiKey,
                    isLoading = false,
                    isSaved = false
                )
            }
        }
    }

    private fun currentSettings(): PrinterSettings? {
        val port = _state.value.portText.toIntOrNull()
        if (port == null || port !in 1..65535) {
            _state.update { it.copy(errorMessage = "Invalid port (1-65535)") }
            return null
        }
        if (_state.value.hostText.isBlank()) {
            _state.update { it.copy(errorMessage = "Host is required") }
            return null
        }
        return PrinterSettings(
            host = _state.value.hostText.trim(),
            port = port,
            apiKey = _state.value.apiKeyText.trim()
        )
    }

    private fun testConnection() {
        val settings = currentSettings() ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(isTesting = true, errorMessage = null, connectionTestOk = null, connectionTestMessage = null)
            }
            when (val result = printerRepository.testConnection(settings)) {
                is ConnectionTestResult.Success -> {
                    repository.saveSettings(settings)
                    _state.update {
                        it.copy(
                            isTesting = false,
                            connectionTestOk = true,
                            connectionTestMessage = result.serverInfo,
                            settings = settings
                        )
                    }
                }
                is ConnectionTestResult.Failure -> _state.update {
                    it.copy(isTesting = false, connectionTestOk = false, connectionTestMessage = result.reason)
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val settings = currentSettings()
            if (settings == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.saveSettings(settings)
                _state.update { it.copy(settings = settings, isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to save")
                }
            }
        }
    }
}