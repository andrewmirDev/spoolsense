package com.spoolsense.shared.domain.usecase

import com.spoolsense.app.shared.domain.repository.SpoolRepository

class UpdateSpoolWeightUseCase(private val repository: SpoolRepository) {
    suspend operator fun invoke(id: String, netWeight: Int){
        repository.updateRemainingWeight(id, netWeight)
    }
}