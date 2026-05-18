package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


/**
 * @(#) SensorDetailViewModel.kt 1.0 2026-05-18
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Backs `SensorDetailScreen`. Reads `sensorId` from the nav back-stack
 * (`SavedStateHandle["sensorId"]`) and combines config + 7-day readings +
 * filtered notification history into a single state stream.
 * Fixes BUG-003 (see docs/bug-log.md).
 */
data class SensorDetailUiState(
    val sensorConfig: SensorConfigEntity? = null,
    val recentReadings: List<SensorReadingEntity> = emptyList(),
    val alertHistory: List<NotificationEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SensorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sensorRepository: SensorRepository,
    notificationRepository: NotificationRepository,
) : ViewModel() {

    private val sensorId: String = savedStateHandle["sensorId"] ?: ""
    // 7-day window — snapshotted once at VM init so the chart bounds stay
    // stable while the user has the screen open; a new instance is created
    // on each navigation entry so the window slides naturally.
    private val sevenDaysAgo: Long = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

    val state: StateFlow<SensorDetailUiState> = combine(
        sensorRepository.observeConfigById(sensorId),
        sensorRepository.observeReadingsForSensorSince(sensorId, sevenDaysAgo),
        notificationRepository.observeAll(),
    ) { config, readings, allNotifs ->
        SensorDetailUiState(
            sensorConfig   = config,
            recentReadings = readings,
            // Notification table is global; filter to the deep-link target id.
            alertHistory   = allNotifs.filter { it.relatedSensorId == sensorId },
            isLoading      = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SensorDetailUiState())
}
