package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import kotlinx.coroutines.delay


private enum class PairingStep { SCANNING, DISCOVERED, CONFIGURING }

private enum class SignalStrength(
    val label: String,
    val color: Color,
) {
    STRONG("Strong signal",  Color(0xFF2E7D32)),
    MEDIUM("Medium signal",  Color(0xFFF57F17)),
    WEAK  ("Weak signal",    Color(0xFFBA1A1A)),
}

private data class DiscoveredSensor(
    val deviceId: String,
    val signal:   SignalStrength,
)

/**
 * Hardcoded the scan result of food item bar code
 */
private val discoveredSensors = listOf(
    DiscoveredSensor("SmartSense-A3F2", SignalStrength.STRONG),
    DiscoveredSensor("TempHumid-B891",  SignalStrength.MEDIUM),
    DiscoveredSensor("EnvMonitor-C204", SignalStrength.WEAK),
)

private enum class EnvironmentType(
    val label: String,
    val emoji: String,
    val description: String,
    val suggestedThresholdC: Float,
    val suggestedMaxLux: Float,
) {
    
    //Predefied thresholds
    REFRIGERATOR("Refrigerator", "🧊", "Sealed cooling unit, 1–7 °C",           7f,   50f),
    FREEZER     ("Freezer",      "❄️", "Sealed freezing, −20 to −15 °C",        -15f,  50f),
    PANTRY      ("Pantry",       "🌡", "Enclosed room-temp storage, 15–25 °C",  28f,  300f),
    CUPBOARD    ("Cupboard",     "🚪", "Enclosed cabinet, 18–28 °C",            25f,  100f),
    CELLAR      ("Cellar",       "🏚", "Cool underground space, 8–15 °C",       18f,   50f),
    OPEN_SHELF  ("Open Shelf",   "📦", "Exposed ambient storage",               35f,  800f),
    GARAGE      ("Garage",       "🏠", "Unregulated outdoor-adjacent space",    40f,  600f),
    CUSTOM      ("Custom",       "⚙️", "Define your own environment type",      30f,  500f),
}


private val SectionGreen = Color(0xFF0F6E56)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSensorScreen(onNavigateBack: () -> Unit) {
    var pairingStep    by remember { mutableStateOf(PairingStep.SCANNING) }
    var selectedDevice by remember { mutableStateOf<DiscoveredSensor?>(null) }
    var sensorName     by remember { mutableStateOf("") }
    var selectedType   by remember { mutableStateOf<EnvironmentType?>(null) }

    val isFormValid = sensorName.isNotBlank() && selectedType != null

    // Simulating the IoT scan
    LaunchedEffect(Unit) {
        delay(2_000L)
        if (pairingStep == PairingStep.SCANNING) pairingStep = PairingStep.DISCOVERED
    }

    val topBarTitle = when (pairingStep) {
        PairingStep.SCANNING    -> "Add New Sensor"
        PairingStep.DISCOVERED  -> "Select Sensor"
        PairingStep.CONFIGURING -> "Configure Sensor"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = topBarTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (pairingStep) {
                                // Go back one step instead of exiting
                                PairingStep.CONFIGURING -> pairingStep = PairingStep.DISCOVERED
                                else                    -> onNavigateBack()
                            }
                        },
                    ) {
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
        when (pairingStep) {
            PairingStep.SCANNING   -> ScanningContent(
                modifier = Modifier.padding(innerPadding),
            )
            PairingStep.DISCOVERED -> DiscoveredContent(
                modifier         = Modifier.padding(innerPadding),
                onDeviceSelected = { device ->
                    selectedDevice = device
                    sensorName     = ""     // reset form when a new device is chosen
                    selectedType   = null
                    pairingStep    = PairingStep.CONFIGURING
                },
            )
            PairingStep.CONFIGURING -> ConfiguringContent(
                modifier           = Modifier.padding(innerPadding),
                device             = selectedDevice!!,
                sensorName         = sensorName,
                onSensorNameChange = { sensorName = it },
                selectedType       = selectedType,
                onTypeSelected     = { selectedType = it },
                isFormValid        = isFormValid,
                onPair             = onNavigateBack,
            )
        }
    }
}

