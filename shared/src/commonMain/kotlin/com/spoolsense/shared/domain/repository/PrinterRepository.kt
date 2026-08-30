package com.spoolsense.shared.domain.repository

import com.spoolsense.shared.domain.model.PrinterSettings
import com.spoolsense.shared.domain.model.PrinterState
import kotlinx.coroutines.flow.Flow

interface PrinterRepository {
    fun observablePrinterState(): Flow<PrinterState>

    suspend fun testConnection(settings: PrinterSettings): ConnectionTestResult
}

sealed interface ConnectionTestResult {
    data class Success(val serverInfo: String) : ConnectionTestResult
    data class Failure(val reason: String) : ConnectionTestResult
}