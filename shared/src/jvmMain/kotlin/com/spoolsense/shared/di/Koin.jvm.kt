package com.spoolsense.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.spoolsense.shared.data.prefs.JvmSettingsStorage
import com.spoolsense.shared.data.prefs.SettingsStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }
    single<SettingsStorage> { JvmSettingsStorage() }
}