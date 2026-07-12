package com.spoolsense.shared.repository

import com.spoolsense.app.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.data.database.createTestSqlDriver
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest


class SpoolRepositoryTest {
    private lateinit var repository: SpoolRepositoryImpl
    private lateinit var database: SpoolDatabase

    @BeforeTest
    fun setup() {
        // Используем нашу фабрику для создания драйвера
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

        // 1. Сохраняем в базу
        repository.insertSpool(testSpool)

        // 2. Получаем список через Flow и берем первый снимок (emission)
        val spools = repository.observeAllSpools().first()

        // 3. Проверяем корректность (обращаемся к первому элементу списка)
        assertEquals(1, spools.size)
        assertEquals("eSUN", spools.first().vendor)
    }
}
