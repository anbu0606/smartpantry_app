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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.components.FoodItemCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.util.daysUntil

import com.lab03g10.smartpantry.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

// Design tokens
private val SelectedChipBg     = Color(0xFFC8E6C9)
private val SelectedChipBorder = Color(0xFF2E7D32)
private val SelectedChipText   = Color(0xFF085041)
private val UnselectedChipBorder = Color(0xFFCCCCCC)
private val SearchBg           = Color(0xFFF0F0F0)
private val FabBg              = Color(0xFFC8E6C9)
private val FabIcon            = Color(0xFF085041)

// Category list
private val categories = listOf("All", "Dairy", "Meat", "Produce", "Dry Goods", "Frozen", "Other")
// Display formatter — no explicit TimeZone, so SimpleDateFormat falls back to
// the JVM default (device-local on Android). User-facing dates should read
// in wall-clock time, not UTC. See gap-closure-checklist §10.2.
private val listDateFmt = SimpleDateFormat("MMM d", Locale.ENGLISH)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onNavigateToAddFood: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventory",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
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
                onClick = onNavigateToAddFood,
                shape = RoundedCornerShape(16.dp),
                containerColor = FabBg,
                contentColor = FabIcon,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
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
            InventorySearchField(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 12.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories) { category ->
                    CategoryChip(
                        label = category,
                        selected = category == state.selectedCategory,
                        onClick = { viewModel.onCategorySelected(category) },
                    )
                }
            }

            // Distinguish "no items in the pantry at all" from "filter/search hides everything".
            val emptyTitle = when {
                state.searchQuery.isNotBlank() -> "No matches for \"${state.searchQuery}\""
                state.selectedCategory != "All" -> "No ${state.selectedCategory.lowercase()} items"
                else -> "Your pantry is empty"
            }
            val emptySupporting = when {
                state.searchQuery.isNotBlank() ->
                    "Try a different search or clear the search box."
                state.selectedCategory != "All" ->
                    "Switch to All or tap + to add ${state.selectedCategory.lowercase()}."
                else ->
                    "Tap + to add your first food item, or scan a barcode from the Scanner tab."
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 80.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            icon           = Icons.Outlined.Inventory2,
                            title          = emptyTitle,
                            supportingText = emptySupporting,
                        )
                    }
                }
                items(
                    items = state.items,
                    key = { it.id },
                ) { item ->
                    FoodItemCard(
                        foodName = item.name,
                        category = item.category,
                        quantity = "${item.quantity}${item.unit}",
                        expiryDate = listDateFmt.format(Date(item.effectiveExpiryDate)),
                        daysUntilExpiry = daysUntil(item.effectiveExpiryDate),
                        onEdit = { onNavigateToFoodDetail(item.id) },
                        onDelete = {
                            viewModel.delete(item)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${item.name} deleted",
                                    actionLabel = "Undo",
                                    withDismissAction = true,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restore(item)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InventorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search food items...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SearchBg,
            unfocusedContainerColor = SearchBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) SelectedChipBg else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SelectedChipBorder else UnselectedChipBorder,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
            color = if (selected) SelectedChipText else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewInventoryLight() {
    SmartPantryTheme {
        Text("Inventory preview uses live app data at runtime.")
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF191C18,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewInventoryDark() {
    SmartPantryTheme(darkTheme = true) {
        Text("Inventory preview uses live app data at runtime.")
    }
}
