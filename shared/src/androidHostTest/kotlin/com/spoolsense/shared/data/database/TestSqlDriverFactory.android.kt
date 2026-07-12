package com.spoolsense.shared.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.spoolsense.shared.database.SpoolDatabase

actual fun createTestSqlDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    SpoolDatabase.Schema.create(driver)
    return driver
}