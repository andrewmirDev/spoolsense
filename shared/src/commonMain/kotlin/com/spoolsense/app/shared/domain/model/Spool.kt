package com.spoolsense.app.shared.domain.model

data class Spool(
    val id: String,
    val name: String,
    val vendor: String,
    val material: String,
    val totalWeightGrams: Int,
    val remainingWeightGrams: Int,
    val colorHex: String
)