package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.repository.SpoolRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnPrintCompletedUseCaseTest {

    private val spoolRepository: SpoolRepository = mockk()
    private val updateSpoolWeightUseCase: UpdateSpoolWeightUseCase = mockk()

    private fun spool(remaining: Int, total: Int) = Spool(
        id = "1",
        name = "spool",
        vendor = "v",
        material = "PLA",
        remainingWeightGrams = remaining,
        totalWeightGrams = total,
        colorHex = "FF0000"
    )

    @Test
    fun `deducts filament weight and records print job`() = runTest {
        val spool = spool(remaining = 1000, total = 1000)
        coEvery { spoolRepository.printJobExists(any()) } returns false
        coEvery { spoolRepository.getSpoolById("1") } returns spool
        coEvery { updateSpoolWeightUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { spoolRepository.insertPrintJob(any(), any(), any(), any(), any(), any()) } returns Unit

        val useCase = OnPrintCompletedUseCase(spoolRepository, updateSpoolWeightUseCase)
        val result = useCase("1", "benchy.gcode", 1000.0, 12345L)

        assertTrue(result.isSuccess)
        coVerify { spoolRepository.insertPrintJob(any(), "benchy.gcode", "1", 3, 12345L, "SUCCESS") }
    }

    @Test
    fun `returns failure when no active spool`() = runTest {
        coEvery { spoolRepository.printJobExists(any()) } returns false
        coEvery { spoolRepository.getSpoolById(any()) } returns null

        val useCase = OnPrintCompletedUseCase(spoolRepository, updateSpoolWeightUseCase)
        val result = useCase("missing", "x.gcode", 1000.0, 1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure when filament usage is zero`() = runTest {
        coEvery { spoolRepository.printJobExists(any()) } returns false
        coEvery { spoolRepository.getSpoolById("1") } returns spool(remaining = 1000, total = 1000)

        val useCase = OnPrintCompletedUseCase(spoolRepository, updateSpoolWeightUseCase)
        val result = useCase("1", "x.gcode", 0.0, 1L)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { spoolRepository.insertPrintJob(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `clamps new weight to zero`() = runTest {
        val spool = spool(remaining = 1, total = 1000)
        coEvery { spoolRepository.printJobExists(any()) } returns false
        coEvery { spoolRepository.getSpoolById("1") } returns spool
        coEvery { updateSpoolWeightUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { spoolRepository.insertPrintJob(any(), any(), any(), any(), any(), any()) } returns Unit

        val useCase = OnPrintCompletedUseCase(spoolRepository, updateSpoolWeightUseCase)
        val result = useCase("1", "x.gcode", 100000.0, 1L)

        assertTrue(result.isSuccess)
        coVerify { updateSpoolWeightUseCase("1", 0) }
    }
}
