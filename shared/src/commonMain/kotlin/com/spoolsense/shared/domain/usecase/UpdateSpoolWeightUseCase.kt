package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.repository.SpoolRepository

class UpdateSpoolWeightUseCase(private val repository: SpoolRepository) {

    class InvalidWeightException(message: String) : Exception(message)

    suspend operator fun invoke(id: String, newWeight: Int): Result<Unit> {
        val spool = repository.getSpoolById(id)
            ?: return Result.failure(InvalidWeightException("Spool $id not found"))
        if (newWeight < 0) {
            return Result.failure(InvalidWeightException("New weight cannot be negative"))
        }
        if (newWeight > spool.totalWeightGrams) {
            return Result.failure(InvalidWeightException("New weight exceeds total weight"))
        }
        return try {
            repository.updateRemainingWeight(id, newWeight)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
