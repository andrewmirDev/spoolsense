package com.spoolsense.shared.domain.usecase

import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.app.shared.domain.repository.SpoolRepository
import kotlinx.coroutines.flow.Flow

class ObserveSpoolsUseCase (private val repository: SpoolRepository) {

    operator fun invoke(): Flow<List<Spool>>{
        return repository.observeAllSpools()
    }
}