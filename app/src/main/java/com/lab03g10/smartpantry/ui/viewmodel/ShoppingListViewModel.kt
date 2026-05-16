package com.lab03g10.smartpantry.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.repo.ShoppingRepository
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * This class stores one row shown in the Shopping List screen.
 */
data class ShoppingListRowUi(
    val id: String,
    val name: String,
    val subtitle: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
    val autoSuggested: Boolean,
)

/**
 * This class stores all data needed by the Shopping List screen.
 */
data class ShoppingListUiState(
    val items: List<ShoppingListRowUi> = emptyList(),
    val suggestedItems: List<ShoppingListRowUi> = emptyList(),
    val checkedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * This ViewModel connects ShoppingRepository and WasteRepository to the UI.
 */
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val wasteRepository: WasteRepository,
) : ViewModel() {

    // This stores the current UI state
    var uiState by mutableStateOf(ShoppingListUiState())
        private set

    // Stores shopping items loaded from Room database
    private var savedShoppingItems: List<ShoppingItemEntity> = emptyList()

    // Stores waste history loaded from Room database
    private var wasteLogItems: List<WasteLogEntity> = emptyList()

    // Load shopping list and waste history when ViewModel starts
    init {
        loadShoppingItems()
        loadWasteHistory()
    }

    /**
     * This function loads saved shopping items from Room database.
     */
    private fun loadShoppingItems() {
        viewModelScope.launch {
            try {

                // Observe shopping list changes
                shoppingRepository.observeAll().collect { shoppingItems ->

                    // Save items locally
                    savedShoppingItems = shoppingItems

                    // Update UI state
                    updateScreenState()
                }
            } catch (exception: Exception) {

                // Show error if loading fails
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load shopping list",
                )
            }
        }
    }

    /**
     * This function loads waste history from Room database.
     *
     * Waste history is used to create shopping suggestions.
     */
    private fun loadWasteHistory() {
        viewModelScope.launch {
            try {

                // Observe waste history changes
                wasteRepository.observeAll().collect { wasteItems ->

                    // Save waste items locally
                    wasteLogItems = wasteItems

                    // Update UI state
                    updateScreenState()
                }
            } catch (exception: Exception) {

                // Show error if loading fails
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load waste history",
                )
            }
        }
    }

    /**
     * This function changes checked state when the user taps an item.
     */
    fun onToggleItem(id: String) {
        viewModelScope.launch {

            // Toggle checked value in database
            shoppingRepository.toggle(id)
        }
    }

    /**
     * This function removes all checked items from the shopping list.
     */
    fun onDeleteCheckedItems() {
        viewModelScope.launch {

            // Loop through all shopping items
            for (item in savedShoppingItems) {

                // Delete only checked items
                if (item.checked) {
                    shoppingRepository.delete(item.id)
                }
            }
        }
    }

    /**
     * This function adds a suggested item into the real shopping list.
     */
    fun onAddSuggestedItem(row: ShoppingListRowUi) {
        viewModelScope.launch {

            // Create new shopping item
            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                name = row.name,
                quantity = row.quantity,
                category = row.category,
                checked = false,
                addedAt = System.currentTimeMillis(),
                autoSuggested = true,
            )

            // Save item into Room database
            shoppingRepository.upsert(item)
        }
    }

    /**
     * This function prepares all screen data in one place.
     */
    private fun updateScreenState() {

        // Store shopping rows for the UI
        val shoppingRows = mutableListOf<ShoppingListRowUi>()

        // Convert Room entities into UI rows
        for (item in savedShoppingItems) {
            shoppingRows.add(item.toShoppingListRowUi())
        }

        // Create suggested shopping items
        val suggestedRows = createSuggestedRows()

        // Count checked shopping items
        var checkedCount = 0

        for (item in savedShoppingItems) {
            if (item.checked) {
                checkedCount += 1
            }
        }

        // Update screen state
        uiState = ShoppingListUiState(
            items = shoppingRows,
            suggestedItems = suggestedRows,
            checkedCount = checkedCount,
            totalCount = shoppingRows.size,
            isLoading = false,
            errorMessage = null,
        )
    }

    /**
     * This function converts database shopping item into UI row data.
     */
    private fun ShoppingItemEntity.toShoppingListRowUi(): ShoppingListRowUi {
        return ShoppingListRowUi(
            id = id,
            name = name,
            subtitle = category,
            quantity = quantity,
            category = category,
            checked = checked,
            autoSuggested = autoSuggested,
        )
    }

    /**
     * This function creates shopping suggestions from waste history.
     */
    private fun createSuggestedRows(): List<ShoppingListRowUi> {

        // Store category count values
        val categoryCounts = mutableMapOf<String, Int>()

        // Count how many times each category appears in waste history
        for (waste in wasteLogItems) {

            val oldCount = categoryCounts[waste.category] ?: 0

            categoryCounts[waste.category] = oldCount + 1
        }

        // Store suggested shopping rows
        val suggestedRows = mutableListOf<ShoppingListRowUi>()

        // Loop through each category count
        for (entry in categoryCounts.entries) {

            val category = entry.key
            val count = entry.value

            // Suggest category if wasted 3 or more times
            if (count >= 3 && !alreadyHasCategory(category)) {

                suggestedRows.add(
                    ShoppingListRowUi(
                        id = "suggestion_$category",
                        name = "Restock $category",
                        subtitle = "Suggested from $count waste records",
                        quantity = "x1",
                        category = category,
                        checked = false,
                        autoSuggested = true,
                    )
                )
            }
        }

        return suggestedRows
    }

    /**
     * This function checks whether the shopping list already has this category.
     */
    private fun alreadyHasCategory(category: String): Boolean {

        // Loop through shopping items
        for (item in savedShoppingItems) {

            // Return true if category already exists
            if (item.category == category) {
                return true
            }
        }

        return false
    }
}