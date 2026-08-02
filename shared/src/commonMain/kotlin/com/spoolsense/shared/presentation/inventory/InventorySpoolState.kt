package com.spoolsense.shared.presentation.inventory

import com.spoolsense.shared.domain.model.Spool

data class InventorySpoolState(
    val allSpools: List<Spool> = emptyList(),
    val filteredSpools: List<Spool> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
