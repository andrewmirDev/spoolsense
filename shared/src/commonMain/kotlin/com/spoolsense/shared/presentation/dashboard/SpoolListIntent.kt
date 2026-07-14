package com.spoolsense.shared.presentation.dashboard

sealed interface SpoolListIntent {
    object Init: SpoolListIntent
    object RefreshPrinterStatus: SpoolListIntent
    data class OnSpoolClick(val spoolId: String): SpoolListIntent
    data class OnDeleteSpool(val spoolId: String): SpoolListIntent
}