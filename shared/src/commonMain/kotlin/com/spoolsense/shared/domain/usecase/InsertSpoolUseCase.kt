package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.repository.SpoolRepository

class InsertSpoolUseCase(private val repository: SpoolRepository) {
    suspend operator fun invoke(spool: Spool){
        repository.insertSpool(spool)
    }
}