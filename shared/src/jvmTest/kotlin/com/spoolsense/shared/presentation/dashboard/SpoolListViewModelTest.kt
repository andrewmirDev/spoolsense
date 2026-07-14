package com.spoolsense.shared.presentation.dashboard

import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpoolListViewModelTest {

    private val observeSpoolsUseCase: ObserveSpoolsUseCase = mockk()
    private val observePrinterStateUseCase: ObservePrinterStateUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup(){
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown(){
        Dispatchers.resetMain()
    }

    @Test
    fun IntentRemainWeight(){
        val spools = listOf(
            Spool(id = "1", name = "redAbs", material = "ABS", remainingWeightGrams = 1000, totalWeightGrams = 750, vendor = "eSUN", colorHex = "RED"),
            Spool(id = "2", name = "pinkAbs", material = "ABS", remainingWeightGrams = 800, totalWeightGrams = 650, vendor = "eSUN", colorHex = "PINK")
        )
        every { observeSpoolsUseCase() } returns flowOf(spools)
        every { observePrinterStateUseCase() } returns flowOf(mockk(relaxed = true))

        val viewModel = SpoolListViewModel(observeSpoolsUseCase, observePrinterStateUseCase)

        viewModel.handleIntent(SpoolListIntent.Init)

        val currentState = viewModel.state.value
        assertEquals(2, currentState.totalSpoolCount)
        assertEquals(1.8f, currentState.totalFilamentKg)
        assertEquals(false, currentState.isLoading)
    }

}