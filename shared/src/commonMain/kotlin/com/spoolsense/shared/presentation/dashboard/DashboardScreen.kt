package com.spoolsense.shared.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(viewModel: DashboardListViewModel){

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit){
        viewModel.handleIntent(DashboardSpoolListIntent.Init)
    }

    Scaffold(topBar = {DashboardTopBar(isSynced = !state.isLoading)
    }) { paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SummaryCard(
                totalKg = state.totalFilamentKg,
                spoolCount = state.totalSpoolCount
            )
            }

            state.activePrinterState?.let{ printer ->
                item{
                    PrinterStatusCard(printer)
                }
            }

            if(state.lowFilamentSpools.isNotEmpty()){
                item {
                    Text(
                        text = "Low filament alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                items(state.lowFilamentSpools) {spool ->
                    LowFilamentItem(spool)
                }
            }
        }
    }
}