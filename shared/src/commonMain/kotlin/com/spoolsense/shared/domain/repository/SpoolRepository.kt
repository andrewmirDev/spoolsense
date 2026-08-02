package com.spoolsense.shared.domain.repository

import com.spoolsense.shared.domain.model.Spool
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SpoolRepository {
    fun observeAllSpools(): Flow<List<Spool>>
    suspend fun getSpoolById(id: String): Spool?
    suspend fun insertSpool(spool: Spool)
    suspend fun updateRemainingWeight(id: String, newWeightGrams: Int)
}