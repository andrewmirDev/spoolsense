package com.spoolsense.shared.presentation.settings

import com.spoolsense.shared.domain.model.PrinterSettings

data class PrinterSettingsState(
    val settings: PrinterSettings = PrinterSettings(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val hostText: String = "",
    val portText: String = "",
    val apiKeyText: String = "",
    val isTesting: Boolean = false,
    val connectionTestOk: Boolean? = null,
    val connectionTestMessage: String? = null
)