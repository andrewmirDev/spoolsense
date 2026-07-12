package com.spoolsense.shared.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.spoolsense.shared.database.SpoolDatabase

actual fun createTestSqlDriver(): SqlDriver {
    return NativeSqliteDriver(SpoolDatabase.Schema, "test.db")
}