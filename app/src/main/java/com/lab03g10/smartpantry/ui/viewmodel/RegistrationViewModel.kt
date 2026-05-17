package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun signUp(email: String, name: String, password: String, onRegistrationSuccess: () -> Unit) {
        if (name.isBlank()) {
            _error.value = "Name cannot be empty"
            return
        }
        if (!isEmailValid(email)) {
            _error.value = "Invalid email format"
            return
        }
        if (password.length < 8) {
            _error.value = "Password must be at least 8 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.signUp(email, name, password)
                .onSuccess {
                    onRegistrationSuccess()
                }
                .onFailure {
                    _error.value = it.message ?: "Registration failed"
                }
            _isLoading.value = false
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearError() {
        _error.value = null
    }
}
