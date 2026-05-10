package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

private val SectionGreen = Color(0xFF0F6E56)
private val DeleteRed    = Color(0xFFBA1A1A)

/**
 * Edit screen for an existing paired sensors
 * pre-populated values for the Pantry sensor prototype
 * Allowing the user to rename the sensor and change its environment type
 * adjust the alert threshold, save changes, or remove the sensor entirely
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSensorScreen(
    sensorId: String? = null,
    onNavigateBack: () -> Unit
) {
    var sensorName       by remember { mutableStateOf("Pantry") }
    var selectedType     by remember { mutableStateOf(EnvironmentType.PANTRY) }
    var threshold        by remember { mutableFloatStateOf(28f) }
    var luxThreshold     by remember { mutableFloatStateOf(selectedType.suggestedMaxLux) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isFormValid = sensorName.isNotBlank()

    // removal confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title  = { Text("Remove Sensor") },
            text   = {
                Text(
                    "Remove TempHumid-B891 from Smart Pantry? All stored readings will be " +
                        "deleted. This cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    onNavigateBack() 
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Edit Sensor",
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
                .padding(start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            //Device chip
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Sensors,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text  = "TempHumid-B891",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            // Sensor name field
            OutlinedTextField(
                value         = sensorName,
                onValueChange = { sensorName = it },
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Sensor name *") },
                leadingIcon   = {
                    Icon(
                        imageVector        = Icons.Outlined.Sensors,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
            )

            //Environment type section
            Text(
                text     = "ENVIRONMENT TYPE",
                style    = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                color    = SectionGreen,
                modifier = Modifier.padding(top = 4.dp),
            )

            // 2 column environment type cards
            val typeList = EnvironmentType.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                typeList.chunked(2).forEach { rowTypes ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowTypes.forEach { type ->
                            EnvironmentTypeCard(
                                type     = type,
                                selected = selectedType == type,
                                onClick  = {
                                    selectedType = type
                                    threshold    = type.suggestedThresholdC
                                    luxThreshold = type.suggestedMaxLux
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowTypes.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // alert threshold section
            Text(
                text     = "ALERT THRESHOLD",
                style    = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                color    = SectionGreen,
                modifier = Modifier.padding(top = 4.dp),
            )

            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = "Alert above",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text  = "${threshold.toInt()} °C",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = SectionGreen,
                        )
                    }

                    Slider(
                        value         = threshold,
                        onValueChange = { threshold = it },
                        valueRange    = -20f..50f,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = SliderDefaults.colors(
                            thumbColor         = MaterialTheme.colorScheme.primary,
                            activeTrackColor   = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    )

                    Text(
                        text  = "Suggested for ${selectedType.label}: " +
                                "${selectedType.suggestedThresholdC.toInt()} °C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // light alert threshold section
            Text(
                text     = "LIGHT ALERT THRESHOLD",
                style    = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                color    = SectionGreen,
                modifier = Modifier.padding(top = 4.dp),
            )

            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = "Alert above",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text  = "${luxThreshold.toInt()} lx",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = SectionGreen,
                        )
                    }

                    Slider(
                        value         = luxThreshold,
                        onValueChange = { luxThreshold = it },
                        valueRange    = 0f..1000f,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = SliderDefaults.colors(
                            thumbColor         = MaterialTheme.colorScheme.primary,
                            activeTrackColor   = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    )

                    Text(
                        text  = "Suggested for ${selectedType.label}: " +
                                "${selectedType.suggestedMaxLux.toInt()} lx",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            //save button
            Button(
                onClick        = { if (isFormValid) onNavigateBack() },
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
                enabled        = isFormValid,
                shape          = RoundedCornerShape(50),
                colors         = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        .copy(alpha = 0.60f),
                    disabledContentColor   = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.38f),
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    text  = "Save Changes",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            // delete sensor button
            OutlinedButton(
                onClick  = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(50),
                border   = BorderStroke(1.dp, DeleteRed),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = DeleteRed,
                ),
            ) {
                Text(
                    text  = "Remove Sensor",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// environment type card

@Composable
private fun EnvironmentTypeCard(
    type:     EnvironmentType,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) MaterialTheme.colorScheme.primaryContainer
                   else MaterialTheme.colorScheme.surfaceContainerLow,
        border   = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = type.emoji, fontSize = 22.sp)
            Text(
                text  = type.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = type.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

// previews

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewEditSensorLight() {
    SmartPantryTheme {
        EditSensorScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewEditSensorDark() {
    SmartPantryTheme(darkTheme = true) {
        EditSensorScreen(onNavigateBack = {})
    }
}
