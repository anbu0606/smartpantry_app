package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.ShoppingListRowUi
import com.lab03g10.smartpantry.ui.viewmodel.ShoppingListUiState
import com.lab03g10.smartpantry.ui.viewmodel.ShoppingListViewModel

// Section header color
private val SectionHeaderGreen = Color(0xFF0F6E56)

// Clear checked items button color
private val ClearAllRed = Color(0xFFB71C1C)

// Checked item color
private val CheckedGreen = Color(0xFF2E7D32)

// Chip colors
private val ChipBg = Color(0xFFC8E6C9)
private val ChipText = Color(0xFF085041)
private val ChipCheckedBg = Color(0xFFE8E8E8)

// Floating action button colors
private val FabBg = Color(0xFFC8E6C9)
private val FabIcon = Color(0xFF085041)

// Small text color
private val SubtitleGrey = Color(0xFF888888)

/**
 * This route connects ShoppingListViewModel to ShoppingListScreen.
 *
 * AppNavigation should call this route instead of screen directly.
 */
@Composable
fun ShoppingListRoute(
    onNavigateBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    ShoppingListScreen(
        onNavigateBack = onNavigateBack,
        uiState = viewModel.uiState,
        onToggleItem = viewModel::onToggleItem,
        onDeleteCheckedItems = viewModel::onDeleteCheckedItems,
        onAddSuggestedItem = viewModel::onAddSuggestedItem,
    )
}

/**
 * Shopping List screen UI.
 *
 * Shows saved items + suggested items from waste history.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListScreen(
    onNavigateBack: () -> Unit,
    uiState: ShoppingListUiState = ShoppingListUiState(),
    onToggleItem: (String) -> Unit = {},
    onDeleteCheckedItems: () -> Unit = {},
    onAddSuggestedItem: (ShoppingListRowUi) -> Unit = {},
) {
    Scaffold(

        // Top app bar section
        topBar = {
            TopAppBar(

                // Screen title
                title = {
                    Text("Shopping List")
                },

                // Back button
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },

                // Actions on top bar
                actions = {

                    // Share icon (not implemented yet)
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                        )
                    }

                    // Clear checked items button
                    TextButton(onClick = onDeleteCheckedItems) {
                        Text("Clear checked", color = ClearAllRed)
                    }
                }
            )
        },

        // Floating button (shows progress + delete action)
        floatingActionButton = {
            ShoppingProgressFab(
                checkedCount = uiState.checkedCount,
                totalCount = uiState.totalCount,
                onDeleteCheckedItems = onDeleteCheckedItems,
            )
        }
    ) { innerPadding ->

        // Full-empty state — both the user-added list AND the auto-suggestions are
        // empty. Per-section "no items / no suggestions" hints below still cover
        // the asymmetric cases (one section populated, the other empty).
        if (uiState.items.isEmpty() && uiState.suggestedItems.isEmpty()) {
            EmptyState(
                icon           = Icons.Outlined.ShoppingCart,
                title          = "Nothing on your list",
                supportingText = "Add an item, or wait for auto-suggestions from your waste history.",
                modifier       = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        // Scrollable list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {

            // Small description text
            item {
                Text(
                    text = "Items to buy and smart restock suggestions",
                    color = SubtitleGrey,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Section header for items
            stickyHeader {
                SectionHeader("ITEMS TO BUY")
            }

            // Empty state
            if (uiState.items.isEmpty()) {
                item {
                    EmptyShoppingMessage("No items to buy yet")
                }
            }

            // Shopping items list
            items(uiState.items, key = { it.id }) { item ->
                ShoppingItemRow(item, onToggleItem)
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Section header for suggestions
            stickyHeader {
                SectionHeader("SMART SUGGESTIONS")
            }

            // Empty suggestions
            if (uiState.suggestedItems.isEmpty()) {
                item {
                    EmptyShoppingMessage("No smart suggestions yet")
                }
            }

            // Suggested items list
            items(uiState.suggestedItems, key = { it.id }) { item ->
                SuggestedItemRow(item, onAddSuggestedItem)
            }
        }
    }
}

/**
 * Floating button area showing checked progress and remove checked action.
 */
@Composable
private fun ShoppingProgressFab(
    checkedCount: Int,
    totalCount: Int,
    onDeleteCheckedItems: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {

        // Progress text
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.background
        ) {
            Text(
                text = if (totalCount == 0) {
                    "No items to clear"
                } else {
                    "$checkedCount of $totalCount checked"
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Delete checked button
        FloatingActionButton(
            onClick = onDeleteCheckedItems,
            containerColor = FabBg,
            contentColor = FabIcon
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Done")
        }
    }
}

/**
 * Section title used above shopping items and suggestions.
 */
@Composable
private fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = title,
            color = SectionHeaderGreen,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * One saved shopping item row.
 *
 * Tapping this row toggles checked or unchecked state.
 */
@Composable
private fun ShoppingItemRow(
    item: ShoppingListRowUi,
    onToggle: (String) -> Unit,
) {
    val checked = item.checked

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onToggle(item.id)
            }
            .alpha(if (checked) 0.6f else 1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckCircle(checked)

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    textDecoration = if (checked) TextDecoration.LineThrough else null
                )

                Text(
                    text = item.subtitle,
                    color = SubtitleGrey
                )
            }

            QuantityChip(item.quantity, checked)
        }
    }
}

/**
 * One suggested row from waste history.
 *
 * Tapping Add saves it into the real shopping list.
 */
@Composable
private fun SuggestedItemRow(
    item: ShoppingListRowUi,
    onAdd: (ShoppingListRowUi) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.name)
                Text(item.subtitle, color = SubtitleGrey)
            }

            // Add button
            TextButton(onClick = { onAdd(item) }) {
                Text("Add")
            }
        }
    }
}

/**
 * Checkbox-style circle shown on saved shopping rows.
 */
@Composable
private fun CheckCircle(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                if (checked) {
                    CheckedGreen
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .border(
                width = 2.dp,
                color = if (checked) {
                    CheckedGreen
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Quantity chip at the end of a shopping item row.
 */
@Composable
private fun QuantityChip(quantity: String, checked: Boolean) {
    Surface(color = if (checked) ChipCheckedBg else ChipBg) {
        Text(
            text = quantity,
            modifier = Modifier.padding(6.dp),
            color = if (checked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                ChipText
            }
        )
    }
}

/**
 * Message shown when a section has no rows.
 */
@Composable
private fun EmptyShoppingMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(16.dp),
        color = SubtitleGrey
    )
}

/**
 * Preview - Light mode.
 */
@Preview(showBackground = true)
@Composable
private fun PreviewShoppingLight() {
    SmartPantryTheme {
        ShoppingListScreen(onNavigateBack = {})
    }
}

/**
 * Preview - Dark mode.
 */
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewShoppingDark() {
    SmartPantryTheme(darkTheme = true) {
        ShoppingListScreen(onNavigateBack = {})
    }
}
