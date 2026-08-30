package com.spoolsense.shared.domain.repository

import com.spoolsense.shared.domain.model.PrinterSettings

interface PrinterSettingsRepository {
    suspend fun loadSettings(): PrinterSettings
    suspend fun saveSettings(settings: PrinterSettings)
}
