package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.repository.SpoolRepository
import kotlin.math.roundToInt

class OnPrintCompletedUseCase(
    private val spoolRepository: SpoolRepository,
    private val updateSpoolWeightUseCase: UpdateSpoolWeightUseCase
) {

    class NoActiveSpoolException : Exception("No active spool selected")
    class ZeroFilamentException : Exception("Filament usage is zero")

    suspend operator fun invoke(
        activeSpoolId: String,
        fileName: String,
        filamentUsedMm: Double,
        timestamp: Long
    ): Result<Unit> {
        val jobId = "job_${timestamp}_${activeSpoolId}"
        if (spoolRepository.printJobExists(jobId)) {
            return Result.success(Unit)
        }

        val spool = spoolRepository.getSpoolById(activeSpoolId)
            ?: return Result.failure(NoActiveSpoolException())

        val usedGrams = ((filamentUsedMm / 1000.0) * GRAMS_PER_METER).roundToInt()
            .coerceAtLeast(0)
        if (usedGrams == 0) {
            return Result.failure(ZeroFilamentException())
        }

        val newWeight = (spool.remainingWeightGrams - usedGrams).coerceAtLeast(0)

        return try {
            updateSpoolWeightUseCase(activeSpoolId, newWeight)
                .onSuccess {
                    spoolRepository.insertPrintJob(
                        id = jobId,
                        fileName = fileName,
                        spoolId = activeSpoolId,
                        weightUsedGrams = usedGrams,
                        timestamp = timestamp,
                        status = "SUCCESS"
                    )
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val GRAMS_PER_METER = 2.98
    }
}
