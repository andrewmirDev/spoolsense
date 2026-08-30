package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.data.database.createTestSqlDriver
import com.spoolsense.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.domain.model.Spool
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OnPrintCompletedUseCaseIdempotencyTest {

    @Test
    fun `repeated completion event with same job id deducts weight only once`() = runTest {
        val driver = createTestSqlDriver()
        val database = SpoolDatabase(driver)
        val repository = SpoolRepositoryImpl(database)
        val useCase = OnPrintCompletedUseCase(repository, UpdateSpoolWeightUseCase(repository))

        repository.insertSpool(
            Spool(
                id = "1",
                name = "spool",
                vendor = "v",
                material = "PLA",
                totalWeightGrams = 1000,
                remainingWeightGrams = 1000,
                colorHex = "FF0000"
            )
        )

        val first = useCase("1", "benchy.gcode", 1000.0, 12345L)
        val second = useCase("1", "benchy.gcode", 1000.0, 12345L)

        assertEquals(true, first.isSuccess)
        assertEquals(true, second.isSuccess)
        assertEquals(997, repository.getSpoolById("1")?.remainingWeightGrams)
    }
}