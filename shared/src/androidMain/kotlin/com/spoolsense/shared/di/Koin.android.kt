package com.spoolsense.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import android.content.Context
import android.content.SharedPreferences
import com.spoolsense.shared.data.prefs.AndroidSettingsStorage
import com.spoolsense.shared.data.prefs.SettingsStorage
import com.spoolsense.shared.database.SpoolDatabase
import org.koin.dsl.module

private const val DB_VERSION_KEY = "spoolsense_db_version"
private const val CURRENT_DB_VERSION = 2

actual val platformModule = module {
    single<SqlDriver> {
        val context: Context = get()
        val dbName = "spoolsense.db"
        val prefs: SharedPreferences = context.getSharedPreferences("spoolsense_prefs", Context.MODE_PRIVATE)
        
        val savedVersion = prefs.getInt(DB_VERSION_KEY, 0)
        
        if (savedVersion < CURRENT_DB_VERSION) {
            context.deleteDatabase(dbName)
            prefs.edit().putInt(DB_VERSION_KEY, CURRENT_DB_VERSION).apply()
        }
        
        AndroidSqliteDriver(
            schema = SpoolDatabase.Schema,
            context = context,
            name = dbName
        )
    }
    single<SettingsStorage> { AndroidSettingsStorage(get()) }
}