package com.spoolsense.shared.data.repository

import com.spoolsense.shared.data.network.dto.MoonrakerPrinterStateDto
import com.spoolsense.shared.domain.model.PrinterState
import com.spoolsense.shared.domain.model.PrinterStatus
import com.spoolsense.shared.domain.repository.PrinterRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class PrinterRepositoryImpl(private val httpClient: HttpClient) : PrinterRepository {
    override fun observablePrinterState(): Flow<PrinterState> = flow {
        while (true) {
            try {
                val response: MoonrakerPrinterStateDto = httpClient.get("").body()
                emit(mapToDomain(response))
            }catch (e:Exception){
                emit(PrinterState(status = PrinterStatus.Disconnected))
            }
            delay(2.seconds)
        }
    }
    private fun mapToDomain(dto: MoonrakerPrinterStateDto): PrinterState{
        return PrinterState(
            status =  when(dto.status?.printStats?.state){
                "printing" -> PrinterStatus.Printing
                "paused" -> PrinterStatus.Paused
                "standby" -> PrinterStatus.Idle
                else -> PrinterStatus.Idle
            },
            nozzleTemperature = dto.status?.extruder?.temperature ?: 0f,
            bedTemperature = dto.status?.heaterBed?.temperature ?: 0f,
            progress = dto.status?.printStats?.progress ?: 0f
        )
    }
}