package com.spoolsense.shared.repository

import com.spoolsense.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.data.database.createTestSqlDriver
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest


class SpoolRepositoryTest {
    private lateinit var repository: SpoolRepositoryImpl
    private lateinit var database: SpoolDatabase

    @BeforeTest
    fun setup() {
        val driver = createTestSqlDriver()
        database = SpoolDatabase(driver)
        repository = SpoolRepositoryImpl(database)
    }

    @Test
    fun `insert spool and retrieve it should return correct data`() = runTest {
        val testSpool = Spool(
            id = "1",
            name = "PLA Red",
            vendor = "eSUN",
            material = "PLA",
            totalWeightGrams = 1000,
            remainingWeightGrams = 750,
            colorHex = "#FF0000"
        )

        repository.insertSpool(testSpool)

        val spool = repository.getSpoolById("1")

        assertNotNull(spool)
        assertEquals("eSUN", spool.vendor)
        assertEquals(750, spool.remainingWeightGrams)
    }
}
