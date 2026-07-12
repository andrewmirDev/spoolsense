package com.spoolsense.shared.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoonrakerPrinterStateDto(
    // Moonraker в ключе "status" передает объект с состояниями
    @SerialName("status") val status: PrinterStatusDto? = null
)

@Serializable
data class PrinterStatusDto(
    @SerialName("print_stats") val printStats: PrintStatsDto? = null,
    @SerialName("heater_bed") val heaterBed: HeaterDto? = null,
    @SerialName("extruder") val extruder: HeaterDto? = null,
    @SerialName("webhooks") val webhooks: WebhooksDto? = null
)

@Serializable
data class PrintStatsDto(
    @SerialName("progress") val progress: Float = 0.0f,
    @SerialName("state") val state: String = "unknown" // Вот тот самый state, который мы искали
)

@Serializable
data class WebhooksDto(
    @SerialName("state") val state: String = "disconnected"
)

@Serializable
data class HeaterDto(
    @SerialName("temperature") val temperature: Float = 0.0f,
    @SerialName("target") val target: Float = 0.0f
)