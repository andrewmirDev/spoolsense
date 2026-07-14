package com.spoolsense.shared.domain.usecase

import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.app.shared.domain.repository.SpoolRepository
import com.spoolsense.shared.domain.repository.PrinterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import com.spoolsense.shared.domain.model.PrinterState


class UseCaseTests {
    private val spoolRepository: SpoolRepository = mockk()
    private val printerRepository: PrinterRepository = mockk()

    @Test
    fun ObserveSpoolUseCaseTest() = runTest {
        val spools = listOf(Spool(id = "1", name = "redAbs", material = "ABS", remainingWeightGrams = 1000, totalWeightGrams = 750, vendor = "eSUN", colorHex = "RED"))
        every{spoolRepository.observeAllSpools()} returns flowOf(spools)

        val useCase = ObserveSpoolsUseCase(spoolRepository)
        val result = useCase().first()

        assertEquals(spools, result)
    }

    @Test
    fun UpdateSpoolWeightUseCaseTest() = runTest {
        coEvery { spoolRepository.updateRemainingWeight(any(), any()) }  returns Unit

        val useCase = UpdateSpoolWeightUseCase(spoolRepository)
        useCase("1", 500)

        coVerify { spoolRepository.updateRemainingWeight("1", 500) }
    }

    @Test
    fun ObservePrinterStateUseCaseTest() = runTest {
        val printerState = PrinterState(progress = 0.5f)
        every { printerRepository.observablePrinterState() } returns flowOf(printerState)

        val useCase = ObservePrinterStateUseCase(printerRepository)
        val result = useCase().first()

        assertEquals(printerState, result)
    }
}