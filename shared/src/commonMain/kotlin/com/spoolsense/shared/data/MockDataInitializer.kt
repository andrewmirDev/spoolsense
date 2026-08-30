package com.spoolsense.shared.data

import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.repository.SpoolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object MockDataInitializer {

    private val mockSpools = listOf(
        Spool(
            id = "spool_001",
            name = "Creality PLA Orange",
            vendor = "Creality",
            material = "PLA",
            totalWeightGrams = 1000,
            remainingWeightGrams = 850,
            colorHex = "FF6B35",
            colorName = "Orange"
        ),
        Spool(
            id = "spool_002",
            name = "eSUN ABS Black",
            vendor = "eSUN",
            material = "ABS",
            totalWeightGrams = 1000,
            remainingWeightGrams = 450,
            colorHex = "000000",
            colorName = "Black"
        ),
        Spool(
            id = "spool_003",
            name = "Prusament PETG Red",
            vendor = "Prusa",
            material = "PETG",
            totalWeightGrams = 500,
            remainingWeightGrams = 120,
            colorHex = "FF0000",
            colorName = "Red"
        ),
        Spool(
            id = "spool_004",
            name = "MatterHackers Blue",
            vendor = "MatterHackers",
            material = "PLA",
            totalWeightGrams = 750,
            remainingWeightGrams = 750,
            colorHex = "0066FF",
            colorName = "Blue"
        ),
        Spool(
            id = "spool_005",
            name = "Overture White",
            vendor = "Overture",
            material = "PLA",
            totalWeightGrams = 1000,
            remainingWeightGrams = 200,
            colorHex = "FFFFFF",
            colorName = "White"
        )
    )

    fun initializeIfEmpty(
        repository: SpoolRepository,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                if (repository.getSpoolById("spool_001") == null) {
                    mockSpools.forEach { spool ->
                        repository.insertSpool(spool)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
