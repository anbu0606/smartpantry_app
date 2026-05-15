package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferenceRepository.observeIsDarkMode().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val isOnboardingCompleted: StateFlow<Boolean> = preferenceRepository.observeOnboardingCompleted().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
}
