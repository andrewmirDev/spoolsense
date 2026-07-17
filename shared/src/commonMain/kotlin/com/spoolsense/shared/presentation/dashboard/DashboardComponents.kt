package com.spoolsense.shared.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.shared.domain.model.PrinterState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import spoolsense.shared.generated.resources.*

@Composable
fun DashboardTopBar(isSynced: Boolean) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSynced) {
                    Icon(
                        vectorResource(Res.drawable.sync_24dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Synced",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },

        navigationIcon = {
            Text(text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(vectorResource(Res.drawable.account_circle_24dp), contentDescription = stringResource(Res.string.profile))
            }
        }
    )
}

@Composable
fun SummaryCard(totalKg: Float, spoolCount: Int){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(contentColor = MaterialTheme.colorScheme.surfaceVariant)
    ){
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(Res.string.total_fil_remaining_kg),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "${totalKg}kg",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(Res.string.across_spools, spoolCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun PrinterStatusCard(printer: PrinterState){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(contentColor = MaterialTheme.colorScheme.surfaceVariant)
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.active_print), fontWeight = FontWeight.Bold)
                Text(text = "${(printer.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { printer.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(Res.string.nozzle_temp))
        }
    }
}

@Composable
fun LowFilamentItem(spool: Spool){
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.errorContainer)
        ){
            Spacer(Modifier.width(12.dp))
            Column {
                Text("${spool.vendor} ${spool.colorHex}", fontWeight = FontWeight.Medium)
                Text(text = stringResource(Res.string.low_rem_left, spool.remainingWeightGrams))
            }
        }


    }
}