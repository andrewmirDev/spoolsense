package com.spoolsense.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.spoolsense.app.ui.theme.SpoolSenseTheme
import com.spoolsense.shared.presentation.dashboard.DashboardScreen
import com.spoolsense.shared.presentation.dashboard.DashboardListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val viewModel = koinViewModel<DashboardListViewModel>()
        DashboardScreen(viewModel)
    }
    SpoolSenseTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        }
    }
}