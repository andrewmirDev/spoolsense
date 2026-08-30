package com.spoolsense.shared.data.repository

import com.spoolsense.shared.data.network.dto.MoonrakerPrinterStateDto
import com.spoolsense.shared.data.network.dto.ServerInfoDto
import com.spoolsense.shared.domain.model.PrinterSettings
import com.spoolsense.shared.domain.model.PrinterState
import com.spoolsense.shared.domain.model.PrinterStatus
import com.spoolsense.shared.domain.repository.ConnectionTestResult
import com.spoolsense.shared.domain.repository.PrinterRepository
import com.spoolsense.shared.domain.repository.PrinterSettingsRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class PrinterRepositoryImpl(
    private val httpClient: HttpClient,
    private val settingsRepository: PrinterSettingsRepository
) : PrinterRepository {

    override fun observablePrinterState(): Flow<PrinterState> = flow {
        while (true) {
            val settings = settingsRepository.loadSettings()
            if (!settings.isConfigured) {
                emit(PrinterState(status = PrinterStatus.Disconnected))
                delay(2.seconds)
                continue
            }
            val state = try {
                val response: MoonrakerPrinterStateDto = httpClient
                    .get("${settings.baseUrl}/printer/objects/query?print_stats&display_status&extruder&heater_bed&webhooks") {
                        apiKeyHeader(settings)
                    }
                    .body()
                mapToDomain(response)
            } catch (e: Exception) {
                PrinterState(status = PrinterStatus.Disconnected)
            }
            emit(state)
            delay(2.seconds)
        }
    }

    override suspend fun testConnection(settings: PrinterSettings): ConnectionTestResult {
        if (settings.host.isBlank()) {
            return ConnectionTestResult.Failure("Host is empty")
        }
        return try {
            val response: ServerInfoDto = httpClient
                .get("${settings.baseUrl}/server/info") {
                    apiKeyHeader(settings)
                }
                .body()
            val result = response.result
            if (result == null) {
                ConnectionTestResult.Failure("Unexpected response from Moonraker")
            } else {
                val klippy = if (result.klippyConnected) result.klippyState.ifBlank { "connected" } else "NOT connected"
                ConnectionTestResult.Success("${result.moonrakerVersion.ifBlank { "Moonraker" }} (klippy: $klippy)")
            }
        } catch (e: Exception) {
            ConnectionTestResult.Failure(readableError(e, settings.baseUrl))
        }
    }

    private fun mapToDomain(dto: MoonrakerPrinterStateDto): PrinterState {
        val status = dto.result?.status
        return PrinterState(
            status = mapStatus(status?.printStats?.state),
            nozzleTemperature = status?.extruder?.temperature ?: 0f,
            nozzleTarget = status?.extruder?.target ?: 0f,
            bedTemperature = status?.heaterBed?.temperature ?: 0f,
            bedTarget = status?.heaterBed?.target ?: 0f,
            progress = status?.displayStatus?.progress ?: status?.printStats?.progress ?: 0f,
            printFileName = status?.printStats?.filename.orEmpty(),
            filamentUsedMm = status?.printStats?.filamentUsed ?: 0.0,
            stateMessage = status?.printStats?.state.orEmpty()
        )
    }

    private fun mapStatus(state: String?): PrinterStatus = when (state) {
        "printing" -> PrinterStatus.Printing
        "paused" -> PrinterStatus.Paused
        "complete" -> PrinterStatus.Complete
        "cancelled", "error" -> PrinterStatus.Error
        null, "standby", "ready" -> PrinterStatus.Idle
        else -> PrinterStatus.Idle
    }

    private fun readableError(e: Exception, baseUrl: String): String {
        val message = e.message ?: e::class.simpleName ?: "unknown error"
        val kind = when {
            e is io.ktor.client.plugins.HttpRequestTimeoutException -> "timeout"
            message.contains("Failed to connect", ignoreCase = true) ||
                message.contains("Connection refused", ignoreCase = true) -> "printer unreachable at $baseUrl (ports closed or wrong IP)"
            message.contains("Cleartext", ignoreCase = true) -> "cleartext HTTP is blocked"
            message.contains("UnknownHost", ignoreCase = true) ||
                message.contains("Unresolved address", ignoreCase = true) -> "host not resolved (wrong IP or different network)"
            message.contains("401", ignoreCase = true) ||
                message.contains("Forbidden", ignoreCase = true) -> "authorization required (check API key)"
            else -> message.take(160)
        }
        return "$kind (${e::class.simpleName})"
    }

    private fun io.ktor.client.request.HttpRequestBuilder.apiKeyHeader(settings: PrinterSettings) {
        if (settings.apiKey.isNotBlank()) {
            header("X-Api-Key", settings.apiKey)
        }
    }
}