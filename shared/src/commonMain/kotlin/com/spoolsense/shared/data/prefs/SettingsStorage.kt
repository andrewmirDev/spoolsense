package com.spoolsense.shared.data.prefs

interface SettingsStorage {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
}
