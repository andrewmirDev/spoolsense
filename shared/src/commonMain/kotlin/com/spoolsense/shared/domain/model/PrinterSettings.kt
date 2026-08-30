package com.spoolsense.shared.domain.model

data class PrinterSettings(
    val host: String = "",
    val port: Int = 7125,
    val apiKey: String = ""
) {
    val baseUrl: String
        get() = "http://$host:$port"

    val wsUrl: String
        get() = "ws://$host:$port/websocket"

    val isConfigured: Boolean
        get() = host.isNotBlank()
}
