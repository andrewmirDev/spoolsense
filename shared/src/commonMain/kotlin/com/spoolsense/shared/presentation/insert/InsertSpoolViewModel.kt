package com.spoolsense.shared.presentation.insert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolsense.shared.domain.model.Spool
import com.spoolsense.shared.domain.usecase.InsertSpoolUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class InsertSpoolViewModel(private val insertSpoolUseCase: InsertSpoolUseCase) : ViewModel() {

    private val _state = MutableStateFlow(InsertSpoolState())
    val state: StateFlow<InsertSpoolState> = _state.asStateFlow()

    fun handleIntent(intent: InsertSpoolIntent) {
        when (intent) {
            is InsertSpoolIntent.UpdateVendor -> _state.update { it.copy(vendor = intent.value) }
            is InsertSpoolIntent.UpdateMaterial -> _state.update { it.copy(material = intent.value) }
            is InsertSpoolIntent.UpdateColorName -> _state.update { it.copy(colorName = intent.value) }
            is InsertSpoolIntent.UpdateColorHex -> _state.update { it.copy(colorHex = intent.value.take(6)) }
            is InsertSpoolIntent.UpdateTotalWeight -> _state.update { it.copy(totalWeightGrams = intent.value) }
            is InsertSpoolIntent.UpdateName -> _state.update { it.copy(name = intent.value) }
            InsertSpoolIntent.Submit -> submitForm()
            InsertSpoolIntent.ClearForm -> clearForm()
        }
    }

    private fun submitForm() {
        val currentState = _state.value
        val errors = validateForm(currentState)

        if (errors.isNotEmpty()) {
            _state.update { it.copy(formErrors = errors) }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, formErrors = emptyMap()) }

        viewModelScope.launch {
            try {
                val newSpool = Spool(
                    id = generateSpoolId(),
                    name = currentState.name,
                    vendor = currentState.vendor,
                    material = currentState.material,
                    colorName = currentState.colorName,
                    colorHex = currentState.colorHex,
                    totalWeightGrams = currentState.totalWeightGrams.toIntOrNull() ?: 1000,
                    remainingWeightGrams = currentState.totalWeightGrams.toIntOrNull() ?: 1000
                )

                insertSpoolUseCase(newSpool)

                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Spool added successfully!",
                        name = "",
                        vendor = "",
                        material = "PLA",
                        colorName = "",
                        colorHex = "FF6B35",
                        totalWeightGrams = "1000"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to add spool"
                    )
                }
            }
        }
    }

    private fun validateForm(state: InsertSpoolState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (state.vendor.isBlank()) {
            errors["vendor"] = "Vendor is required"
        }

        if (state.material.isBlank()) {
            errors["material"] = "Material is required"
        }

        if (state.colorName.isBlank()) {
            errors["colorName"] = "Color name is required"
        }

        if (state.colorHex.isBlank() || state.colorHex.length != 6) {
            errors["colorHex"] = "Valid hex color required (6 digits)"
        }

        state.totalWeightGrams.toIntOrNull()?.let { weight ->
            if (weight <= 0) {
                errors["totalWeightGrams"] = "Weight must be greater than 0"
            }
        } ?: run {
            errors["totalWeightGrams"] = "Valid weight required"
        }

        return errors
    }

    private fun clearForm() {
        _state.update {
            InsertSpoolState(
                colorHex = "FF6B35"
            )
        }
    }

    private fun generateSpoolId(): String {
        return "spool_${Clock.System}_${Random.nextInt(1000, 9999)}"
    }
}
