package com.lab03g10.smartpantry.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
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
 * This ViewModel connects ShoppingRepository, WasteRepository
 * and PreferenceRepository to the UI.
 */
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val wasteRepository: WasteRepository,
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    companion object {
        // A dismissed suggestion will stay hidden for 3 days
        private const val SUGGESTION_COOLDOWN_MS = 3L * 24L * 60L * 60L * 1000L
    }

    // This stores the current UI state
    var uiState by mutableStateOf(ShoppingListUiState())
        private set

    // Stores shopping items loaded from Room database
    private var savedShoppingItems: List<ShoppingItemEntity> = emptyList()

    // Stores waste history loaded from Room database
    private var wasteLogItems: List<WasteLogEntity> = emptyList()

    // Stores dismissed suggestion times loaded from DataStore
    private var dismissedSuggestions: Map<String, Long> = emptyMap()

    // Load shopping list, waste history and dismissed suggestions when ViewModel starts
    init {
        loadShoppingItems()
        loadWasteHistory()
        loadDismissedSuggestions()
    }

    /**
     * This function loads saved shopping items from Room database.
     */
    private fun loadShoppingItems() {
        viewModelScope.launch {
            try {
                shoppingRepository.observeAll().collect { shoppingItems ->
                    savedShoppingItems = shoppingItems
                    updateScreenState()
                }
            } catch (exception: Exception) {
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
                wasteRepository.observeAll().collect { wasteItems ->
                    wasteLogItems = wasteItems
                    updateScreenState()
                }
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load waste history",
                )
            }
        }
    }

    /**
     * This function loads dismissed suggestions from DataStore.
     *
     * This is used to stop the same suggestion from appearing again immediately.
     */
    private fun loadDismissedSuggestions() {
        viewModelScope.launch {
            try {
                preferenceRepository.observeDismissedSuggestions().collect { dismissedMap ->
                    dismissedSuggestions = dismissedMap
                    updateScreenState()
                }
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load suggestion preferences",
                )
            }
        }
    }

    /**
     * This function changes checked state when the user taps an item.
     */
    fun onToggleItem(id: String) {
        viewModelScope.launch {

            // Find the item before changing its checked state
            val item = findShoppingItemById(id)

            // Toggle checked value in database
            shoppingRepository.toggle(id)

            // If the user checks an auto-suggested item,
            // remember this exact item suggestion so it does not appear again immediately.
            if (item != null && item.autoSuggested && !item.checked) {
                val suggestionKey = getSuggestionKey(
                    name = item.name.removePrefix("Restock "),
                    category = item.category,
                )

                preferenceRepository.markSuggestionDismissed(suggestionKey)
            }
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

                    // If this item came from suggestions,
                    // save this exact item suggestion into DataStore before deleting.
                    if (item.autoSuggested) {
                        val suggestionKey = getSuggestionKey(
                            name = item.name.removePrefix("Restock "),
                            category = item.category,
                        )

                        preferenceRepository.markSuggestionDismissed(suggestionKey)
                    }

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

            /*
             * Do not save this as dismissed here.
             *
             * Add only moves the suggestion into the shopping list.
             * The suggestion should be dismissed only after the user checks
             * and clears the item.
             */
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

        // Store item count values.
        // Example key: Frozen|fish
        val itemCounts = mutableMapOf<String, Int>()

        // Store category for each item key
        val itemCategories = mutableMapOf<String, String>()

        // Count how many times each food item appears in waste history
        for (waste in wasteLogItems) {
            val suggestionKey = getSuggestionKey(
                name = waste.name,
                category = waste.category,
            )

            val oldCount = itemCounts[suggestionKey] ?: 0
            itemCounts[suggestionKey] = oldCount + 1

            itemCategories[suggestionKey] = waste.category
        }

        // Store suggested shopping rows
        val suggestedRows = mutableListOf<ShoppingListRowUi>()

        // Loop through each wasted item count
        for (entry in itemCounts.entries) {
            val suggestionKey = entry.key
            val count = entry.value

            val category = itemCategories[suggestionKey] ?: "Other"
            val foodName = suggestionKey.substringAfter("|")

            // Suggest item if:
            // 1. The same item was wasted 3 or more times
            // 2. It is not already in the shopping list
            // 3. It is not inside the 3-day cooldown
            if (
                count >= 3 &&
                !alreadyHasItem(foodName, category) &&
                !isSuggestionInCooldown(suggestionKey)
            ) {
                suggestedRows.add(
                    ShoppingListRowUi(
                        id = "suggestion_$suggestionKey",
                        name = "Restock $foodName",
                        subtitle = "$category - Suggested from $count waste records",
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
     * This function checks whether the shopping list already has this suggested item.
     */
    private fun alreadyHasItem(
        foodName: String,
        category: String,
    ): Boolean {
        val restockName = "Restock $foodName"

        // Loop through shopping items
        for (item in savedShoppingItems) {

            // Return true if the same suggested item already exists
            if (item.name == restockName && item.category == category) {
                return true
            }
        }

        return false
    }

    /**
     * This function checks whether the suggestion is still in the 3-day cooldown.
     */
    private fun isSuggestionInCooldown(suggestionKey: String): Boolean {
        val dismissedAt = dismissedSuggestions[suggestionKey]

        if (dismissedAt == null) {
            return false
        }

        val now = System.currentTimeMillis()
        val timePassed = now - dismissedAt

        return timePassed < SUGGESTION_COOLDOWN_MS
    }

    /**
     * This creates one unique key for item-specific suggestions.
     *
     * Example:
     * Frozen + fish becomes Frozen|fish
     */
    private fun getSuggestionKey(
        name: String,
        category: String,
    ): String {
        return "$category|$name"
    }

    /**
     * This function finds a shopping item using its id.
     */
    private fun findShoppingItemById(id: String): ShoppingItemEntity? {
        for (item in savedShoppingItems) {
            if (item.id == id) {
                return item
            }
        }

        return null
    }
}
