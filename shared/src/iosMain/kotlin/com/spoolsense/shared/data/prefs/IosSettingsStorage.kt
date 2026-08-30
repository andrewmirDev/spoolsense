package com.spoolsense.shared.data.prefs

import platform.Foundation.NSUserDefaults

class IosSettingsStorage : SettingsStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String, defaultValue: String): String =
        defaults.stringForKey(key) ?: defaultValue

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getInt(key: String, defaultValue: Int): Int =
        defaults.integerForKey(key).let { if (it == 0L) defaultValue else it.toInt() }

    override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}
