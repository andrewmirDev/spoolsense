package com.spoolsense.shared.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolsense.shared.domain.model.PrinterStatus
import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.model.PrinterState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import spoolsense.shared.generated.resources.*
import kotlin.math.roundToInt

@Composable
fun DashboardTopBar(isSynced: Boolean, onSettingsClick: () -> Unit = {}) {
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
            IconButton(onClick = onSettingsClick) {
                Icon(vectorResource(Res.drawable.account_circle_24dp), contentDescription = stringResource(Res.string.profile))
            }
        }
    )
}

@Composable
fun SummaryCard(totalKg: Float, spoolCount: Int){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ){
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(Res.string.total_fil_remaining_kg),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${totalKg}kg",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(Res.string.across_spools, spoolCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PrinterStatusCard(printer: PrinterState){
    val statusLabel = statusLabel(printer.status)
    val statusColor = when (printer.status) {
        PrinterStatus.Printing -> MaterialTheme.colorScheme.primary
        PrinterStatus.Error -> MaterialTheme.colorScheme.error
        PrinterStatus.Disconnected -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = statusLabel,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (printer.progress > 0f) {
                    Text(text = "${(printer.progress * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary)
                }
            }
            if (printer.progress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { printer.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (printer.printFileName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.print_file, printer.printFileName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.nozzle_temp, formatTemp(printer.nozzleTemperature), formatTemp(printer.nozzleTarget)),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.bed_temp, formatTemp(printer.bedTemperature), formatTemp(printer.bedTarget)),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTemp(value: Float): String =
    if (value == 0f) "—" else value.roundToInt().toString()

@Composable
private fun statusLabel(status: PrinterStatus): String = stringResource(
    when (status) {
        PrinterStatus.Printing -> Res.string.status_printing
        PrinterStatus.Idle -> Res.string.status_idle
        PrinterStatus.Paused -> Res.string.status_paused
        PrinterStatus.Error -> Res.string.status_error
        PrinterStatus.Disconnected -> Res.string.status_disconnected
        PrinterStatus.Complete -> Res.string.status_complete
    }
)

@Composable
fun LowFilamentItem(spool: Spool){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(MaterialTheme.colorScheme.error, shape = RoundedCornerShape(4.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${spool.vendor} ${spool.colorName}",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(Res.string.low_rem_left, spool.remainingWeightGrams),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}