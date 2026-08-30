package com.spoolsense.app

import androidx.compose.runtime.*
import com.spoolsense.app.ui.theme.SpoolSenseTheme
import com.spoolsense.shared.presentation.dashboard.DashboardScreen
import com.spoolsense.shared.presentation.dashboard.DashboardListViewModel
import com.spoolsense.shared.presentation.settings.PrinterSettingsScreen
import com.spoolsense.shared.presentation.settings.PrinterSettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    SpoolSenseTheme {
        var showSettings by remember { mutableStateOf(false) }
        if (showSettings) {
            val settingsViewModel = koinViewModel<PrinterSettingsViewModel>()
            PrinterSettingsScreen(
                viewModel = settingsViewModel,
                onSaved = { showSettings = false }
            )
        } else {
            val dashboardViewModel = koinViewModel<DashboardListViewModel>()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onSettingsClick = { showSettings = true }
            )
        }
    }
}
