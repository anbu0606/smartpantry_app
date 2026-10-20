package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.model.BarcodeProduct
import com.lab03g10.smartpantry.domain.usecase.LookupBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Classifies a barcode-lookup failure so the screen can pick the right title
 * and supporting copy without parsing exception messages itself.
 */
enum class BarcodeErrorKind {
    /** OFF returned status=0 (no entry), or the API returned HTTP 404. */
    NOT_IN_DATABASE,
    /** IOException / SocketTimeoutException / 5xx — phone has no path to the server. */
    NETWORK,
    /** User-supplied barcode failed local validation (length / characters). */
    INVALID,
    /** Anything we don't recognise — keep the raw message visible for debugging. */
    UNKNOWN,
}

data class BarcodeScanResultUiState(
    val barcode: String = "",
    val isLoading: Boolean = false,
    val product: BarcodeProduct? = null,
    val errorMessage: String? = null,
    val errorKind: BarcodeErrorKind? = null,
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
                it.copy(isLoading = true, errorMessage = null, errorKind = null)
            }
            lookupBarcodeUseCase(barcode)
                .onSuccess { product ->
                    _state.update {
                        it.copy(isLoading = false, product = product, errorMessage = null, errorKind = null)
                    }
                }
                .onFailure { error ->
                    val (kind, copy) = error.toUiCopy()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = null,
                            errorMessage = copy,
                            errorKind = kind,
                        )
                    }
                }
        }
    }
}

/**
 * Map a lookup failure to (kind, user-facing copy). Centralised here so the
 * screen never inspects exception types and `error.message` strings never
 * leak into the UI verbatim.
 */
private fun Throwable.toUiCopy(): Pair<BarcodeErrorKind, String> = when (this) {
    is IllegalArgumentException ->
        BarcodeErrorKind.INVALID to (message ?: "Please enter a valid barcode.")
    is HttpException -> if (code() == 404) {
        BarcodeErrorKind.NOT_IN_DATABASE to "Product not in database. Add details manually."
    } else {
        BarcodeErrorKind.NETWORK to "Couldn't reach Open Food Facts (${code()}). Pull to retry."
    }
    is IOException ->
        // IOException covers SocketTimeoutException, UnknownHostException, etc.
        BarcodeErrorKind.NETWORK to "No internet. Pull to retry."
    is IllegalStateException ->
        // Thrown by LookupBarcodeUseCase when OFF returns status=0 (no entry).
        BarcodeErrorKind.NOT_IN_DATABASE to "Product not in database. Add details manually."
    else ->
        BarcodeErrorKind.UNKNOWN to (message ?: "Could not load product details.")
}
