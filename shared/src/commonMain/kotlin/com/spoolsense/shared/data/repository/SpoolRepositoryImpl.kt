package com.spoolsense.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.spoolsense.shared.data.mapper.toDomain
import com.spoolsense.shared.data.mapper.toEntity
import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.repository.SpoolRepository
import com.spoolsense.shared.database.SpoolDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpoolRepositoryImpl (
    database: SpoolDatabase
) : SpoolRepository {

    private val queries = database.spoolDatabaseQueries

    override fun observeAllSpools(): Flow<List<Spool>> {
        return queries.selectAllSpools()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getSpoolById(id: String): Spool? {
        return queries.selectSpoolById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertSpool(spool: Spool) {
        val entity = spool.toEntity()
        queries.insertSpool(
            id = entity.id,
            name = entity.name,
            vendor = entity.vendor,
            material = entity.material,
            totalWeightGrams = entity.totalWeightGrams,
            remainingWeightGrams = entity.remainingWeightGrams,
            colorHex = entity.colorHex,
            colorName = entity.colorName
        )
    }

    override suspend fun updateRemainingWeight(id: String, newWeightGrams: Int) {
        queries.updateSpoolRemainingWeight(
            newWeight = newWeightGrams.toLong(),
            id = id
        )
    }

    override suspend fun printJobExists(id: String): Boolean {
        return queries.selectPrintJobById(id).executeAsOneOrNull() != null
    }

    override suspend fun insertPrintJob(
        id: String,
        fileName: String,
        spoolId: String,
        weightUsedGrams: Int,
        timestamp: Long,
        status: String
    ) {
        queries.insertPrintJob(
            id = id,
            fileName = fileName,
            spoolId = spoolId,
            weightUsedGrams = weightUsedGrams.toLong(),
            timestamp = timestamp,
            status = status
        )
    }
}