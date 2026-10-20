package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.worker.ShelfLifeRecalcScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditFoodUiState(
    val foodId: String? = null,
    val name: String = "",
    val category: String = "Dairy",
    val quantity: String = "",
    val unit: String = "g",
    val barcode: String = "",
    val imageUrl: String? = null,
    val purchaseDate: Long? = todayStartMillis(),
    val nominalExpiryDate: Long? = null,
    val storageLocationId: String? = null,
    val notes: String = "",
    val sensors: List<SensorConfigEntity> = emptyList(),
    val nameTouched: Boolean = false,
    val quantityTouched: Boolean = false,
    val expiryTouched: Boolean = false,
    val isSaved: Boolean = false,
) {
    val isEditMode: Boolean = foodId != null
    val nameError: String? = if (nameTouched && name.isBlank()) "Name is required" else null
    val quantityError: String? = when {
        !quantityTouched -> null
        quantity.isBlank() -> "Quantity is required"
        quantity.toDoubleOrNull() == null -> "Enter a number"
        quantity.toDouble() <= 0.0 -> "Quantity must be greater than 0"
        else -> null
    }
    val expiryError: String? = when {
        !expiryTouched && nominalExpiryDate == null -> null
        nominalExpiryDate == null -> "Expiry date is required"
        nominalExpiryDate < todayStartMillis() -> "Expiry date cannot be in the past"
        purchaseDate != null && nominalExpiryDate < purchaseDate -> "Expiry must be after purchase"
        else -> null
    }
    val isValid: Boolean =
        name.isNotBlank() &&
                quantity.toDoubleOrNull()?.let { it > 0.0 } == true &&
                purchaseDate != null &&
                nominalExpiryDate != null &&
                expiryError == null
}

