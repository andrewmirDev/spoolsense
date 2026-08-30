package com.spoolsense.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolsense.shared.domain.model.PrinterStatus
import com.spoolsense.shared.domain.usecase.ObserveActiveSpoolUseCase
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import com.spoolsense.shared.domain.usecase.OnPrintCompletedUseCase
import com.spoolsense.shared.domain.usecase.SetActiveSpoolUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class DashboardListViewModel(
    private val observeSpoolsUseCase: ObserveSpoolsUseCase,
    private val observePrinterStateUseCase: ObservePrinterStateUseCase,
    private val observeActiveSpoolUseCase: ObserveActiveSpoolUseCase,
    private val setActiveSpoolUseCase: SetActiveSpoolUseCase,
    private val onPrintCompletedUseCase: OnPrintCompletedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardSpoolListState())
    val state: StateFlow<DashboardSpoolListState> = _state.asStateFlow()

    private var lastState: String? = null
    private var lastFileName: String? = null

    fun handleIntent(intent: DashboardSpoolListIntent) {
        when (intent) {
            DashboardSpoolListIntent.Init -> startObservingData()
            DashboardSpoolListIntent.RefreshPrinterStatus -> {}
            is DashboardSpoolListIntent.OnSpoolClick -> {}
            is DashboardSpoolListIntent.OnDashboardSpool -> {}
            is DashboardSpoolListIntent.SelectActiveSpool -> selectActiveSpool(intent.spoolId)
        }
    }

    private fun startObservingData() {
        _state.update { it.copy(isLoading = true) }

        observeSpoolsUseCase()
            .onEach { spools ->
                _state.update { currentState ->
                    val totalKg = spools.sumOf { it.remainingWeightGrams.toDouble() }.toFloat() / 1000
                    val lowSpools = spools.filter { it.remainingWeightGrams < 150 }
                    currentState.copy(
                        allSpools = spools,
                        totalSpoolCount = spools.size,
                        totalFilamentKg = totalKg,
                        lowFilamentSpools = lowSpools,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)

        observeActiveSpoolUseCase().onEach { activeId ->
            _state.update { it.copy(activeSpoolId = activeId) }
        }.launchIn(viewModelScope)

        observePrinterStateUseCase().onEach { printerState ->
            handlePrinterState(printerState.status, printerState)
        }.launchIn(viewModelScope)
    }

    private fun selectActiveSpool(spoolId: String) {
        viewModelScope.launch {
            setActiveSpoolUseCase(spoolId)
        }
    }

    private fun handlePrinterState(status: PrinterStatus, printerState: com.spoolsense.shared.domain.model.PrinterState) {
        val fileName = printerState.printFileName

        if (status == PrinterStatus.Complete) {
            val shouldDeduct =
                lastState != "complete" &&
                        fileName.isNotBlank() &&
                        fileName != lastFileName &&
                        !_state.value.printDeducted

            if (shouldDeduct) {
                deductWeight(fileName, printerState.filamentUsedMm)
            }
        }

        lastState = when (status) {
            PrinterStatus.Complete -> "complete"
            PrinterStatus.Printing -> "printing"
            PrinterStatus.Idle -> "idle"
            PrinterStatus.Paused -> "paused"
            PrinterStatus.Error -> "error"
            PrinterStatus.Disconnected -> "disconnected"
        }
        if (fileName.isNotBlank()) {
            lastFileName = fileName
        }

        _state.update { it.copy(activePrinterState = printerState) }
    }

    private fun deductWeight(fileName: String, filamentUsedMm: Double) {
        val activeSpoolId = _state.value.activeSpoolId ?: return
        viewModelScope.launch {
            val result = onPrintCompletedUseCase(activeSpoolId, fileName, filamentUsedMm, Clock.System.now().toEpochMilliseconds())
            _state.update {
                it.copy(
                    printDeducted = result.isSuccess,
                    errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }
}
