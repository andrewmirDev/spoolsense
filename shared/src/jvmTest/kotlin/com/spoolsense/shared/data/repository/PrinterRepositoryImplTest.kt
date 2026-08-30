package com.spoolsense.shared.data.repository

import com.spoolsense.shared.data.network.dto.MoonrakerPrinterStateDto
import com.spoolsense.shared.domain.model.PrinterSettings
import com.spoolsense.shared.domain.model.PrinterStatus
import com.spoolsense.shared.domain.repository.ConnectionTestResult
import com.spoolsense.shared.domain.repository.PrinterSettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PrinterRepositoryImplTest {

    private val settingsRepository: PrinterSettingsRepository = mockk()

    private fun clientWith(body: String) = HttpClient(MockEngine { _ ->
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private fun setupSettings(configured: Boolean = true) {
        coEvery { settingsRepository.loadSettings() } returns
            PrinterSettings(host = if (configured) "192.168.1.100" else "", port = 7125)
    }

    @Test
    fun `maps printing state, temperatures and progress from real Moonraker response`() = runTest {
        setupSettings()
        val body = """
            {
              "result": {
                "status": {
                  "print_stats": { "state": "printing", "filament_used": 500.0, "filename": "benchy.gcode" },
                  "display_status": { "progress": 0.68 },
                  "heater_bed": { "temperature": 60.0, "target": 60.0 },
                  "extruder": { "temperature": 210.5, "target": 210.0 }
                }
              }
            }
        """.trimIndent()
        val repo = PrinterRepositoryImpl(clientWith(body), settingsRepository)

        val state = repo.observablePrinterState().first()

        assertEquals(PrinterStatus.Printing, state.status)
        assertEquals(0.68f, state.progress)
        assertEquals("benchy.gcode", state.printFileName)
        assertEquals(500.0, state.filamentUsedMm)
        assertEquals(210.5f, state.nozzleTemperature)
        assertEquals(210.0f, state.nozzleTarget)
        assertEquals(60.0f, state.bedTemperature)
        assertEquals(60.0f, state.bedTarget)
    }

    @Test
    fun `maps complete state`() = runTest {
        setupSettings()
        val body = """
            { "result": { "status": { "print_stats": { "state": "complete", "filename": "x.gcode" } } } }
        """.trimIndent()
        val repo = PrinterRepositoryImpl(clientWith(body), settingsRepository)

        val state = repo.observablePrinterState().first()

        assertEquals(PrinterStatus.Complete, state.status)
    }

    @Test
    fun `returns idle when response has no temperatures`() = runTest {
        setupSettings()
        val body = """
            { "result": { "status": { "print_stats": { "state": "standby" } } } }
        """.trimIndent()
        val repo = PrinterRepositoryImpl(clientWith(body), settingsRepository)

        val state = repo.observablePrinterState().first()

        assertEquals(PrinterStatus.Idle, state.status)
        assertEquals(0f, state.nozzleTemperature)
        assertEquals(0f, state.bedTemperature)
    }

    @Test
    fun `returns disconnected when printer not configured`() = runTest {
        setupSettings(configured = false)
        val repo = PrinterRepositoryImpl(clientWith("{}"), settingsRepository)

        val state = repo.observablePrinterState().first()

        assertEquals(PrinterStatus.Disconnected, state.status)
    }

    @Test
    fun `returns disconnected on network error`() = runTest {
        setupSettings()
        val repo = PrinterRepositoryImpl(throwOnRequestClient(), settingsRepository)

        val state = repo.observablePrinterState().first()

        assertEquals(PrinterStatus.Disconnected, state.status)
    }

    @Test
    fun `testConnection reports success with server info`() = runTest {
        val body = """
            { "result": { "moonraker_version": "v0.7.1", "klippy_connected": true, "klippy_state": "ready" } }
        """.trimIndent()
        val repo = PrinterRepositoryImpl(clientWith(body), settingsRepository)

        val result = repo.testConnection(PrinterSettings(host = "192.168.1.100", port = 7125))

        val success = assertIs<ConnectionTestResult.Success>(result)
        assertTrue(success.serverInfo.contains("v0.7.1"))
        assertTrue(success.serverInfo.contains("klippy: ready"))
    }

    @Test
    fun `testConnection reports failure when host is blank`() = runTest {
        val repo = PrinterRepositoryImpl(clientWith("{}"), settingsRepository)

        val result = repo.testConnection(PrinterSettings(host = "", port = 7125))

        assertIs<ConnectionTestResult.Failure>(result)
    }

    @Test
    fun `testConnection reports failure on network error`() = runTest {
        val repo = PrinterRepositoryImpl(throwOnRequestClient(), settingsRepository)

        val result = repo.testConnection(PrinterSettings(host = "192.168.1.100", port = 7125))

        assertIs<ConnectionTestResult.Failure>(result)
    }

    private fun throwOnRequestClient(): HttpClient {
        val engine = MockEngine { _ ->
            throw RuntimeException("network down")
        }
        return HttpClient(engine)
    }
}