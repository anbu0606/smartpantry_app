package com.lab03g10.smartpantry.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// This class stores one row that will be shown in the Waste Log screen
data class WasteLogRowUi(
    val id: String,
    val foodName: String,
    val category: String,
    val quantity: String,
    val reason: String,
    val dateLabel: String,
    val monthGroup: String,
    val co2eGrams: Float,
    val co2eLabel: String
)

// This class stores all the data needed by the Waste Log screen
data class WasteLogUiState(
    val rows: List<WasteLogRowUi> = emptyList(),
    val groupedRows: Map<String, List<WasteLogRowUi>> = emptyMap(),
    val groupOrder: List<String> = emptyList(),
    val selectedFilter: String = "All",
    val wastedCount: Int = 0,
    val totalCo2eGrams: Float = 0f,
    val totalCo2eLabel: String = "0 g",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// This ViewModel is used for the Waste Log screen
@HiltViewModel
class WasteLogViewModel @Inject constructor(
    private val wasteRepository: WasteRepository
) : ViewModel() {

    // This keeps the current screen state
    var uiState by mutableStateOf(WasteLogUiState())
        private set

    // This stores all waste rows before applying any filter
    private var allRows: List<WasteLogRowUi> = emptyList()

    // Load waste log data when the ViewModel starts
    init {
        loadWasteLogs()
    }

    // This function gets waste log data from the repository
    private fun loadWasteLogs() {
        viewModelScope.launch {
            try {
                wasteRepository.observeAll().collect { wasteList ->

                    // Convert database data into UI row data
                    val rowList = mutableListOf<WasteLogRowUi>()

                    for (waste in wasteList) {
                        val row = waste.toWasteLogRowUi()
                        rowList.add(row)
                    }

                    // Save all rows
                    allRows = rowList

                    // Update the screen state
                    updateScreenState(
                        rows = allRows,
                        selectedFilter = uiState.selectedFilter
                    )
                }
            } catch (exception: Exception) {
                // Show error message if data cannot be loaded
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load waste log"
                )
            }
        }
    }

    // This function is called when the user selects a filter
    fun onFilterSelected(filter: String) {
        updateScreenState(
            rows = allRows,
            selectedFilter = filter
        )
    }

    // This function updates the screen data after filtering and grouping
    private fun updateScreenState(
        rows: List<WasteLogRowUi>,
        selectedFilter: String
    ) {
        val filteredRows = mutableListOf<WasteLogRowUi>()

        // Apply filter based on the selected reason
        for (row in rows) {
            if (selectedFilter == "All" || row.reason == selectedFilter) {
                filteredRows.add(row)
            }
        }

        // Group rows by month, for example MAY 2026
        val groupedRows = filteredRows.groupBy { row ->
            row.monthGroup
        }

        // Store month group names
        val groupOrder = groupedRows.keys.toList()

        // Calculate total CO2e value
        var totalCo2e = 0f

        for (row in rows) {
            totalCo2e += row.co2eGrams
        }

        // Update the UI state with new values
        uiState = WasteLogUiState(
            rows = filteredRows,
            groupedRows = groupedRows,
            groupOrder = groupOrder,
            selectedFilter = selectedFilter,
            wastedCount = rows.size,
            totalCo2eGrams = totalCo2e,
            totalCo2eLabel = formatCo2e(totalCo2e),
            isLoading = false,
            errorMessage = null
        )
    }

    // This function converts database entity into screen row data
    private fun WasteLogEntity.toWasteLogRowUi(): WasteLogRowUi {
        return WasteLogRowUi(
            id = id,
            foodName = name,
            category = category,
            quantity = quantity,
            reason = formatReason(reason),
            dateLabel = formatDate(wastedAt),
            monthGroup = formatMonthGroup(wastedAt),
            co2eGrams = co2eGrams,
            co2eLabel = formatCo2e(co2eGrams)
        )
    }

    // This function formats the waste reason text
    private fun formatReason(reason: String): String {
        return when (reason.uppercase(Locale.ENGLISH)) {
            "EXPIRED" -> "Expired"
            "SPOILED" -> "Spoiled"
            else -> "Other"
        }
    }

    // This function formats date like 13 May 2026
    private fun formatDate(timeMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return formatter.format(Date(timeMillis))
    }

    // This function formats month group like MAY 2026
    private fun formatMonthGroup(timeMillis: Long): String {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        return formatter.format(Date(timeMillis)).uppercase(Locale.ENGLISH)
    }

    // This function formats CO2e value into grams or kg
    private fun formatCo2e(co2eGrams: Float): String {
        if (co2eGrams >= 1000f) {
            val kg = co2eGrams / 1000f
            return "%.1f kg".format(kg)
        }

        return "%.0f g".format(co2eGrams)
    }
}