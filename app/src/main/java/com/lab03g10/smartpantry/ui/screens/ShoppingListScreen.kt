package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
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
        onUpdateQuantity = viewModel::onUpdateQuantity,
        onUpdateUnit = viewModel::onUpdateUnit,
        onUpdateExpiry = viewModel::onUpdateExpiry,
        onUpdateStorage = viewModel::onUpdateStorage,
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
    onUpdateQuantity: (String, String) -> Unit = { _, _ -> },
    onUpdateUnit: (String, String) -> Unit = { _, _ -> },
    onUpdateExpiry: (String, Long) -> Unit = { _, _ -> },
    onUpdateStorage: (String, String?) -> Unit = { _, _ -> },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    // Wrap the FAB action so we can post a snackbar based on the count
    // observed at tap time. We deliberately read checkedCount BEFORE the
    // suspend call so the message reflects what the user intended to move.
    val onFabClick: () -> Unit = {
        val count = uiState.checkedCount
        onDeleteCheckedItems()
        if (count > 0) {
            snackbarScope.launch {
                val itemWord = if (count == 1) "item" else "items"
                snackbarHostState.showSnackbar("$count $itemWord moved to pantry")
            }
        }
    }

    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },

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
                    TextButton(onClick = onFabClick) {
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
                onDeleteCheckedItems = onFabClick,
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
                ShoppingItemRow(
                    item = item,
                    sensors = uiState.sensors,
                    onToggle = onToggleItem,
                    onUpdateQuantity = onUpdateQuantity,
                    onUpdateUnit = onUpdateUnit,
                    onUpdateExpiry = onUpdateExpiry,
                    onUpdateStorage = onUpdateStorage,
                )
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
 * The check circle toggles checked state. The two chips on the third
 * line (quantity+unit and expiry) each open an edit dialog without
 * affecting the checked state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingListRowUi,
    sensors: List<SensorConfigEntity>,
    onToggle: (String) -> Unit,
    onUpdateQuantity: (String, String) -> Unit,
    onUpdateUnit: (String, String) -> Unit,
    onUpdateExpiry: (String, Long) -> Unit,
    onUpdateStorage: (String, String?) -> Unit,
) {
    val checked = item.checked
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showExpiryDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (checked) 0.6f else 1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {

            // Tappable check column — kept isolated from the chips so
            // tapping a chip never accidentally toggles the item.
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onToggle(item.id) }
                    .padding(2.dp),
            ) {
                CheckCircle(checked)
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                )

                Text(
                    text = item.subtitle,
                    color = SubtitleGrey,
                )

                Spacer(Modifier.height(8.dp))

                // FlowRow so 3 chips wrap to a second line on narrow
                // phones instead of clipping off-screen.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EditableChip(
                        label = "${item.quantity} ${item.unit}",
                        checked = checked,
                        onClick = { showQuantityDialog = true },
                    )

                    EditableChip(
                        label = formatExpiryLabel(item.nominalExpiryDate),
                        checked = checked,
                        onClick = { showExpiryDialog = true },
                    )

                    EditableChip(
                        label = item.storageLabel,
                        checked = checked,
                        onClick = { showStorageDialog = true },
                    )
                }
            }
        }
    }

    if (showQuantityDialog) {
        EditQuantityUnitDialog(
            initialQuantity = item.quantity,
            initialUnit = item.unit,
            onDismiss = { showQuantityDialog = false },
            onConfirm = { newQuantity, newUnit ->
                if (newQuantity != item.quantity) onUpdateQuantity(item.id, newQuantity)
                if (newUnit != item.unit) onUpdateUnit(item.id, newUnit)
                showQuantityDialog = false
            },
        )
    }

    if (showExpiryDialog) {
        EditExpiryDialog(
            initialEpochMillis = item.nominalExpiryDate,
            onDismiss = { showExpiryDialog = false },
            onConfirm = { newMillis ->
                onUpdateExpiry(item.id, newMillis)
                showExpiryDialog = false
            },
        )
    }

    if (showStorageDialog) {
        EditStorageDialog(
            sensors = sensors,
            initialStorageId = item.storageLocationId,
            onDismiss = { showStorageDialog = false },
            onConfirm = { newId ->
                if (newId != item.storageLocationId) onUpdateStorage(item.id, newId)
                showStorageDialog = false
            },
        )
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
 * Tappable chip used for the quantity-and-unit and the expiry on a
 * saved shopping row. Tapping opens an edit dialog.
 */
@Composable
private fun EditableChip(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (checked) ChipCheckedBg else ChipBg,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (checked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                ChipText
            },
        )
    }
}

/**
 * Dialog letting the user edit both the numeric quantity and the unit
 * in one go — they are coupled (e.g. "500 g" only reads correctly as a
 * pair) so we keep them in the same screen.
 */
@Composable
private fun EditQuantityUnitDialog(
    initialQuantity: String,
    initialUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: String, unit: String) -> Unit,
) {
    var quantityText by remember { mutableStateOf(initialQuantity) }
    var unit by remember { mutableStateOf(initialUnit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit quantity") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newValue ->
                        // Accept digits plus optional decimal point. Reject
                        // anything else so we never persist garbage that
                        // FoodItemEntity can't render.
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            quantityText = newValue
                        }
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                Spacer(Modifier.height(12.dp))

                Text("Unit", style = MaterialTheme.typography.labelLarge)

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UNIT_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = option == unit,
                            onClick = { unit = option },
                            label = { Text(option) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sanitized = quantityText.ifBlank { "1" }
                    onConfirm(sanitized, unit)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Sensor picker — RadioButton list of the user's active sensors plus
 * an "Unmonitored" fallback. Inactive (soft-deleted) sensors are
 * hidden from the picker, but the row still resolves them to their
 * stored display name via [ShoppingListViewModel.storageLabelFor] if
 * they were assigned before deactivation.
 */
@Composable
private fun EditStorageDialog(
    sensors: List<SensorConfigEntity>,
    initialStorageId: String?,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var selected: String? by remember { mutableStateOf(initialStorageId) }
    val activeSensors = sensors.filter { it.active }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage location") },
        text = {
            Column {
                activeSensors.forEach { sensor ->
                    StorageOption(
                        label = sensor.displayName,
                        selected = selected == sensor.id,
                        onClick = { selected = sensor.id },
                    )
                }
                StorageOption(
                    label = "Unmonitored",
                    selected = selected == null,
                    onClick = { selected = null },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun StorageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}

/**
 * Material3 date picker dialog wrapper used for the expiry chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpiryDialog(
    initialEpochMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val picked = state.selectedDateMillis ?: initialEpochMillis
                    onConfirm(picked)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = state)
    }
}

private val UNIT_OPTIONS = listOf("pcs", "g", "kg", "ml", "L")

private val EXPIRY_FORMAT = SimpleDateFormat("MMM d", Locale.getDefault()).apply {
    // DatePicker returns UTC-midnight millis; render the same wall date.
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun formatExpiryLabel(epochMillis: Long): String =
    "Exp " + EXPIRY_FORMAT.format(epochMillis)

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
