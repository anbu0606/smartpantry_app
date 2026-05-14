package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.AddEditFoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val SaveGreen   = Color(0xFF2E7D32)
private val LabelGrey   = Color(0xFF888888)

// Date format
private val displayFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun epochMillisToDisplay(millis: Long?): String =
    if (millis != null) displayFmt.format(Date(millis)) else ""

//Drop down listing items
private val categoryOptions = listOf("Dairy", "Meat", "Produce", "Dry Goods", "Frozen", "Other")
private val unitOptions     = listOf("g", "kg", "L", "ml", "pcs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    foodId: String? = null,
    isEditMode: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: AddEditFoodViewModel = hiltViewModel(),
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    var categoryExpanded  by remember { mutableStateOf(false) }

    var storageLocationExpanded by remember { mutableStateOf(false) }


    var unitExpanded      by remember { mutableStateOf(false) }


    var showPurchasePicker by remember { mutableStateOf(false) }
    var showExpiryPicker   by remember { mutableStateOf(false) }


    //Date picker lazy layout
    val purchasePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.purchaseDate,
    )
    val expiryPickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.nominalExpiryDate,
    )
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    if (showPurchasePicker) {
        DatePickerDialog(
            onDismissRequest = { showPurchasePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.onPurchaseDateChange(purchasePickerState.selectedDateMillis)
                    showPurchasePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(
                state  = purchasePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }

    if (showExpiryPicker) {
        DatePickerDialog(
                onDismissRequest = {
                    viewModel.onExpiryTouched()
                    showExpiryPicker = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onExpiryDateChange(expiryPickerState.selectedDateMillis)
                        showExpiryPicker = false
                    }) { Text("OK") }
                },
                dismissButton = @androidx.compose.runtime.Composable {
                    TextButton(onClick = {
                        viewModel.onExpiryTouched()
                        showExpiryPicker = false
                    }) { Text("Cancel") }
                },
        ) @androidx.compose.runtime.Composable {
            DatePicker(
                state  = expiryPickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }


    //screen scaffold
    Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (state.isEditMode || isEditMode || foodId != null) "Edit Food Item" else "Add Food Item",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Normal,
                                    ),
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                    )
                                }
                            },
                            actions = {
                                TextButton(
                                    onClick = viewModel::save,
                                    enabled = state.isValid,
                                ) {
                                    Text(
                                        text = "Save",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = if (state.isValid) {
                                            SaveGreen
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        },
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                        )
                    },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.labelSmall,
                    color = LabelGrey,
                )

                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) viewModel.onNameBlur() },
                    label = { Text("Food name *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                        )
                    },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { message -> { Text(message) } },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                DropdownField(
                    label = "Category *",
                    value = state.category,
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    options = categoryOptions,
                    onOptionSelected = {
                        viewModel.onCategoryChange(it)
                        categoryExpanded = false
                    },
                )

                ExposedDropdownMenuBox(
                    expanded = storageLocationExpanded,
                    onExpandedChange = { storageLocationExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val storageLabel = state.sensors
                        .firstOrNull { it.id == state.storageLocationId }
                        ?.displayName
                        ?: "Unmonitored"
                    OutlinedTextField(
                        value = storageLabel,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text("Storage location") },
                        trailingIcon = { DropdownIcon(storageLocationExpanded) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = storageLocationExpanded,
                        onDismissRequest = { storageLocationExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unmonitored") },
                            onClick = {
                                viewModel.onStorageLocationChange(null)
                                storageLocationExpanded = false
                            },
                        )
                        state.sensors.forEach { sensor ->
                            DropdownMenuItem(
                                text = { Text(sensor.displayName) },
                                onClick = {
                                    viewModel.onStorageLocationChange(sensor.id)
                                    storageLocationExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = epochMillisToDisplay(state.purchaseDate),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Purchase date *") },
                    trailingIcon = {
                        IconButton(onClick = { showPurchasePicker = true }) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Pick purchase date",
                                tint = SaveGreen,
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = epochMillisToDisplay(state.nominalExpiryDate),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Expiry date *") },
                    trailingIcon = {
                        IconButton(onClick = { showExpiryPicker = true }) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Pick expiry date",
                                tint = if (state.expiryError != null) MaterialTheme.colorScheme.error else SaveGreen,
                            )
                        }
                    },
                    isError = state.expiryError != null,
                    supportingText = state.expiryError?.let { message -> { Text(message) } },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        modifier = Modifier
                            .weight(0.6f)
                            .onFocusChanged { if (!it.isFocused) viewModel.onQuantityBlur() },
                        label = { Text("Quantity *") },
                        singleLine = true,
                        isError = state.quantityError != null,
                        supportingText = state.quantityError?.let { message -> { Text(message) } },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )

                    DropdownField(
                        label = "Unit",
                        value = state.unit,
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        options = unitOptions,
                        onOptionSelected = {
                            viewModel.onUnitChange(it)
                            unitExpanded = false
                        },
                        modifier = Modifier.weight(0.4f),
                    )
                }

                OutlinedTextField(
                    value = state.barcode,
                    onValueChange = viewModel::onBarcodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Barcode") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
                    placeholder = {
                        Text(
                            text = "Add storage tips or details...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                )

                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.isValid,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.60f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                ) {
                    Text(
                        text = "Save Food Item",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DropdownField(
        label: String,
        value: String,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        options: List<String>,
        onOptionSelected: (String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { DropdownIcon(expanded) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (option == value) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (option == value) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = { onOptionSelected(option) },
                    )
                }
            }
        }
    }
// inline validation for the prototype
@Composable
private fun DropdownIcon(expanded: Boolean) {
    Icon(
        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
        contentDescription = null,
    )
}

    @Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
    @Composable
    private fun PreviewAddLight() {
        SmartPantryTheme {
            Text("Add food form uses live app data at runtime.")
        }
    }

    @Preview(
        name = "Dark",
        showBackground = true,
        backgroundColor = 0xFF191C18,
        uiMode = UI_MODE_NIGHT_YES,
    )
    @Composable
    private fun PreviewAddDark() {
        SmartPantryTheme(darkTheme = true) {
            Text("Add food form uses live app data at runtime.")
        }
    }

