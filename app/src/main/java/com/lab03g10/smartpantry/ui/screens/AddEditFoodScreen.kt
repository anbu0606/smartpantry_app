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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
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
private val categoryOptions = listOf("Dairy", "Meat", "Produce", "Dry Goods", "Beverages", "Other")
private val unitOptions     = listOf("g", "kg", "L", "ml", "pcs")

/**
 Storage locations matching the sensors configured in StorageMonitorScreen
 */
private val storageLocationOptions = listOf("Fridge", "Pantry", "Cupboard")

// default dates for the ui prototype
private val defaultPurchaseMillis = 1_744_761_600_000L  // 2026-04-16
private val defaultExpiryMillis   = 1_744_848_000_000L  // 2026-04-17
private val todayStartMillis      = 1_744_848_000_000L  // 2026-04-17


/**
 * Add and Edit Food Item screen
 * DatePickerDialog triggered by clicking the calendar symbol
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    foodId: String? = null,
    isEditMode: Boolean = false,
    onNavigateBack: () -> Unit,
) {
    var foodName          by remember { mutableStateOf("") }
    var foodNameTouched   by remember { mutableStateOf(false) }

    var category          by remember { mutableStateOf("Dairy") }
    var categoryExpanded  by remember { mutableStateOf(false) }

    var storageLocation         by remember { mutableStateOf("Fridge") }
    var storageLocationExpanded by remember { mutableStateOf(false) }

    var purchaseMillis    by remember { mutableStateOf<Long?>(defaultPurchaseMillis) }
    var expiryMillis      by remember { mutableStateOf<Long?>(defaultExpiryMillis) }

    var quantity          by remember { mutableStateOf("500") }
    var unit              by remember { mutableStateOf("g") }
    var unitExpanded      by remember { mutableStateOf(false) }

    var notes             by remember { mutableStateOf("") }

    var showPurchasePicker by remember { mutableStateOf(false) }
    var showExpiryPicker   by remember { mutableStateOf(false) }
    val foodNameError  = foodNameTouched && foodName.isBlank()
    val expiryError    = expiryMillis != null && expiryMillis!! < todayStartMillis
    val isFormValid    = foodName.isNotBlank()
            && purchaseMillis != null
            && expiryMillis   != null
            && !expiryError
            && quantity.isNotBlank()

    //Date picker lazy layout
    val purchasePickerState = rememberDatePickerState(
        initialSelectedDateMillis = purchaseMillis ?: defaultPurchaseMillis,
    )
    val expiryPickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryMillis ?: defaultExpiryMillis,
    )

    if (showPurchasePicker) {
        DatePickerDialog(
            onDismissRequest = { showPurchasePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    purchaseMillis = purchasePickerState.selectedDateMillis
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
            onDismissRequest = { showExpiryPicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    expiryMillis = expiryPickerState.selectedDateMillis
                    showExpiryPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel") }
            },
        ) {
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
                        text  = if (isEditMode) "Edit Food Item" else "Add Food Item",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick  = { if (isFormValid) onNavigateBack() },
                        enabled  = isFormValid,
                    ) {
                        Text(
                            text  = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = if (isFormValid) SaveGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
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

            //Legend
            Text(
                text  = "* Required fields",
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey,
            )

            //Food Name
            OutlinedTextField(
                value         = foodName,
                onValueChange = { foodName = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (!fs.isFocused) foodNameTouched = true
                    },
                label       = { Text("Food name *") },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Outlined.Restaurant,
                        contentDescription = null,
                    )
                },
                isError        = foodNameError,
                supportingText = if (foodNameError) {
                    { Text("Name is required", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
            )

            //Category dropdown
            ExposedDropdownMenuBox(
                expanded          = categoryExpanded,
                onExpandedChange  = { categoryExpanded = it },
                modifier          = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value         = category,
                    onValueChange = {},
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly      = true,
                    label         = { Text("Category *") },
                    trailingIcon  = {
                        Icon(
                            imageVector = if (categoryExpanded) {
                                Icons.Filled.ArrowDropUp
                            } else {
                                Icons.Filled.ArrowDropDown
                            },
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded         = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text    = {
                                Text(
                                    text  = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (option == category) {
                                            FontWeight.SemiBold
                                        } else {
                                            FontWeight.Normal
                                        },
                                    ),
                                    color = if (option == category) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }

            //Storage Location
            ExposedDropdownMenuBox(
                expanded         = storageLocationExpanded,
                onExpandedChange = { storageLocationExpanded = it },
                modifier         = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value         = storageLocation,
                    onValueChange = {},
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly      = true,
                    label         = { Text("Storage location *") },
                    trailingIcon  = {
                        Icon(
                            imageVector = if (storageLocationExpanded) {
                                Icons.Filled.ArrowDropUp
                            } else {
                                Icons.Filled.ArrowDropDown
                            },
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded         = storageLocationExpanded,
                    onDismissRequest = { storageLocationExpanded = false },
                ) {
                    storageLocationOptions.forEach { option ->
                        DropdownMenuItem(
                            text    = {
                                Text(
                                    text  = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (option == storageLocation) {
                                            FontWeight.SemiBold
                                        } else {
                                            FontWeight.Normal
                                        },
                                    ),
                                    color = if (option == storageLocation) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                storageLocation         = option
                                storageLocationExpanded = false
                            },
                        )
                    }
                }
            }

            //Purchase Date
            OutlinedTextField(
                value         = epochMillisToDisplay(purchaseMillis),
                onValueChange = {},
                modifier      = Modifier.fillMaxWidth(),
                readOnly      = true,
                label         = { Text("Purchase date *") },
                trailingIcon  = {
                    IconButton(onClick = { showPurchasePicker = true }) {
                        Icon(
                            imageVector        = Icons.Outlined.CalendarToday,
                            contentDescription = "Pick purchase date",
                            tint               = SaveGreen,
                        )
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
            )

            // expiry Date
            OutlinedTextField(
                value         = epochMillisToDisplay(expiryMillis),
                onValueChange = {},
                modifier      = Modifier.fillMaxWidth(),
                readOnly      = true,
                label         = { Text("Expiry date *") },
                trailingIcon  = {
                    IconButton(onClick = { showExpiryPicker = true }) {
                        Icon(
                            imageVector        = Icons.Outlined.CalendarToday,
                            contentDescription = "Pick expiry date",
                            tint               = if (expiryError) MaterialTheme.colorScheme.error else SaveGreen,
                        )
                    }
                },
                isError        = expiryError,
                supportingText = if (expiryError) {
                    {
                        Text(
                            text  = "⚠ Expiry date cannot be in the past",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else null,
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
            )

            //Quantity
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value         = quantity,
                    onValueChange = { quantity = it },
                    modifier      = Modifier.weight(0.6f),
                    label         = { Text("Quantity *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                //exposed drop down menu box
                ExposedDropdownMenuBox(
                    expanded         = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier         = Modifier.weight(0.4f),
                ) {
                    OutlinedTextField(
                        value         = unit,
                        onValueChange = {},
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly      = true,
                        label         = { Text("Unit") },
                        trailingIcon  = {
                            Icon(
                                imageVector = if (unitExpanded) {
                                    Icons.Filled.ArrowDropUp
                                } else {
                                    Icons.Filled.ArrowDropDown
                                },
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        shape      = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded         = unitExpanded,
                        onDismissRequest = { unitExpanded = false },
                    ) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text    = { Text(option) },
                                onClick = {
                                    unit = option
                                    unitExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            //Notes field
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Notes (optional)") },
                placeholder   = {
                    Text(
                        text  = "Add storage tips or details...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                minLines = 3,
                shape    = RoundedCornerShape(12.dp),
            )

            //save button
            Button(
                onClick  = { if (isFormValid) onNavigateBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isFormValid,
                shape   = RoundedCornerShape(50),
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        .copy(alpha = 0.60f),
                    disabledContentColor   = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.38f),
                ),
            ) {
                Text(
                    text  = "Save Food Item",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// inline validation for the prototype

@Preview(name = "Light · Add (expiry error visible)", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewAddLight() {
    SmartPantryTheme {
        AddEditFoodScreen(
            isEditMode     = false,
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Light · Edit mode", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewEditLight() {
    SmartPantryTheme {
        AddEditFoodScreen(
            isEditMode     = true,
            onNavigateBack = {},
        )
    }
}

@Preview(
    name            = "Dark · Add",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAddDark() {
    SmartPantryTheme(darkTheme = true) {
        AddEditFoodScreen(
            isEditMode     = false,
            onNavigateBack = {},
        )
    }
}
