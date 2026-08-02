package com.spoolsense.shared.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventorySpoolViewModel(private val observeSpoolsUseCase: ObserveSpoolsUseCase) : ViewModel() {

    private val _state = MutableStateFlow(InventorySpoolState())
    val state: StateFlow<InventorySpoolState> = _state.asStateFlow()

    init {
        loadSpools()
    }

    fun handleIntent(intent: InventorySpoolIntent) {
        when (intent) {
            is InventorySpoolIntent.UpdateSearch -> updateSearchQuery(intent.value)
            InventorySpoolIntent.Refresh -> loadSpools()
        }
    }

    private fun loadSpools() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            observeSpoolsUseCase()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unknown error"
                        )
                    }
                }
                .map { spools ->
                    _state.update {
                        it.copy(
                            allSpools = spools,
                            isLoading = false,
                            filteredSpools = filterSpools(spools, it.searchQuery)
                        )
                    }
                }
                .collect { }
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update { currentState ->
            val filtered = filterSpools(currentState.allSpools, query)
            currentState.copy(
                searchQuery = query,
                filteredSpools = filtered
            )
        }
    }

    private fun filterSpools(spools: List<Spool>, query: String): List<Spool> {
        if (query.isBlank()) return spools

        val lowerQuery = query.lowercase().trim()
        return spools.filter { spool ->
            spool.vendor.lowercase().contains(lowerQuery) ||
                    spool.material.lowercase().contains(lowerQuery) ||
                    spool.colorName.lowercase().contains(lowerQuery) ||
                    spool.name.lowercase().contains(lowerQuery)
        }
    }
}