package com.spoolsense.shared.domain.repository

import kotlinx.coroutines.flow.Flow

interface ActiveSpoolRepository {
    suspend fun getActiveSpoolId(): String?
    suspend fun setActiveSpoolId(id: String?)
    fun observeActiveSpoolId(): Flow<String?>
}
