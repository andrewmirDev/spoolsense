package com.spoolsense.shared.domain.repository

import com.spoolsense.shared.domain.model.PrinterState
import kotlinx.coroutines.flow.Flow

interface PrinterRepository {
    fun observablePrinterState(): Flow<PrinterState>
}