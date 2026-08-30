package com.spoolsense.shared.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE

class AndroidSettingsStorage(context: Context) : SettingsStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spoolsense_prefs", MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
