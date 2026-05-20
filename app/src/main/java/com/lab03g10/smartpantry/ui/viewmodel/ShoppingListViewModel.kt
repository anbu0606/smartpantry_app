package com.lab03g10.smartpantry.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.PreferenceRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.domain.repo.ShoppingRepository
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import com.lab03g10.smartpantry.worker.ShelfLifeRecalcScheduler
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
    val unit: String,
    val category: String,
    val checked: Boolean,
    val nominalExpiryDate: Long,
    val storageLocationId: String?,
    // Pre-resolved label for the storage chip; the row composable
    // therefore doesn't need to know about the sensors list.
    val storageLabel: String,
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
    // Passed into the storage picker dialog on each row.
    val sensors: List<SensorConfigEntity> = emptyList(),
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
    private val foodRepository: FoodRepository,
    private val sensorRepository: SensorRepository,
    private val recalcScheduler: ShelfLifeRecalcScheduler,
) : ViewModel() {

    companion object {
        // A dismissed suggestion will stay hidden for 3 days
        private const val SUGGESTION_COOLDOWN_MS = 3L * 24L * 60L * 60L * 1000L

        private const val DAY_MS = 24L * 60L * 60L * 1000L

        // Category-based shelf life used as the default expiry shown on a
        // shopping row (and copied into inventory on purchase). Numbers are
        // intentionally conservative — the user can edit each row.
        private val DEFAULT_SHELF_LIFE_DAYS = mapOf(
            "Dairy" to 7,
            "Meat" to 5,
            "Produce" to 7,
            "Grains" to 30,
            "Frozen" to 90,
        )
        private const val DEFAULT_SHELF_LIFE_DAYS_OTHER = 14

        // Category → preferred storage environment when the user hasn't
        // overridden the row. Resolved against the user's actual sensors;
        // if none of that type exist we fall back to "Unmonitored" (null).
        private val DEFAULT_ENVIRONMENT_BY_CATEGORY = mapOf(
            "Dairy" to EnvironmentType.REFRIGERATOR,
            "Meat" to EnvironmentType.REFRIGERATOR,
            "Produce" to EnvironmentType.REFRIGERATOR,
            "Frozen" to EnvironmentType.FREEZER,
            "Grains" to EnvironmentType.PANTRY,
        )
        private val DEFAULT_ENVIRONMENT_OTHER = EnvironmentType.PANTRY

        // Single-purchase default when the source (waste log) has no unit info.
        private const val DEFAULT_QUANTITY = "1"
        private const val DEFAULT_UNIT = "pcs"

        const val STORAGE_LABEL_UNMONITORED = "Unmonitored"
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

    // Sensors available for storage assignment (drives the row's storage
    // chip + picker dialog). Active and inactive sensors both flow
    // through observeConfigs(); the picker filters as needed.
    private var availableSensors: List<SensorConfigEntity> = emptyList()

    // Load shopping list, waste history and dismissed suggestions when ViewModel starts
    init {
        loadShoppingItems()
        loadWasteHistory()
        loadDismissedSuggestions()
        loadSensors()
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
     * Loads the user's sensor configurations so the row can show the
     * paired sensor's display name and the picker can list them.
     */
    private fun loadSensors() {
        viewModelScope.launch {
            try {
                sensorRepository.observeConfigs().collect { sensors ->
                    availableSensors = sensors
                    updateScreenState()
                }
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Could not load sensor configurations",
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
     * Mark all checked items as purchased: copy each into the inventory
     * (food_items) using the per-row quantity / unit / expiry the user
     * adjusted on the shopping row, then remove them from the shopping list.
     */
    fun onDeleteCheckedItems() {
        viewModelScope.launch {

            val now = System.currentTimeMillis()
            var anyTransferred = false

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

                    // Hand the item off to inventory using the values the
                    // user confirmed on the row. nominalExpiryDate doubles
                    // as effectiveExpiryDate until a sensor adjusts it.
                    val foodItem = FoodItemEntity(
                        id = UUID.randomUUID().toString(),
                        name = item.name.removePrefix("Restock "),
                        category = item.category,
                        quantity = item.quantity,
                        unit = item.unit,
                        barcode = null,
                        imageUrl = null,
                        purchaseDate = now,
                        nominalExpiryDate = item.nominalExpiryDate,
                        effectiveExpiryDate = item.nominalExpiryDate,
                        storageLocationId = item.storageLocationId,
                        storageChangedAt = now,
                        notes = null,
                        createdAt = now,
                        updatedAt = now,
                        consumed = false,
                        wasted = false,
                    )
                    foodRepository.upsert(foodItem)
                    anyTransferred = true

                    shoppingRepository.delete(item.id)
                }
            }

            // One recalc pass picks up every newly inserted food item;
            // REPLACE policy dedupes if a SensorTickWorker also triggered.
            if (anyTransferred) recalcScheduler.triggerOnce()
        }
    }

    /**
     * This function adds a suggested item into the real shopping list.
     */
    fun onAddSuggestedItem(row: ShoppingListRowUi) {
        viewModelScope.launch {

            val now = System.currentTimeMillis()

            // Create new shopping item
            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                name = row.name,
                quantity = row.quantity,
                unit = row.unit,
                category = row.category,
                checked = false,
                addedAt = now,
                nominalExpiryDate = row.nominalExpiryDate,
                storageLocationId = row.storageLocationId,
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
     * Row-level edits — three independent updates so a re-render only
     * touches the one column that changed.
     */
    fun onUpdateQuantity(id: String, quantity: String) {
        viewModelScope.launch {
            shoppingRepository.updateQuantity(id, quantity)
        }
    }

    fun onUpdateUnit(id: String, unit: String) {
        viewModelScope.launch {
            shoppingRepository.updateUnit(id, unit)
        }
    }

    fun onUpdateExpiry(id: String, nominalExpiryDate: Long) {
        viewModelScope.launch {
            shoppingRepository.updateExpiry(id, nominalExpiryDate)
        }
    }

    fun onUpdateStorage(id: String, storageLocationId: String?) {
        viewModelScope.launch {
            shoppingRepository.updateStorage(id, storageLocationId)
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
            sensors = availableSensors,
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
            unit = unit,
            category = category,
            checked = checked,
            nominalExpiryDate = nominalExpiryDate,
            storageLocationId = storageLocationId,
            storageLabel = storageLabelFor(storageLocationId),
            autoSuggested = autoSuggested,
        )
    }

    /**
     * Default expiry (epoch millis) to seed a new shopping row with. Used by
     * auto-suggestions; the user can still edit it on the row.
     */
    private fun defaultExpiryFor(category: String, now: Long = System.currentTimeMillis()): Long {
        val days = DEFAULT_SHELF_LIFE_DAYS[category] ?: DEFAULT_SHELF_LIFE_DAYS_OTHER
        return now + days * DAY_MS
    }

    /**
     * Picks a sensor of the preferred environment type for the given
     * category. Returns null ("Unmonitored") if no matching sensor exists.
     */
    private fun defaultStorageFor(category: String): String? {
        val targetType = DEFAULT_ENVIRONMENT_BY_CATEGORY[category] ?: DEFAULT_ENVIRONMENT_OTHER
        return availableSensors.firstOrNull { it.environmentType == targetType.name }?.id
    }

    /**
     * Resolves a sensor id into a human label for the storage chip. The
     * row entity stores only the id; the UI display name lives on
     * SensorConfigEntity, which can be renamed by the user.
     */
    private fun storageLabelFor(storageLocationId: String?): String {
        if (storageLocationId == null) return STORAGE_LABEL_UNMONITORED
        return availableSensors.firstOrNull { it.id == storageLocationId }?.displayName
            ?: STORAGE_LABEL_UNMONITORED
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
                val defaultStorage = defaultStorageFor(category)
                suggestedRows.add(
                    ShoppingListRowUi(
                        id = "suggestion_$suggestionKey",
                        name = "Restock $foodName",
                        subtitle = "$category - Suggested from $count waste records",
                        quantity = DEFAULT_QUANTITY,
                        unit = DEFAULT_UNIT,
                        category = category,
                        checked = false,
                        nominalExpiryDate = defaultExpiryFor(category),
                        storageLocationId = defaultStorage,
                        storageLabel = storageLabelFor(defaultStorage),
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
