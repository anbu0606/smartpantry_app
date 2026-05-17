package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * @(#) AddSensorViewModel.kt 1.0 2026-05-17
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
data class AddSensorUiState(
    val displayName: String = "",
    val environmentType: EnvironmentType = EnvironmentType.REFRIGERATOR,
    val deviceId: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val isValid: Boolean = displayName.isNotBlank() && deviceId.isNotBlank()
}

@HiltViewModel
class AddSensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddSensorUiState())
    val state: StateFlow<AddSensorUiState> = _state.asStateFlow()

    fun onDisplayNameChange(value: String) = _state.update { it.copy(displayName = value) }
    fun onEnvironmentChange(value: EnvironmentType) = _state.update { it.copy(environmentType = value) }
    fun onDeviceIdChange(value: String) = _state.update { it.copy(deviceId = value) }

    fun save() {
        val s = _state.value
        if (!s.isValid) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            sensorRepository.upsertConfig(
                SensorConfigEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = s.displayName.trim(),
                    deviceId = s.deviceId.trim(),
                    environmentType = s.environmentType.name,
                    thresholdC = s.environmentType.suggestedThresholdC,
                    maxLux = s.environmentType.suggestedMaxLux,
                    csvAssetName = when (s.environmentType) {
                        EnvironmentType.REFRIGERATOR -> "fridge.csv"
                        EnvironmentType.FREEZER -> "freezer.csv"
                        else -> "pantry.csv"
                    },
                    pairedAt = System.currentTimeMillis(),
                    active = true,
                )
            )
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}