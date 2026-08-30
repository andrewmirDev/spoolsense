package com.spoolsense.shared.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoonrakerPrinterStateDto(
    @SerialName("result") val result: MoonrakerResultDto? = null
)

@Serializable
data class MoonrakerResultDto(
    @SerialName("status") val status: PrinterStatusDto? = null,
    @SerialName("eventtime") val eventtime: Double? = null
)

@Serializable
data class PrinterStatusDto(
    @SerialName("print_stats") val printStats: PrintStatsDto? = null,
    @SerialName("display_status") val displayStatus: DisplayStatusDto? = null,
    @SerialName("heaters") val heaters: HeatersDto? = null,
    @SerialName("heater_bed") val heaterBed: HeaterDto? = null,
    @SerialName("extruder") val extruder: HeaterDto? = null,
    @SerialName("webhooks") val webhooks: WebhooksDto? = null
)

@Serializable
data class PrintStatsDto(
    @SerialName("progress") val progress: Float = 0.0f,
    @SerialName("state") val state: String = "unknown",
    @SerialName("filename") val filename: String = "",
    @SerialName("print_duration") val printDuration: Double = 0.0,
    @SerialName("total_duration") val totalDuration: Double = 0.0,
    @SerialName("filament_used") val filamentUsed: Double = 0.0
)

@Serializable
data class DisplayStatusDto(
    @SerialName("progress") val progress: Float = 0.0f,
    @SerialName("message") val message: String? = null
)

@Serializable
data class HeatersDto(
    @SerialName("available_heaters") val availableHeaters: List<String> = emptyList(),
    @SerialName("available_sensors") val availableSensors: List<String> = emptyList()
)

@Serializable
data class WebhooksDto(
    @SerialName("state") val state: String = "disconnected"
)

@Serializable
data class HeaterDto(
    @SerialName("temperature") val temperature: Float = 0.0f,
    @SerialName("target") val target: Float = 0.0f,
    @SerialName("power") val power: Float = 0.0f
)

@Serializable
data class ServerInfoDto(
    @SerialName("result") val result: ServerInfoResultDto? = null
)

@Serializable
data class ServerInfoResultDto(
    @SerialName("klippy_connected") val klippyConnected: Boolean = false,
    @SerialName("klippy_state") val klippyState: String = "",
    @SerialName("moonraker_version") val moonrakerVersion: String = "",
    @SerialName("api_version_string") val apiVersionString: String = ""
)