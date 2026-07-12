package com.spoolsense.app.shared.domain.repository

import com.spoolsense.app.shared.domain.model.Spool
import kotlinx.coroutines.flow.Flow

interface SpoolRepository {
    fun observeAllSpools(): Flow<List<Spool>>
    suspend fun getSpoolById(id: String): Spool?
    suspend fun insertSpool(spool: Spool)
    suspend fun updateRemainingWeight(id: String, newWeightGrams: Int)
}