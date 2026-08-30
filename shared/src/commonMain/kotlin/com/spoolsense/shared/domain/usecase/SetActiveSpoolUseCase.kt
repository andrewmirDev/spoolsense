package com.spoolsense.shared.domain.usecase

import com.spoolsense.shared.domain.repository.ActiveSpoolRepository

class SetActiveSpoolUseCase(
    private val repository: ActiveSpoolRepository
) {
    suspend operator fun invoke(spoolId: String?) {
        repository.setActiveSpoolId(spoolId)
    }
}
