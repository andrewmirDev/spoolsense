package com.spoolsense.shared.domain.model

data class Spool(
    val id: String,
    val name: String = "",
    val vendor: String,
    val material: String = "PLA",
    val totalWeightGrams: Int = 1000,
    val remainingWeightGrams: Int = 1000,
    val colorHex: String,
    val colorName: String = "Orange"
)