@HiltViewModel
class AddEditFoodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val sensorRepository: SensorRepository,
    private val recalcScheduler: ShelfLifeRecalcScheduler,
) : ViewModel() {

    private val foodId: String? = savedStateHandle["foodId"]
    private val prefillName: String? = savedStateHandle["name"]
    private val prefillCategory: String? = savedStateHandle["category"]
    private val prefillBarcode: String? = savedStateHandle["barcode"]
    private val prefillImageUrl: String? = savedStateHandle["imageUrl"]
    private val form = MutableStateFlow(
        AddEditFoodUiState(
            foodId = foodId,
            name = prefillName.orEmpty(),
            category = prefillCategory?.takeIf { it.isNotBlank() } ?: "Dairy",
            barcode = prefillBarcode.orEmpty(),
            imageUrl = prefillImageUrl?.takeIf { it.isNotBlank() },
            quantity = if (prefillName.isNullOrBlank()) "" else "1",
            unit = if (prefillName.isNullOrBlank()) "g" else "pcs",
        ),
    )

    val state: StateFlow<AddEditFoodUiState> = combine(
        form,
        sensorRepository.observeConfigs(),
    ) { current, sensors ->
        current.copy(sensors = sensors)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddEditFoodUiState(foodId = foodId),
    )

    init {
        if (foodId != null) {
            viewModelScope.launch {
                foodRepository.observeById(foodId).firstOrNull()?.let { item ->
                    form.value = form.value.copy(
                        foodId = item.id,
                        name = item.name,
                        category = item.category,
                        quantity = item.quantity,
                        unit = item.unit,
                        barcode = item.barcode.orEmpty(),
                        imageUrl = item.imageUrl,
                        purchaseDate = item.purchaseDate,
                        nominalExpiryDate = item.nominalExpiryDate,
                        storageLocationId = item.storageLocationId,
                        notes = item.notes.orEmpty(),
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = form.update { it.copy(name = value, nameTouched = true) }
    fun onCategoryChange(value: String) = form.update { it.copy(category = value) }
    fun onQuantityChange(value: String) = form.update { it.copy(quantity = value, quantityTouched = true) }
    fun onUnitChange(value: String) = form.update { it.copy(unit = value) }
    fun onBarcodeChange(value: String) = form.update { it.copy(barcode = value.filter(Char::isDigit)) }
    fun onStorageLocationChange(value: String?) = form.update { it.copy(storageLocationId = value) }
    fun onPurchaseDateChange(value: Long?) = form.update { it.copy(purchaseDate = value) }
    fun onExpiryDateChange(value: Long?) = form.update { it.copy(nominalExpiryDate = value, expiryTouched = true) }
    fun onNotesChange(value: String) = form.update { it.copy(notes = value) }
    fun onNameBlur() = form.update { it.copy(nameTouched = true) }
    fun onQuantityBlur() = form.update { it.copy(quantityTouched = true) }
    fun onExpiryTouched() = form.update { it.copy(expiryTouched = true) }

    fun save() {
        val current = form.value.copy(
            nameTouched = true,
            quantityTouched = true,
            expiryTouched = true,
        )
        form.value = current
        if (!current.isValid) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = current.foodId?.let { foodRepository.observeById(it).firstOrNull() }
            val storageChanged =
                existing != null && existing.storageLocationId != current.storageLocationId
            val expiryUserEdited = existing != null
                && current.nominalExpiryDate != null
                && current.nominalExpiryDate != existing.nominalExpiryDate

            // Storage-move shelf-life scaling: when the user moves a food
            // to a different sensor's environment (and isn't simultaneously
            // overriding the expiry by hand), re-predict the remaining
            // days by the destination/source env-type ratio. Without this,
            // moving Bread Loaf from Pantry → Freezer leaves "3 days left"
            // unchanged, which contradicts everyday intuition about
            // freezing. Returned `null` means "no scaling applies" (no
            // move, same env type, expired food, unknown sensor, or user
            // edited expiry on the same save).
            val scaledExpiry: Long? = existing
                ?.takeIf { storageChanged && !expiryUserEdited }
                ?.let { previous ->
                    val sensors = sensorRepository.observeConfigs().firstOrNull().orEmpty()
                    val fromEnv = sensors.firstOrNull { it.id == previous.storageLocationId }
                        ?.environmentType
                    val toEnv = sensors.firstOrNull { it.id == current.storageLocationId }
                        ?.environmentType
                    scaledExpiryAfterMove(previous.effectiveExpiryDate, now, fromEnv, toEnv)
                }

            val item = FoodItemEntity(
                id = current.foodId ?: UUID.randomUUID().toString(),
                name = current.name.trim(),
                category = current.category,
                quantity = current.quantity.trim(),
                unit = current.unit,
                barcode = current.barcode.trim().takeIf { it.isNotBlank() },
                imageUrl = existing?.imageUrl ?: current.imageUrl,
                purchaseDate = current.purchaseDate ?: now,
                // After a storage move, both nominal and effective shift
                // to the scaled value — keeping the old nominal would let
                // ShelfLifeCalculator's (nominal - purchase) baseline pull
                // the scaled-up effective back down on the next tick.
                nominalExpiryDate = scaledExpiry ?: (current.nominalExpiryDate ?: now),
                // Three-case fix for the regression introduced in a00483a:
                //   A — add mode: nothing to preserve, start from nominal.
                //   B — edit mode AND user changed nominalExpiryDate: their explicit
                //       edit takes precedence; ShelfLifeRecalcWorker will re-adjust
                //       on the next tick.
                //   C — edit mode but expiry untouched: preserve the worker's
                //       computed effective value so context-aware recalculation
                //       isn't silently undone by a no-op edit-save.
                //   Storage move with scaling: scaledExpiry wins outright.
                //   Storage move without scaling (same env type, already
                //   expired, or unknown sensor): fall back to clean-slate
                //   on nominal so the worker reaccumulates from the new
                //   location forward.
                effectiveExpiryDate = scaledExpiry ?: when {
                    !current.isEditMode -> current.nominalExpiryDate ?: now
                    storageChanged -> current.nominalExpiryDate ?: now
                    current.nominalExpiryDate != null
                        && current.nominalExpiryDate != existing?.nominalExpiryDate ->
                            current.nominalExpiryDate
                    else -> existing?.effectiveExpiryDate ?: current.nominalExpiryDate ?: now
                },
                storageLocationId = current.storageLocationId,
                // Stamp the move time so ShelfLifeRecalcWorker's lookback
                // floor for this food jumps to "now" — destination-sensor
                // history older than the move is excluded from stress calc.
                storageChangedAt = when {
                    !current.isEditMode -> now
                    storageChanged -> now
                    else -> existing?.storageChangedAt ?: now
                },
                notes = current.notes.trim().takeIf { it.isNotBlank() },
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                consumed = existing?.consumed ?: false,
                wasted = existing?.wasted ?: false,
            )
            foodRepository.upsert(item)
            // Immediate recalc on move so the user doesn't wait up to
            // 15 min for the next periodic tick to refresh expiry.
            if (storageChanged) recalcScheduler.triggerOnce()
            form.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        // Relative shelf-life multipliers per storage environment, with
        // REFRIGERATOR pinned to 1.0 as the baseline. Applied only when a
        // food's storage moves between sensors of different environment
        // types — scales the currently-displayed remaining shelf life by
        // toMult/fromMult. Numbers are deliberately broad-brush; tune in
        // one place when domain rules sharpen.
        private val STORAGE_SHELF_LIFE_MULTIPLIER = mapOf(
            EnvironmentType.REFRIGERATOR to 1.0,
            EnvironmentType.FREEZER      to 5.0,
            EnvironmentType.PANTRY       to 0.5,
            EnvironmentType.CUPBOARD     to 0.5,
            EnvironmentType.CELLAR       to 0.75,
            EnvironmentType.OPEN_SHELF   to 0.3,
            EnvironmentType.GARAGE       to 0.25,
            EnvironmentType.CUSTOM       to 1.0,
        )
        private const val NEUTRAL_MULTIPLIER = 1.0
    }

    /**
     * Resolve a SensorConfigEntity.environmentType string into a
     * relative shelf-life multiplier. Unknown / null / unmonitored all
     * fall back to neutral (no scaling) so the move still completes
     * without surprising the user.
     */
    private fun multiplierFor(envTypeName: String?): Double {
        if (envTypeName == null) return NEUTRAL_MULTIPLIER
        return runCatching { EnvironmentType.valueOf(envTypeName) }
            .getOrNull()
            ?.let { STORAGE_SHELF_LIFE_MULTIPLIER[it] }
            ?: NEUTRAL_MULTIPLIER
    }

    /**
     * Re-predict a food's expiry after a storage move. Scales the
     * currently-displayed remaining shelf life by destination/source
     * env-type ratio so freezing visibly extends and ambient storage
     * visibly shortens.
     *
     * Returns null when scaling shouldn't apply — same env type, or the
     * food was already expired (freezing doesn't unmold what's gone off).
     */
    private fun scaledExpiryAfterMove(
        oldEffectiveExpiry: Long,
        now: Long,
        fromEnvType: String?,
        toEnvType: String?,
    ): Long? {
        val oldRemaining = oldEffectiveExpiry - now
        if (oldRemaining <= 0) return null

        val fromMult = multiplierFor(fromEnvType)
        val toMult = multiplierFor(toEnvType)
        if (fromMult == toMult) return null

        val newRemaining = (oldRemaining * (toMult / fromMult)).toLong()
        return now + newRemaining
    }
}

/**
 * Epoch millis for **local** midnight today. Local — not UTC — because
 * `purchaseDate` and the "expiry in the past" validator are user-facing
 * concepts tied to the user's wall-clock day. UTC would shift the boundary
 * by up to 14 h, so an 11 pm AEST add could record purchaseDate = tomorrow.
 * The same local-time policy is used across the app.
 */
private fun todayStartMillis(): Long {
    val calendar = Calendar.getInstance()    // default TimeZone = device-local
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
