package com.spoolsense.app.shared.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoonrakerResponseDto<T>(
    @SerialName("result") val result: T
)

@Serializable
data class MoonrakerPrinterStateDto(
    @SerialName("state") val state: String,
    @SerialName("state_message") val stateMessage: String = ""
)

@Serializable
data class MoonrakerTemperatureDto(
    @SerialName("temperature") val temperature: Float,
    @SerialName("target") val target: Float
)
