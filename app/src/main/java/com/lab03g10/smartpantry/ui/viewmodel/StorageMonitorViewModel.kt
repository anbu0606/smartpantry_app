package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn


/**
 * @(#) StorageMonitorViewModel.kt 1.0 2026-05-18
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Backs `StorageMonitorScreen`, replacing the A2-prototype hardcoded
 * `LocationConfig` list with live config + reading flows from Room.
 */

/** UI-model row — one card per active sensor. Joins config + latest reading
 *  + recent 24 h history so the screen can render the temperature card AND
 *  the historical line chart without further DB queries. */
data class LocationLiveData(
    val sensorId: String,
    val displayName: String,
    val environmentType: EnvironmentType,
    val latestReading: SensorReadingEntity?,
    val thresholdC: Float,
    val humidityThreshold: Float,
    val maxLux: Float,
    val recentReadings: List<SensorReadingEntity>,
)

data class StorageMonitorUiState(
    val locations: List<LocationLiveData> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StorageMonitorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    val state: StateFlow<StorageMonitorUiState> = sensorRepository.observeConfigs()
        .flatMapLatest { allConfigs ->
            val configs = allConfigs.filter { it.active }
            if (configs.isEmpty()) {
                flowOf(StorageMonitorUiState(locations = emptyList()))
            } else {
                // 24 h sliding window for the historical chart. Snapshot the
                // cutoff once per config-change emission — readings inside the
                // window keep streaming via the inner flows.
                val since = System.currentTimeMillis() - 24L * 60 * 60 * 1000
                val recentFlows = configs.map { config ->
                    sensorRepository.observeReadingsForSensorSince(config.id, since)
                }
                combine(
                    sensorRepository.observeLatestPerSensor(),
                    combine(recentFlows) { arr -> arr.toList() },
                ) { latest, recentPerConfig ->
                    val latestById = latest.associateBy { it.sensorId }
                    StorageMonitorUiState(
                        locations = configs.mapIndexed { index, config ->
                            LocationLiveData(
                                sensorId        = config.id,
                                displayName     = config.displayName,
                                // valueOf throws on unknown strings — guard for legacy/seed mismatch
                                environmentType = runCatching {
                                    EnvironmentType.valueOf(config.environmentType)
                                }.getOrDefault(EnvironmentType.CUSTOM),
                                latestReading     = latestById[config.id],
                                thresholdC        = config.thresholdC,
                                humidityThreshold = config.humidityThreshold,
                                maxLux            = config.maxLux,
                                recentReadings    = recentPerConfig[index],
                            )
                        },
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageMonitorUiState())
}
