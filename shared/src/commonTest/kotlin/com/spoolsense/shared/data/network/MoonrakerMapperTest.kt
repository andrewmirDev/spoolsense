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
        val jsonResponse = """
            {
                "result": {
                    "status": {
                        "print_stats": {
                            "state": "printing",
                            "filename": "benchy.gcode",
                            "filament_used": 1234.5
                        },
                        "display_status": {
                            "progress": 0.68
                        },
                        "heater_bed": { "temperature": 60.0, "target": 60.0 },
                        "extruder": { "temperature": 210.5, "target": 210.0 }
                    }
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(jsonResponse)

        assertNotNull(dto.result?.status)

        assertEquals("printing", dto.result?.status?.printStats?.state)
        assertEquals(0.68f, dto.result?.status?.displayStatus?.progress)
        assertEquals(60.0f, dto.result?.status?.heaterBed?.temperature)
        assertEquals(210.5f, dto.result?.status?.extruder?.temperature)
        assertEquals(210.0f, dto.result?.status?.extruder?.target)
        assertEquals("benchy.gcode", dto.result?.status?.printStats?.filename)
        assertEquals(1234.5, dto.result?.status?.printStats?.filamentUsed)
    }

    @Test
    fun `missing fields in JSON should use default values in DTO`() {
        val minimalJson = """{ "result": { "status": { "print_stats": { "state": "standby" } } } }"""

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(minimalJson)

        assertEquals("standby", dto.result?.status?.printStats?.state)
        assertEquals(0.0f, dto.result?.status?.heaterBed?.temperature ?: 0.0f)
    }

    @Test
    fun `response without result wrapper should parse to nulls`() {
        val unexpected = """{ "status": { "print_stats": { "state": "printing" } } }"""

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(unexpected)

        assertEquals(null, dto.result)
    }

    @Test
    fun `live Moonraker payload with null message parses without failure`() {
        val live = """
            {
              "result": {
                "status": {
                  "display_status": { "progress": 0.0, "message": null },
                  "webhooks": { "state": "ready", "state_message": "Printer is ready" },
                  "heater_bed": { "temperature": 29.49, "power": 0.0, "target": 0.0 },
                  "print_stats": {
                    "info": { "total_layer": null, "current_layer": null },
                    "print_duration": 0.0, "total_duration": 0.0, "filament_used": 0.0,
                    "filename": "", "state": "standby", "message": ""
                  },
                  "extruder": {
                    "pressure_advance": 0.032, "target": 0.0, "power": 0.0,
                    "can_extrude": false, "smooth_time": 0.03, "temperature": 36.5
                  }
                },
                "eventtime": 769.202178949
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<MoonrakerPrinterStateDto>(live)

        val status = dto.result?.status
        assertEquals("standby", status?.printStats?.state)
        assertEquals(36.5f, status?.extruder?.temperature)
        assertEquals(29.49f, status?.heaterBed?.temperature)
        assertEquals(0f, status?.displayStatus?.progress)
    }
}