package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme


private val SectionLabelGreen = Color(0xFF0F6E56)
private val EditGreen         = Color(0xFF2E7D32)
private val DaysGreen         = Color(0xFF2E7D32)
private val DaysAmber         = Color(0xFFF57F17)

// Reason options shown in the "Mark as Wasted" picker.
// `code` mirrors WasteLogEntity.reason ("EXPIRED" / "SPOILED" / "OTHER") —
// Anbu Day 4 will pass it to viewModel.markWasted(code) when wiring the dialog onClick.
private data class ReasonOption(val code: String, val label: String, val description: String)

private val ReasonOptions = listOf(
    ReasonOption("EXPIRED", "Expired", "Past its expiry date"),
    ReasonOption("SPOILED", "Spoiled", "Bad smell, mould, or visibly off"),
    ReasonOption("OTHER",   "Other",   "Some other reason"),
)

//Hardcoded data for the prototype
private const val ItemName           = "Full Cream Milk"
private const val ItemCategory       = "Dairy"
private const val ItemQuantity       = "1 L"
private const val ItemStorageLocation = "Fridge"
private const val ItemPurchaseDate   = "16 Apr 2026"
private const val ItemExpiryDate     = "19 Apr 2026"
private const val ItemDaysUntilExpiry = 3
private const val ItemIsExpired      = false
private const val ItemNotes          = "Store in coldest part of fridge, away from door"


/**
 * Detail view for a single pantry item, showing hero expiry countdown
 * @param onNavigateBack   Called by the top-bar back arrow
 * @param onNavigateToEdit Called by the top-bar "Edit" text button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
) {
    val expiringSoon = !ItemIsExpired && ItemDaysUntilExpiry in 1..3
    val daysColor = when {
        ItemIsExpired || ItemDaysUntilExpiry <= 0 -> MaterialTheme.colorScheme.error
        ItemDaysUntilExpiry <= 3                  -> DaysAmber
        else                                      -> DaysGreen
    }

    // Controls visibility of the reason picker shown when the user taps "Mark as Wasted".
    // Anbu Day 4 will replace the onClick stubs inside the dialog with viewModel.markWasted(code).
    var showReasonPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Food Details",
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
                    TextButton(onClick = onNavigateToEdit) {
                        Text(
                            text  = "Edit",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = EditGreen,
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
        LazyColumn(
            modifier            = Modifier.padding(innerPadding),
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // item 1
            item {
                ElevatedCard(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier            = Modifier.weight(1f),
                        ) {
                            Text(
                                text  = ItemName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CategoryChip(label = "🥛 $ItemCategory")
                                CategoryChip(label = "🧊 $ItemStorageLocation")
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text  = ItemDaysUntilExpiry.toString(),
                                fontSize   = 40.sp,
                                fontWeight = FontWeight.Light,
                                color      = daysColor,
                            )
                            Text(
                                text  = "days left",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight    = FontWeight.Medium,
                                    letterSpacing = 1.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            //Item 2 Details section
            item {
                SectionLabel(text = "DETAILS")
            }
            item {
                ElevatedCard(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                    DetailRow(label = "Quantity",      value = ItemQuantity)
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Purchase date", value = ItemPurchaseDate)
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(
                        label      = "Expiry date",
                        value      = ItemExpiryDate,
                        valueColor = when {
                            ItemIsExpired  -> MaterialTheme.colorScheme.error
                            expiringSoon   -> DaysAmber
                            else           -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Storage", value = "🧊 $ItemStorageLocation")
                }
            }

            // Item 3 Notes section
            item {
                SectionLabel(text = "NOTES")
            }
            item {
                ElevatedCard(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Text(
                        text     = ItemNotes,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }

            //Item 4 - Storage environment section
            item {
                SectionLabel(text = "STORAGE ENVIRONMENT")
            }
            item {
                TemperatureCard(
                    locationName = "Fridge",
                    temperature  = 4.2f,
                    humidity     = 58f,
                    lux          = 12f,
                    isNormal     = true,
                )
            }

            // Item 5 - Action buttons
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        // TODO(Anbu Day 4): viewModel.markConsumed() then onNavigateBack().
                        // Sets FoodItemEntity.consumed = true; does NOT write to WasteLog
                        // (waste-only by design — see day2-room-di-checklist §1.5).
                        onClick  = {},
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape  = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text  = "Mark as Consumed",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    OutlinedButton(
                        // Opens the reason picker dialog below. Anbu Day 4 will replace
                        // each dialog option's onClick with viewModel.markWasted(opt.code).
                        onClick  = { showReasonPicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape  = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            text  = "Mark as Wasted",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }
    }

    // Reason picker shown when the user taps "Mark as Wasted". Prototype-only:
    // dialog onClick currently just closes the picker; Anbu Day 4 wires the VM call.
    if (showReasonPicker) {
        AlertDialog(
            onDismissRequest = { showReasonPicker = false },
            title = {
                Text(
                    text  = "Why are you discarding this?",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReasonOptions.forEach { opt ->
                        TextButton(
                            // TODO(Anbu Day 4): viewModel.markWasted(opt.code) ->
                            //   wasteRepo.log(WasteLogEntity(reason = opt.code, ...))
                            //   + foodRepo.markWasted(id); then showReasonPicker = false + onNavigateBack().
                            onClick  = { showReasonPicker = false },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text  = opt.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                                Text(
                                    text  = opt.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReasonPicker = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

// section label

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.sp,
        ),
        color = SectionLabelGreen,
    )
}

// detail row

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = if (valueColor == Color.Unspecified) {
                MaterialTheme.colorScheme.onSurface
            } else {
                valueColor
            },
        )
    }
}

// category
@Composable
private fun CategoryChip(label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}


@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewFoodDetailLight() {
    SmartPantryTheme {
        FoodDetailScreen(
            onNavigateBack   = {},
            onNavigateToEdit = {},
        )
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewFoodDetailDark() {
    SmartPantryTheme(darkTheme = true) {
        FoodDetailScreen(
            onNavigateBack   = {},
            onNavigateToEdit = {},
        )
    }
}
