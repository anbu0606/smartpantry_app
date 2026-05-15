package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.repo.AuthRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val currentUser = authRepository.observeCurrentUser().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val isDarkMode: StateFlow<Boolean> = preferenceRepository.observeIsDarkMode().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val notifyDaysBefore: StateFlow<Int> = preferenceRepository.observeNotifyDaysBefore().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        3
    )

    val quietHoursStart: StateFlow<Int> = preferenceRepository.observeQuietHoursStart().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        22
    )

    val quietHoursEnd: StateFlow<Int> = preferenceRepository.observeQuietHoursEnd().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        8
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setDarkMode(enabled)
        }
    }

    fun setNotifyDaysBefore(days: Int) {
        viewModelScope.launch {
            preferenceRepository.setNotifyDaysBefore(days)
        }
    }

    val userName: StateFlow<String> = preferenceRepository.observeUserName().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    fun setQuietHours(start: Int, end: Int) {
        viewModelScope.launch {
            preferenceRepository.setQuietHoursStart(start)
            preferenceRepository.setQuietHoursEnd(end)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { preferenceRepository.setUserName(name) }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
