package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.model.BarcodeProduct
import com.lab03g10.smartpantry.domain.usecase.LookupBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BarcodeScanResultUiState(
    val barcode: String = "",
    val isLoading: Boolean = false,
    val product: BarcodeProduct? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class BarcodeScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lookupBarcodeUseCase: LookupBarcodeUseCase,
) : ViewModel() {

    private val barcode: String = checkNotNull(savedStateHandle["barcode"])
    private val _state = MutableStateFlow(BarcodeScanResultUiState(barcode = barcode))
    val state: StateFlow<BarcodeScanResultUiState> = _state.asStateFlow()

    init {
        lookup()
    }

    fun lookup() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, errorMessage = null)
            }
            lookupBarcodeUseCase(barcode)
                .onSuccess { product ->
                    _state.update {
                        it.copy(isLoading = false, product = product, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = null,
                            errorMessage = error.message ?: "Could not load product details.",
                        )
                    }
                }
        }
    }
}
