package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch { preferenceRepository.setOnboardingCompleted(true) }
    }
}
