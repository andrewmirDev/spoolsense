package com.spoolsense.shared.data.prefs

import java.io.File
import java.util.Properties

class JvmSettingsStorage : SettingsStorage {

    private val properties = Properties()
    private val file = File(System.getProperty("user.home"), ".spoolsense.properties")

    init {
        if (file.exists()) {
            file.inputStream().use { properties.load(it) }
        }
    }

    override fun getString(key: String, defaultValue: String): String =
        properties.getProperty(key, defaultValue)

    override fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        persist()
    }

    override fun getInt(key: String, defaultValue: Int): Int =
        properties.getProperty(key)?.toIntOrNull() ?: defaultValue

    override fun putInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        persist()
    }

    private fun persist() {
        file.outputStream().use { properties.store(it, "SpoolSense settings") }
    }
}
