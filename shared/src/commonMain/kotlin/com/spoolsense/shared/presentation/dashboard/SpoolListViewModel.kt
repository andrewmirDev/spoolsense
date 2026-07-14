package com.spoolsense.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SpoolListViewModel(
    private val observeSpoolsUseCase: ObserveSpoolsUseCase,
    private val observePrinterStateUseCase: ObservePrinterStateUseCase
): ViewModel() {

    private val _state = MutableStateFlow(SpoolListState())
    val state: StateFlow<SpoolListState> = _state.asStateFlow()

    fun handleIntent(intent: SpoolListIntent){
        when(intent){
            SpoolListIntent.Init -> startObservingData()
            SpoolListIntent.RefreshPrinterStatus -> {}
            else -> {}
        }
    }

    private fun startObservingData() {
        _state.update { it.copy(isLoading = true) }
        observeSpoolsUseCase()
            .onEach { spools ->
                _state.update { currentState ->
                currentState.copy(allSpools = spools,
                    totalSpoolCount = spools.size,
                    totalFilamentKg = spools.sumOf { it.remainingWeightGrams.toDouble() }.toFloat() / 1000,
                    lowFilamentSpools = spools.filter { it.remainingWeightGrams > 150f },
                    isLoading = false
                )
                }
            }.launchIn(viewModelScope)

        observePrinterStateUseCase().onEach { printerState ->
            _state.update { it.copy(activePrinterState = printerState)
            }
        }.launchIn(viewModelScope)
    }
}
