package com.spoolsense.shared.data.repository

import com.spoolsense.shared.data.prefs.SettingsStorage
import com.spoolsense.shared.domain.model.PrinterSettings
import com.spoolsense.shared.domain.repository.PrinterSettingsRepository

class PrinterSettingsRepositoryImpl(
    private val storage: SettingsStorage
) : PrinterSettingsRepository {

    override suspend fun loadSettings(): PrinterSettings {
        return PrinterSettings(
            host = storage.getString(KEY_HOST, ""),
            port = storage.getInt(KEY_PORT, 7125),
            apiKey = storage.getString(KEY_API_KEY, "")
        )
    }

    override suspend fun saveSettings(settings: PrinterSettings) {
        storage.putString(KEY_HOST, settings.host)
        storage.putInt(KEY_PORT, settings.port)
        storage.putString(KEY_API_KEY, settings.apiKey)
    }

    private companion object {
        const val KEY_HOST = "printer_host"
        const val KEY_PORT = "printer_port"
        const val KEY_API_KEY = "printer_api_key"
    }
}
