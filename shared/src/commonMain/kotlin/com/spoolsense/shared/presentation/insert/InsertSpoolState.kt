package com.spoolsense.shared.presentation.insert

data class InsertSpoolState(
    val name: String = "",
    val vendor: String = "",
    val material: String = "PLA",
    val colorName: String = "",
    val colorHex: String = "FF6B35",
    val totalWeightGrams: String = "1000",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val formErrors: Map<String, String> = emptyMap()
)