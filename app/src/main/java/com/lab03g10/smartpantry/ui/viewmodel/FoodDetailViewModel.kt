package com.lab03g10.smartpantry.ui.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodDetailUiState(
    val item: FoodItemEntity? = null,
    val storageLocation: SensorConfigEntity? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val wasteRepository: WasteRepository,
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val foodId: String = savedStateHandle["foodId"] ?: ""

    val state: StateFlow<FoodDetailUiState> = combine(
        foodRepository.observeById(foodId),
        sensorRepository.observeConfigs(),
    ) { item, sensors ->
        FoodDetailUiState(
            item = item,
            storageLocation = sensors.firstOrNull { it.id == item?.storageLocationId },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FoodDetailUiState(),
    )

    fun markConsumed() {
        viewModelScope.launch {
            foodRepository.markConsumed(foodId)
        }
    }

    fun markWasted(reason: String) {
        val item = state.value.item ?: return
        viewModelScope.launch {
            wasteRepository.log(
                WasteLogEntity(
                    id = UUID.randomUUID().toString(),
                    foodItemId = item.id,
                    name = item.name,
                    category = item.category,
                    quantity = "${item.quantity} ${item.unit}",
                    wastedAt = System.currentTimeMillis(),
                    reason = reason,
                    co2eGrams = estimateCo2eGrams(item),
                ),
            )
            foodRepository.markWasted(item.id)
        }
    }
}

private fun estimateCo2eGrams(item: FoodItemEntity): Float {
    val kg = when (item.unit.lowercase()) {
        "kg", "l" -> item.quantity.toFloatOrNull() ?: 0f
        "g", "ml" -> (item.quantity.toFloatOrNull() ?: 0f) / 1_000f
        "pcs" -> (item.quantity.toFloatOrNull() ?: 1f) * 0.15f
        else -> item.quantity.toFloatOrNull() ?: 0f
    }
    val kgCo2ePerKg = when (item.category) {
        "Dairy" -> 18f
        "Meat" -> 27f
        "Produce" -> 2f
        "Dry Goods", "Grains" -> 1.5f
        else -> 5f
    }
    return kg * kgCo2ePerKg * 1_000f
}