//Scanning
@Composable
private fun ScanningContent(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color    = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "Searching for nearby sensors…",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = "Make sure your sensor is powered on\nand within range",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

//Discovered
@Composable
private fun DiscoveredContent(
    onDeviceSelected: (DiscoveredSensor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text  = "${discoveredSensors.size} SENSORS FOUND NEARBY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
            color = SectionGreen,
        )

        Spacer(Modifier.height(4.dp))

        discoveredSensors.forEach { sensor ->
            DiscoveredSensorCard(
                sensor  = sensor,
                onClick = { onDeviceSelected(sensor) },
            )
        }
    }
}

//Configuring
@Composable
private fun ConfiguringContent(
    device: DiscoveredSensor,
    sensorName: String,
    onSensorNameChange: (String) -> Unit,
    selectedType: EnvironmentType?,
    onTypeSelected: (EnvironmentType) -> Unit,
    isFormValid: Boolean,
    onPair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

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
                    text  = "Pairing with: ${device.deviceId}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        //Sensor name
        OutlinedTextField(
            value         = sensorName,
            onValueChange = onSensorNameChange,
            modifier      = Modifier.fillMaxWidth(),
            label         = { Text("Sensor name *") },
            placeholder   = {
                Text(
                    text  = "e.g. Kitchen Fridge, Basement Pantry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector        = Icons.Outlined.Sensors,
                    contentDescription = null,
                )
            },
            singleLine = true,
            shape      = RoundedCornerShape(12.dp),
        )

        //Environment types
        Text(
            text     = "ENVIRONMENT TYPE *",
            style    = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
            color    = SectionGreen,
            modifier = Modifier.padding(top = 4.dp),
        )

        // 2 column layout
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
                            onClick  = { onTypeSelected(type) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowTypes.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        //Pair Sensor button
        Button(
            onClick        = { if (isFormValid) onPair() },
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
                text  = "Pair Sensor",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

//Discovered sensor card

@Composable
private fun DiscoveredSensorCard(
    sensor:   DiscoveredSensor,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Sensors,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp),
                        tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = sensor.deviceId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = sensor.signal.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = sensor.signal.color,
                    )
                }
            }

            //signal strength bars
            SignalBars(strength = sensor.signal)
        }
    }
}

//Signal strength bar indicator


@Composable
private fun SignalBars(
    strength: SignalStrength,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment     = Alignment.Bottom,
    ) {
        val lit = strength.color
        val dim = lit.copy(alpha = 0.20f)

        listOf(
            6.dp  to true,
            10.dp to (strength != SignalStrength.WEAK),
            14.dp to (strength == SignalStrength.STRONG),
        ).forEach { (barHeight, active) ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .background(
                        color = if (active) lit else dim,
                        shape = RoundedCornerShape(1.dp),
                    ),
            )
        }
    }
}

//Environment type cards

/**
 * Selectable cards for EnvironmentType .
 */
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

@Preview(name = "Light — Scanning", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewScanningLight() {
    SmartPantryTheme { AddSensorScreen(onNavigateBack = {}) }
}

@Preview(
    name            = "Dark — Scanning",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewScanningDark() {
    SmartPantryTheme(darkTheme = true) { AddSensorScreen(onNavigateBack = {}) }
}

@Preview(name = "Light — Discovered", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewDiscoveredLight() {
    SmartPantryTheme {
        DiscoveredContent(onDeviceSelected = {})
    }
}

@Preview(
    name            = "Dark — Discovered",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewDiscoveredDark() {
    SmartPantryTheme(darkTheme = true) {
        DiscoveredContent(onDeviceSelected = {})
    }
}

@Preview(name = "Light — Configuring", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewConfiguringLight() {
    SmartPantryTheme {
        ConfiguringContent(
            device             = DiscoveredSensor("SmartSense-A3F2", SignalStrength.STRONG),
            sensorName         = "",
            onSensorNameChange = {},
            selectedType       = EnvironmentType.REFRIGERATOR,
            onTypeSelected     = {},
            isFormValid        = false,
            onPair             = {},
        )
    }
}

@Preview(
    name            = "Dark — Configuring",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewConfiguringDark() {
    SmartPantryTheme(darkTheme = true) {
        ConfiguringContent(
            device             = DiscoveredSensor("SmartSense-A3F2", SignalStrength.STRONG),
            sensorName         = "Kitchen Fridge",
            onSensorNameChange = {},
            selectedType       = EnvironmentType.REFRIGERATOR,
            onTypeSelected     = {},
            isFormValid        = true,
            onPair             = {},
        )
    }
}
