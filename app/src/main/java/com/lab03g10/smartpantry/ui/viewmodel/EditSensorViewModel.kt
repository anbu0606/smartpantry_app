package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * @(#) EditSensorViewModel.kt 1.0 2026-05-18
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Backs `EditSensorScreen`. Reads `sensorId` from the nav back-stack
 * (`SavedStateHandle["sensorId"]`), pre-fills the form from
 * `SensorRepository.observeConfigById`, and on save() persists the
 * mutated fields via `upsertConfig` so the next SensorTickWorker tick
 * picks up the new threshold immediately. Fixes BUG-004 (see
 * docs/bug-log.md).
 */
data class EditSensorUiState(
    val sensorId: String = "",
    val displayName: String = "",
    val environmentType: EnvironmentType = EnvironmentType.REFRIGERATOR,
    val thresholdC: Float = 0f,
    val maxLux: Float = 0f,
    val deviceId: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
) {
    val isValid: Boolean = displayName.isNotBlank()
}

@HiltViewModel
class EditSensorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    private val sensorId: String = savedStateHandle["sensorId"] ?: ""
    private val _state = MutableStateFlow(EditSensorUiState(sensorId = sensorId))
    val state: StateFlow<EditSensorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // One-shot read on entry — the form holds its own mutable copy of
            // the values, so we don't want subsequent flow emissions to clobber
            // user edits in progress.
            sensorRepository.observeConfigById(sensorId).firstOrNull()?.let { config ->
                _state.update {
                    it.copy(
                        displayName     = config.displayName,
                        // valueOf throws on unknown strings — guard for legacy/seed mismatch.
                        environmentType = runCatching {
                            EnvironmentType.valueOf(config.environmentType)
                        }.getOrDefault(EnvironmentType.CUSTOM),
                        thresholdC      = config.thresholdC,
                        maxLux          = config.maxLux,
                        deviceId        = config.deviceId,
                        isLoading       = false,
                    )
                }
            } ?: run {
                // Sensor row missing (e.g. soft-deleted from another screen) —
                // still flip isLoading so the UI doesn't hang on a spinner.
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDisplayNameChange(value: String) = _state.update { it.copy(displayName = value) }
    fun onEnvironmentChange(value: EnvironmentType) = _state.update {
        // Match AddSensor behaviour: switching env type seeds the suggested
        // thresholds. User can still slide them off afterwards.
        it.copy(
            environmentType = value,
            thresholdC      = value.suggestedThresholdC,
            maxLux          = value.suggestedMaxLux,
        )
    }
    fun onThresholdChange(value: Float) = _state.update { it.copy(thresholdC = value) }
    fun onMaxLuxChange(value: Float) = _state.update { it.copy(maxLux = value) }

    fun save() {
        val s = _state.value
        if (!s.isValid || s.isLoading || s.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            // Re-fetch existing on save so id / deviceId / csvAssetName /
            // pairedAt / active survive the mutation untouched.
            sensorRepository.observeConfigById(sensorId).firstOrNull()?.let { existing ->
                sensorRepository.upsertConfig(
                    existing.copy(
                        displayName     = s.displayName.trim(),
                        environmentType = s.environmentType.name,
                        thresholdC      = s.thresholdC,
                        maxLux          = s.maxLux,
                    ),
                )
            }
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    /** Soft delete (DAO sets `active = 0`); historical readings are preserved. */
    fun delete() {
        viewModelScope.launch {
            sensorRepository.deleteConfig(sensorId)
            _state.update { it.copy(isDeleted = true) }
        }
    }
}
