package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val items: List<FoodItemEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow("All")

    private val categoryItems = selectedCategory.flatMapLatest { category ->
        if (category == "All") {
            foodRepository.observeAll()
        } else {
            foodRepository.observeByCategory(category)
        }
    }

    val state: StateFlow<InventoryUiState> = combine(
        searchQuery,
        selectedCategory,
        categoryItems,
    ) { query, category, items ->
        val visibleItems = if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.notes.orEmpty().contains(query, ignoreCase = true)
            }
        }
        InventoryUiState(
            searchQuery = query,
            selectedCategory = category,
            items = visibleItems,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        selectedCategory.value = category
    }

    fun delete(item: FoodItemEntity) {
        viewModelScope.launch {
            foodRepository.delete(item.id)
        }
    }

    fun restore(item: FoodItemEntity) {
        viewModelScope.launch {
            foodRepository.upsert(item.copy(updatedAt = System.currentTimeMillis()))
        }
    }
}
