package com.spoolsense.shared.presentation.dashboard

sealed interface DashboardSpoolListIntent {
    object Init: DashboardSpoolListIntent
    object RefreshPrinterStatus: DashboardSpoolListIntent
    data class OnSpoolClick(val spoolId: String): DashboardSpoolListIntent
    data class OnDashboardSpool(val spoolId: String): DashboardSpoolListIntent
}