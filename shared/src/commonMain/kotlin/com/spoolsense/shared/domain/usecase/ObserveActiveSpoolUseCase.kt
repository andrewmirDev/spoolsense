package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.repository.ActiveSpoolRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveSpoolUseCase(
    private val repository: ActiveSpoolRepository
) {
    operator fun invoke(): Flow<String?> = repository.observeActiveSpoolId()
}
