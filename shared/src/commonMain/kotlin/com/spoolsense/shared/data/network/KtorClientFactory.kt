package com.spoolsense.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object KtorClientFactory {
    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                })
            }

            install(WebSockets) {
                pingInterval = 20.seconds
            }
        }
    }
}