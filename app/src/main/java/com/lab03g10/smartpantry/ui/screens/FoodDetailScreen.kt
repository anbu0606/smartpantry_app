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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.FoodDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit


private val SectionLabelGreen = Color(0xFF0F6E56)
private val EditGreen         = Color(0xFF2E7D32)
private val DaysGreen         = Color(0xFF2E7D32)
private val DaysAmber         = Color(0xFFF57F17)


private data class ReasonOption(val code: String, val label: String, val description: String)

private val ReasonOptions = listOf(
    ReasonOption("EXPIRED", "Expired", "Past its expiry date"),
    ReasonOption("SPOILED", "Spoiled", "Bad smell, mould, or visibly off"),
    ReasonOption("OTHER",   "Other",   "Some other reason"),
)

private val detailDateFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showReasonPicker by remember { mutableStateOf(false) }
    val item = state.item
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
                    TextButton(onClick = onNavigateToEdit,
                        enabled = item != null && foodId != null,) {
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
        when {
            state.isLoading -> {
                Text(
                    text = "Loading food item...",
                    modifier = Modifier.padding(innerPadding).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item == null -> {
                Text(
                    text = "This food item is no longer in your pantry.",
                    modifier = Modifier.padding(innerPadding).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            else -> {
                FoodDetailContent(
                    item = item,
                    storageLocation = state.storageLocation,
                    onMarkConsumed = {
                        viewModel.markConsumed()
                        onNavigateBack()
                    },
                    onMarkWasted = { showReasonPicker = true },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (showReasonPicker && item != null) {
        AlertDialog(
            onDismissRequest = { showReasonPicker = false },
            title = {
                Text(
                    text = "Why are you discarding this?",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReasonOptions.forEach { option ->
                        TextButton(
                            onClick = {
                                viewModel.markWasted(option.code)
                                showReasonPicker = false
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                                Text(
                                    text = option.description,
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
@Composable
private fun FoodDetailContent(
    item: FoodItemEntity,
    storageLocation: SensorConfigEntity?,
    onMarkConsumed: () -> Unit,
    onMarkWasted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysUntilExpiry = daysUntil(item.effectiveExpiryDate)
    val expiringSoon = daysUntilExpiry in 1..3
    val daysColor = when {
        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.error
        daysUntilExpiry <= 3 -> DaysAmber
        else -> DaysGreen
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryChip(label = item.category)
                            CategoryChip(label = storageLocation?.displayName ?: "Unmonitored")
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = daysUntilExpiry.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Light,
                            color = daysColor,
                        )
                        Text(
                            text = "days left",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item { SectionLabel(text = "DETAILS") }
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                DetailRow(label = "Quantity", value = "${item.quantity} ${item.unit}")
                HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                DetailRow(label = "Purchase date", value = detailDateFmt.format(Date(item.purchaseDate)))
                HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                DetailRow(
                    label = "Expiry date",
                    value = detailDateFmt.format(Date(item.effectiveExpiryDate)),
                    valueColor = when {
                        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.error
                        expiringSoon -> DaysAmber
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                DetailRow(label = "Storage", value = storageLocation?.displayName ?: "Unmonitored")
                item.barcode?.let {
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Barcode", value = it)
                }
            }
        }

        item { SectionLabel(text = "NOTES") }
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Text(
                    text = item.notes ?: "No notes added.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }

        if (storageLocation != null) {
            item { SectionLabel(text = "STORAGE ENVIRONMENT") }
            item {
                TemperatureCard(
                    locationName = storageLocation.displayName,
                    temperature = storageLocation.thresholdC,
                    humidity = 60f,
                    lux = storageLocation.maxLux,
                    isNormal = true,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onMarkConsumed,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Mark as Consumed",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                OutlinedButton(
                    onClick = onMarkWasted,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(
                        text = "Mark as Wasted",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
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

private fun daysUntil(expiryMillis: Long): Int {
    return TimeUnit.MILLISECONDS
        .toDays(expiryMillis - System.currentTimeMillis())
        .toInt()
        .coerceAtLeast(0)
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewFoodDetailLight() {
    SmartPantryTheme {
        Text("Food detail preview uses live app data at runtime.")

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
        Text("Food detail preview uses live app data at runtime.")
    }
}
