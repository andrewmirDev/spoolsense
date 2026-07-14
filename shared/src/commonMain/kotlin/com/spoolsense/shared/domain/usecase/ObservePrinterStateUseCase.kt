package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.model.PrinterState
import com.spoolsense.shared.domain.repository.PrinterRepository
import kotlinx.coroutines.flow.Flow

class ObservePrinterStateUseCase(private val printerRepository: PrinterRepository) {
    operator fun invoke(): Flow<PrinterState> {
        return printerRepository.observablePrinterState()
    }
}