package com.spoolsense.app.shared.data.mapper

import com.spoolsense.app.shared.domain.model.Spool
import com.spoolsense.shared.database.SpoolEntity

fun SpoolEntity.toDomain(): Spool {
    return Spool(
        id = id,
        name = name,
        vendor = vendor,
        material = material,
        totalWeightGrams = totalWeightGrams.toInt(),
        remainingWeightGrams = remainingWeightGrams.toInt(),
        colorHex = colorHex
    )
}

fun Spool.toEntity(): SpoolEntity {
    return SpoolEntity(
        id = id,
        name = name,
        vendor = vendor,
        material = material,
        totalWeightGrams = totalWeightGrams.toLong(),
        remainingWeightGrams = remainingWeightGrams.toLong(),
        colorHex = colorHex
    )
}