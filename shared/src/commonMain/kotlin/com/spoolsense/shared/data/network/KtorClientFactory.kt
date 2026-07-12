package com.spoolsense.app.shared.data.network

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
            // Настройка парсинга JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Игнорируем поля из API, которые нам не нужны
                })
            }

            // Настройка WebSockets для реалтайм статусов
            install(WebSockets) {
                pingInterval = 20.seconds // Поддержание соединения (20 секунд)
            }
        }
    }
}