package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.components.FoodItemCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// Design tokens
private val SelectedChipBg     = Color(0xFFC8E6C9)
private val SelectedChipBorder = Color(0xFF2E7D32)
private val SelectedChipText   = Color(0xFF085041)
private val UnselectedChipBorder = Color(0xFFCCCCCC)
private val SearchBg           = Color(0xFFF0F0F0)
private val FabBg              = Color(0xFFC8E6C9)
private val FabIcon            = Color(0xFF085041)

// Category list
private val categories = listOf("All", "Dairy", "Meat", "Produce", "Dry Goods", "Beverages")

// Hardcoded sample data

/**
 * Simple model used only within this screen.
 * [FoodItemCard] will decide the icon/emoji based on [category].
 */
private data class InventoryItem(
    val id: String,          // Unique ID for LazyColumn
    val name: String,
    val category: String,
    val quantity: String,    // Shows how much stock is available (example: "500g", "2 units")
    val expiryLabel: String, //  Date displayed in the card (easy to read format)
    val daysUntilExpiry: Int,
)

// Today's date (2026-04-17) is hardcoded for testing
private val initialItems = listOf(
    InventoryItem("milk",    "Full Cream Milk",  "Dairy",     "1L",      "Apr 17", 0),
    InventoryItem("chicken", "Chicken Breast",   "Meat",      "500g",    "Apr 18", 1),
    InventoryItem("spinach", "Baby Spinach",     "Produce",   "200g",    "Apr 19", 2),
    InventoryItem("cheese",  "Cheddar Cheese",   "Dairy",     "400g",    "Apr 22", 5),
    InventoryItem("rice",    "Basmati Rice",     "Dry Goods", "1kg",     "Aug 15", 120),
    InventoryItem("carrots", "Carrots",          "Produce",   "6 units", "Apr 21", 4),
    InventoryItem("butter",  "Butter",           "Dairy",     "250g",    "Apr 17", 0),
)

// Public composable

/**
 * Inventory list screen — shows all pantry items with search, filter, and swipe-to-delete.
 *
 * Screen layout includes:
 * - Top bar with title
 * - Search field
 * - Category filter row
 * - List of food items
 * - Add button at bottom right
 *
 * Items are stored in a list and updates happen in the same session
 * Search and filter only change what is shown, not the actual data
 *
 * @param onNavigateToAddFood -> opens add food screen when + button is clicked
 * @param onNavigateToFoodDetail -> opens item details when user clicks an item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit = {},
) {

    // ── Mutable list — survives recompositions; swipe-deletes are permanent
    val foodItems = remember { mutableStateListOf(*initialItems.toTypedArray()) }

    // UI state
    var searchQuery       by remember { mutableStateOf("") }
    var selectedCategory  by remember { mutableStateOf("All") }

    // ── Derived filtered list (no snapshot needed — recomputed on each render) ─
    val visibleItems = foodItems.filter { item ->
        (selectedCategory == "All" || item.category == selectedCategory) &&
        (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Inventory",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: advanced sort/filter sheet */ }) {
                        Icon(
                            imageVector        = Icons.Outlined.FilterList,
                            contentDescription = "Sort and filter",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onNavigateToAddFood,
                shape            = RoundedCornerShape(16.dp),
                containerColor   = FabBg,
                contentColor     = FabIcon,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Add,
                    contentDescription = "Add food item",
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            // Search field
            InventorySearchField(
                query          = searchQuery,
                onQueryChange  = { searchQuery = it },
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Category filter chips
            LazyRow(
                contentPadding        = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = 4.dp,
                    bottom = 12.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories) { category ->
                    CategoryChip(
                        label    = category,
                        selected = category == selectedCategory,
                        onClick  = { selectedCategory = category },
                    )
                }
            }

            // Food item list
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    bottom = 80.dp,   // clears FAB + breathing room
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = visibleItems,
                    key   = { it.id },    // stable key preserves item animations
                ) { item ->
                    FoodItemCard(
                        foodName        = item.name,
                        category        = item.category,
                        quantity        = item.quantity,
                        expiryDate      = item.expiryLabel,
                        daysUntilExpiry = item.daysUntilExpiry,
                        onEdit          = { onNavigateToFoodDetail("sample-1") }, // TODO(Anbu Day 4): pass real item.id
                        onDelete        = { foodItems.remove(item) },
                    )
                }
            }
        }
    }
}

// Search field

/**
 * Search field used in this screen
 * Styled with light grey background and rounded corners
 * Includes a search icon at the start
 * No underline is shown
 * Uses [TextField] and the default bottom line is hidden
 */
@Composable
private fun InventorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier,
        placeholder   = {
            Text(
                text  = "Search food items...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Outlined.Search,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape      = RoundedCornerShape(12.dp),
        colors     = TextFieldDefaults.colors(
            focusedContainerColor   = SearchBg,
            unfocusedContainerColor = SearchBg,
            // Remove M3's default bottom indicator line
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor  = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

// Category chip

/**
 * Category chip used for filtering items
 *
 * Rounded button style
 * Changes color and text style when selected
 * Looks normal when not selected
 */
@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape   = CircleShape,
        color   = if (selected) SelectedChipBg else MaterialTheme.colorScheme.surface,
        border  = BorderStroke(
            width = 1.dp,
            color = if (selected) SelectedChipBorder else UnselectedChipBorder,
        ),
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style    = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
            color    = if (selected) {
                SelectedChipText
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

// Previews
// Note:  Swipe to delete cannot be shown in preview, it only works when the app is running.
// This is handled inside FoodItemCard

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewInventoryLight() {
    SmartPantryTheme {
        InventoryListScreen(onNavigateToAddFood = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewInventoryDark() {
    SmartPantryTheme(darkTheme = true) {
        InventoryListScreen(onNavigateToAddFood = {})
    }
}
