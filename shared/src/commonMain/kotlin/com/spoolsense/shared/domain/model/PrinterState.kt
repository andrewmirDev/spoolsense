package com.spoolsense.shared.domain.model

data class PrinterState(
    val status: PrinterStatus = PrinterStatus.Disconnected,
    val nozzleTemperature: Float = 0f,
    val nozzleTarget: Float = 0f,
    val bedTemperature: Float = 0f,
    val betTarget: Float = 0f,
    val progress: Float = 0f,
    val stateMessage: String = ""
)

sealed interface PrinterStatus{
    object Printing: PrinterStatus
    object Idle: PrinterStatus
    object Paused: PrinterStatus
    object Error: PrinterStatus
    object Disconnected: PrinterStatus
}