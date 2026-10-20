package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.domain.repo.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * @(#) NotificationsViewModel.kt 1.0 2026-05-18
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Backs `NotificationsScreen`, replacing the A2-prototype hardcoded
 * notification lists with live rows from `NotificationRepository`.
 * Fixes BUG-006 (see docs/bug-log.md).
 */
data class NotificationsUiState(
    val today: List<NotificationEntity> = emptyList(),
    val earlier: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    val state: StateFlow<NotificationsUiState> = notificationRepository.observeAll()
        .map { all ->
            // Snapshot start-of-today per emission so the cutoff slides if the
            // user keeps the screen open past midnight.
            val startOfToday = startOfTodayLocalMillis()
            NotificationsUiState(
                today       = all.filter { it.createdAt >= startOfToday },
                earlier     = all.filter { it.createdAt < startOfToday },
                unreadCount = all.count { !it.read },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    fun markAllRead() {
        viewModelScope.launch {
            val current = state.value
            (current.today + current.earlier)
                .filter { !it.read }
                .forEach { notificationRepository.markRead(it.id) }
        }
    }

    fun markOneRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markRead(id)
        }
    }
}

/**
 * Midnight today in the device-local timezone. Local — not UTC — so the
 * "Today" section header matches the user's wall-clock day. Matches the
 * project-wide §10.2 timezone policy.
 */
private fun startOfTodayLocalMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
