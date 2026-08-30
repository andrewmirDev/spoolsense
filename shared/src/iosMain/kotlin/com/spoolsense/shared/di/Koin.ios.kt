package com.spoolsense.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.spoolsense.shared.data.prefs.IosSettingsStorage
import com.spoolsense.shared.data.prefs.SettingsStorage
import com.spoolsense.shared.database.SpoolDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> {
        NativeSqliteDriver(SpoolDatabase.Schema, "spoolsense.db")
    }
    single<SettingsStorage> { IosSettingsStorage() }
}
