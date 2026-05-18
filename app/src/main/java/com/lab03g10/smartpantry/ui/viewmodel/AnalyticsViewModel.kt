package com.lab03g10.smartpantry.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.domain.model.CategoryTotal
import com.lab03g10.smartpantry.domain.model.MonthlyTotal
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch


data class AnalyticsUiState(
    val monthlyTotals: List<MonthlyTotal> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val totalWastedItems: Int = 0,
    val totalCo2eGrams: Float = 0f,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val wasteRepository: WasteRepository
) : ViewModel() {

    var uiState by mutableStateOf(AnalyticsUiState())
        private set

    init {
        loadMonthlyTotals()
        loadCategoryTotals()
    }

    private fun loadMonthlyTotals() {
        viewModelScope.launch {
            wasteRepository.observeMonthlyTotals().collect { monthlyList ->

                var totalItems = 0

                for (month in monthlyList) {
                    totalItems += month.count
                }

                uiState = uiState.copy(
                    monthlyTotals = monthlyList,
                    totalWastedItems = totalItems
                )
            }
        }
    }

    private fun loadCategoryTotals() {
        viewModelScope.launch {
            wasteRepository.observeByCategory().collect { categoryList ->

                var totalCo2e = 0f

                for (category in categoryList) {
                    totalCo2e += category.co2eGramsTotal
                }

                uiState = uiState.copy(
                    categoryTotals = categoryList,
                    totalCo2eGrams = totalCo2e
                )
            }
        }
    }
}
