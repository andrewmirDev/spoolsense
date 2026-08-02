package com.spoolsense.shared.presentation.inventory

sealed interface InventorySpoolIntent {
    data class UpdateSearch(val value: String) : InventorySpoolIntent
    object Refresh : InventorySpoolIntent
}