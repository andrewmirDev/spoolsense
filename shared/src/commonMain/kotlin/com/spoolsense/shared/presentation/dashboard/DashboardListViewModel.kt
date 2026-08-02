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

class DashboardListViewModel(
    private val observeSpoolsUseCase: ObserveSpoolsUseCase,
    private val observePrinterStateUseCase: ObservePrinterStateUseCase
): ViewModel() {

    private val _state = MutableStateFlow(DashboardSpoolListState())
    val state: StateFlow<DashboardSpoolListState> = _state.asStateFlow()

    fun handleIntent(intent: DashboardSpoolListIntent){
        when(intent){
            DashboardSpoolListIntent.Init -> startObservingData()
            DashboardSpoolListIntent.RefreshPrinterStatus -> {}
            else -> {}
        }
    }

    private fun startObservingData() {
        println("🔍 DashboardVM.startObservingData() called")
        _state.update { it.copy(isLoading = true) }
        
        println("🔍 Setting up spools observation...")
        observeSpoolsUseCase()
            .onEach { spools ->
                println("🔍 DashboardVM received ${spools.size} spools")
                spools.forEach { spool ->
                    println("   - ${spool.vendor} ${spool.colorName} (${spool.remainingWeightGrams}g)")
                }
                
                _state.update { currentState ->
                    val totalKg = spools.sumOf { it.remainingWeightGrams.toDouble() }.toFloat() / 1000
                    val lowSpools = spools.filter { it.remainingWeightGrams < 150 }
                    
                    println("📊 Total: ${totalKg}kg, Low filament: ${lowSpools.size}")
                    
                    currentState.copy(
                        allSpools = spools,
                        totalSpoolCount = spools.size,
                        totalFilamentKg = totalKg,
                        lowFilamentSpools = lowSpools,
                        isLoading = false
                    )
                }
            }.launchIn(viewModelScope)

        println("🔍 Setting up printer state observation...")
        observePrinterStateUseCase().onEach { printerState ->
            println("🖨️ Printer state: $printerState")
            _state.update { it.copy(activePrinterState = printerState)
            }
        }.launchIn(viewModelScope)
    }
}
