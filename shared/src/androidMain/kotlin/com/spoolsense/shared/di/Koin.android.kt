package com.spoolsense.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.spoolsense.shared.database.SpoolDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = SpoolDatabase.Schema,
            context = get(),
            name = "spoolsense.db"
        )
    }
}