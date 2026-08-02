package com.spoolsense.shared.presentation.insert

sealed interface InsertSpoolIntent {
    data class UpdateVendor(val value: String) : InsertSpoolIntent
    data class UpdateMaterial(val value: String) : InsertSpoolIntent
    data class UpdateColorName(val value: String) : InsertSpoolIntent
    data class UpdateColorHex(val value: String) : InsertSpoolIntent
    data class UpdateTotalWeight(val value: String) : InsertSpoolIntent
    data class UpdateName(val value: String) : InsertSpoolIntent
    object Submit : InsertSpoolIntent
    object ClearForm : InsertSpoolIntent
}
