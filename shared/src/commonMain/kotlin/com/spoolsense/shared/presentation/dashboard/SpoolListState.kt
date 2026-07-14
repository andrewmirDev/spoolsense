package com.spoolsense.shared.presentation.dashboard

import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.shared.domain.model.PrinterState

data class SpoolListState(
    val isLoading: Boolean = false,
    val totalFilamentKg: Float = 0f,
    val totalSpoolCount: Int = 0,
    val activePrinterState: PrinterState? = null,
    val lowFilamentSpools: List<Spool> = emptyList(),
    val allSpools: List<Spool> = emptyList(),
    val errorMessage: String? = null
)
