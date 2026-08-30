package com.spoolsense.shared.presentation.settings

sealed interface PrinterSettingsIntent {
    data class UpdateHost(val value: String) : PrinterSettingsIntent
    data class UpdatePort(val value: String) : PrinterSettingsIntent
    data class UpdateApiKey(val value: String) : PrinterSettingsIntent
    object Load : PrinterSettingsIntent
    object TestConnection : PrinterSettingsIntent
    object Save : PrinterSettingsIntent
}
