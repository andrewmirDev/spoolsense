package com.spoolsense.shared.data.network

import com.spoolsense.shared.data.network.dto.MoonrakerPrinterStateDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MoonrakerMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse printer status JSON should map to DTO correctly`() {
        // Имитируем реальную вложенность Moonraker API
        val jsonResponse = """
            {
                "status": {
                    "print_stats": { 
                        "state": "printing", 
                        "progress": 0.68 
                    },
                    "heater_bed": { "temperature": 60.0 },
                    "extruder": { "temperature": 210.5 }
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(jsonResponse)

        // Проверяем вложенные объекты (обязательно для надежности)
        assertNotNull(dto.status)

        // Ожидаем значения из вложенных структур [cite: 5, 48]
        assertEquals("printing", dto.status?.printStats?.state)
        assertEquals(0.68f, dto.status?.printStats?.progress)
        assertEquals(60.0f, dto.status?.heaterBed?.temperature)
        assertEquals(210.5f, dto.status?.extruder?.temperature)
    }

    @Test
    fun `missing fields in JSON should use default values in DTO`() {
        // Тестируем устойчивость к отсутствующим данным (например, только статус)
        val minimalJson = """{ "status": { "print_stats": { "state": "idle" } } }"""

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(minimalJson)

        assertEquals("idle", dto.status?.printStats?.state)
        // Ожидаем 0.0f по умолчанию, если ключа нет в JSON [cite: 108]
        assertEquals(0.0f, dto.status?.heaterBed?.temperature ?: 0.0f)
    }
}
