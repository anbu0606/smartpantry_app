package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.LocationLiveData
import com.lab03g10.smartpantry.ui.viewmodel.StorageMonitorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Chart colour
private val NormalGreen    = Color(0xFF2E7D32)
private val AlertRed       = Color(0xFFBA1A1A)
private val AlertRedHalo   = Color(0x33BA1A1A)   // 20 % opacity
private val ThresholdOrange = Color(0xFFF57F17)
private val GridLine       = Color(0xFFE0E0E0)

private data class LocationConfig(
    val sensorId: String,
    val name: String,
    val emoji: String,
    val currentTemp: Float,
    val currentHumidity: Float,
    val currentLux: Float,
    val isNormal: Boolean,
    val tempReadings: FloatArray,       // historical values(newest last)
    val humidityReadings: FloatArray,
    val luxReadings: FloatArray,
    val timeLabels: List<String>,       // x-axis
    val alertThreshold: Float,
    val humidityAlertThreshold: Float,
    val luxAlertThreshold: Float,
    val chartMinTemp: Float,
    val chartMaxTemp: Float,
)

// Humidity isn't tracked on SensorConfigEntity; per-environment defaults
// approximate what the A2 prototype used to hardcode. Move to the entity if
// users need to customise per-sensor.
private val DEFAULT_HUMIDITY_THRESHOLDS: Map<EnvironmentType, Float> = mapOf(
    EnvironmentType.REFRIGERATOR to 70f,
    EnvironmentType.FREEZER      to 70f,
    EnvironmentType.PANTRY       to 70f,
    EnvironmentType.CUPBOARD     to 60f,
    EnvironmentType.CELLAR       to 75f,
    EnvironmentType.OPEN_SHELF   to 70f,
    EnvironmentType.GARAGE       to 80f,
    EnvironmentType.CUSTOM       to 70f,
)

// HH:mm so multiple readings within an hour aren't all labelled the same.
private val chartTimeLabelFmt = SimpleDateFormat("HH:mm", Locale.ENGLISH)

/** Down-sample [readings] to at most [target] evenly-spaced HH:mm labels.
 *  Without this, N readings → N labels, which `Arrangement.SpaceBetween`
 *  jams into the row width as a wall of overlapping text. */
private fun sampledTimeLabels(readings: List<SensorReadingEntity>, target: Int = 5): List<String> {
    if (readings.isEmpty()) return emptyList()
    val n = readings.size.coerceAtMost(target)
    if (n == 1) return listOf(chartTimeLabelFmt.format(Date(readings.first().timestamp)))
    val step = (readings.size - 1).toFloat() / (n - 1).toFloat()
    return (0 until n).map { i ->
        val idx = (i * step).toInt().coerceAtMost(readings.lastIndex)
        chartTimeLabelFmt.format(Date(readings[idx].timestamp))
    }
}

/** Adapts the VM's `LocationLiveData` to the existing `LocationConfig` view
 *  shape so the chart / tab / TemperatureCard code below doesn't have to be
 *  rewritten. Charts read 0/empty when there's no history yet. */
private fun LocationLiveData.toViewConfig(): LocationConfig {
    val temps = recentReadings.map { it.temperatureC }.toFloatArray()
    val hums  = recentReadings.map { it.humidity }.toFloatArray()
    val luxes = recentReadings.map { it.lux }.toFloatArray()
    val timeLabels = sampledTimeLabels(recentReadings)

    // Chart y-axis: wide enough to always show the alert threshold, with a
    // little padding around the actual data range.
    val tempMin = (temps.minOrNull() ?: thresholdC).coerceAtMost(thresholdC) - 2f
    val tempMax = (temps.maxOrNull() ?: thresholdC).coerceAtLeast(thresholdC) + 2f

    val humidityThreshold = DEFAULT_HUMIDITY_THRESHOLDS[environmentType] ?: 70f

    return LocationConfig(
        sensorId               = sensorId,
        name                   = displayName,
        emoji                  = environmentType.emoji,
        currentTemp            = latestReading?.temperatureC ?: 0f,
        currentHumidity        = latestReading?.humidity ?: 0f,
        currentLux             = latestReading?.lux ?: 0f,
        // breachFlag 0 = clean reading. Null reading (worker hasn't fired yet)
        // shows as normal so the card doesn't false-alarm on first launch.
        isNormal               = (latestReading?.breachFlag ?: 0) == 0,
        tempReadings           = temps,
        humidityReadings       = hums,
        luxReadings            = luxes,
        timeLabels             = timeLabels,
        alertThreshold         = thresholdC,
        humidityAlertThreshold = humidityThreshold,
        luxAlertThreshold      = maxLux,
        chartMinTemp           = tempMin,
        chartMaxTemp           = tempMax,
    )
}

//Metric labels

private val metricTabs = listOf("🌡 Temperature", "💧 Humidity", "☀ Light")


/**
 * Storage Monitor screen - shows live and historical sensor data for every
 * configured storage location Fridge, Pantry, Cupboard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageMonitorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSensorDetail: (String) -> Unit = {},
    viewModel: StorageMonitorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Adapt the VM's LocationLiveData to the existing LocationConfig view
    // shape (charts / tabs / cards keep their current signatures).
    val locations = remember(state.locations) {
        state.locations.map { it.toViewConfig() }
    }

    if (locations.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Storage Monitor", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            EmptyState(
                icon           = Icons.Outlined.Sensors,
                title          = "No sensors paired yet",
                supportingText = "Pair a sensor from the Dashboard to start monitoring storage conditions.",
                modifier       = Modifier.padding(innerPadding),
            )
        }
        return
    }

    var selectedTab       by remember { mutableIntStateOf(0) }
    var selectedMetricTab by remember { mutableIntStateOf(0) }
    // Clamp in case the list shrinks (e.g. user deletes the sensor they were
    // viewing). Without this, locations[selectedTab] would throw.
    val safeIndex = selectedTab.coerceIn(0, locations.lastIndex)
    val location = locations[safeIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Storage Monitor",
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
                    IconButton(onClick = { onNavigateToSensorDetail(location.sensorId) }) {
                        Icon(
                            imageVector        = Icons.Outlined.Settings,
                            contentDescription = "Sensor settings",
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
                .padding(innerPadding),
        ) {
            //Location tab row
            TabRow(
                selectedTabIndex = safeIndex,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = MaterialTheme.colorScheme.primary,
                indicator        = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                        color    = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                locations.forEachIndexed { index, loc ->
                    Tab(
                        selected = safeIndex == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                text  = "${loc.emoji} ${loc.name}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (safeIndex == index) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    },
                                ),
                            )
                        },
                    )
                }
            }

            // location content
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = 20.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    TabRow(
                        selectedTabIndex = selectedMetricTab,
                        containerColor   = MaterialTheme.colorScheme.background,
                        contentColor     = MaterialTheme.colorScheme.primary,
                        indicator        = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMetricTab]),
                                color    = MaterialTheme.colorScheme.primary,
                            )
                        },
                    ) {
                        metricTabs.forEachIndexed { index, label ->
                            Tab(
                                selected = selectedMetricTab == index,
                                onClick  = { selectedMetricTab = index },
                                text     = {
                                    Text(
                                        text  = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selectedMetricTab == index) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            fontSize = 12.sp,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }

                item {
                    TemperatureCard(
                        locationName = location.name,
                        temperature  = location.currentTemp,
                        humidity     = location.currentHumidity,
                        lux          = location.currentLux,
                        isNormal     = location.isNormal,
                    )
                }

                // historical sensor chart
                item {
                    HistoricalChartCard(location = location, selectedMetricTab = selectedMetricTab)
                }
            }
        }
    }
}

//Historical chart card
@Composable
private fun HistoricalChartCard(location: LocationConfig, selectedMetricTab: Int) {
    val readings: FloatArray
    val threshold: Float
    val chartMin: Float
    val chartMax: Float
    val title: String
    val thresholdLabel: String
    val unit: String

    when (selectedMetricTab) {
        1 -> {
            readings        = location.humidityReadings
            threshold       = location.humidityAlertThreshold
            chartMin        = 0f
            chartMax        = 100f
            title           = "Humidity History"
            thresholdLabel  = "${location.humidityAlertThreshold.toInt()}%"
            unit            = "%"
        }
        2 -> {
            readings        = location.luxReadings
            threshold       = location.luxAlertThreshold
            chartMin        = 0f
            chartMax        = (location.luxAlertThreshold * 1.5f).coerceAtLeast(200f)
            title           = "Light History"
            thresholdLabel  = "${location.luxAlertThreshold.toInt()} lx"
            unit            = "lx"
        }
        else -> {
            //Temperature
            readings        = location.tempReadings
            threshold       = location.alertThreshold
            chartMin        = location.chartMinTemp
            chartMax        = location.chartMaxTemp
            title           = "Temperature History"
            thresholdLabel  = "${location.alertThreshold.toInt()}°C"
            unit            = "°C"
        }
    }

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "Alert threshold: $thresholdLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            // Chart and  threshold labe
            Box(modifier = Modifier.fillMaxWidth()) {
                SensorLineChart(
                    readings   = readings,
                    threshold  = threshold,
                    chartMin   = chartMin,
                    chartMax   = chartMax,
                    timeLabels = location.timeLabels,
                    modifier   = Modifier.fillMaxWidth(),
                )

                // Threshold label
                val thresholdFraction =
                    1f - (threshold - chartMin) / (chartMax - chartMin)
                Text(
                    text     = thresholdLabel,
                    style    = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp,
                    ),
                    color    = ThresholdOrange,
                    modifier = Modifier.padding(top = (180.dp * thresholdFraction - 8.dp)),
                )
            }
        }
    }
}

//Canvas line chart
/**
 * Generalised sensor line chart drawn with drawWithCache.
 */
@Composable
private fun SensorLineChart(
    readings: FloatArray,
    threshold: Float,
    chartMin: Float,
    chartMax: Float,
    timeLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    val valueRange = chartMax - chartMin
    val spikeIndex = readings.indexOfFirst { it > threshold }
    val hasSpike   = spikeIndex >= 0

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .drawWithCache {
                    val w        = size.width
                    val h        = size.height
                    val count    = readings.size
                    // Single-reading series → stepX collapses to 0 so the dot
                    // anchors at x=0 instead of dividing by zero.
                    val stepX    = if (count > 1) w / (count - 1).toFloat() else 0f

                    fun valueToY(v: Float) = h * (1f - (v - chartMin) / valueRange)
                    fun xAt(i: Int)        = i * stepX

                    val pts = readings.mapIndexed { i, v -> Offset(xAt(i), valueToY(v)) }

                    onDrawBehind {
                        // Horizontal grid line
                        repeat(5) { i ->
                            val y = h * i / 4f
                            drawLine(
                                color       = GridLine,
                                start       = Offset(0f, y),
                                end         = Offset(w, y),
                                strokeWidth = 1f,
                            )
                        }

                        // Dashed threshold line
                        val threshY = valueToY(threshold)
                        drawLine(
                            color       = ThresholdOrange,
                            start       = Offset(0f, threshY),
                            end         = Offset(w, threshY),
                            strokeWidth = 2f,
                            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
                        )

                        // Per-segment colouring. Each segment between adjacent
                        // points is red iff EITHER endpoint exceeds threshold;
                        // green otherwise. Previous logic used
                        // indexOfFirst { > threshold } and painted everything
                        // after that index red — which meant a sawtooth that
                        // crossed in and out stayed red on the safe stretches,
                        // and any second breach got no visual treatment.
                        if (count >= 2) {
                            for (i in 0 until count - 1) {
                                val segIsBreach = readings[i] > threshold
                                    || readings[i + 1] > threshold
                                drawLine(
                                    color       = if (segIsBreach) AlertRed else NormalGreen,
                                    start       = pts[i],
                                    end         = pts[i + 1],
                                    strokeWidth = 2.5f,
                                )
                            }
                        }

                        // Halo on the FIRST breach point — single attention
                        // marker for "this is when it crossed the line".
                        if (hasSpike) {
                            val spikePt = pts[spikeIndex]
                            drawCircle(AlertRedHalo, radius = 20f, center = spikePt)
                            drawCircle(AlertRed,     radius = 6f,  center = spikePt)
                        }

                        // Dot colour also per-value, not "everything after the
                        // first spike".
                        pts.forEachIndexed { i, pt ->
                            val dotColor = if (readings[i] > threshold) AlertRed else NormalGreen
                            drawCircle(Color.White, radius = 5f, center = pt)
                            drawCircle(dotColor,    radius = 3f, center = pt)
                        }
                    }
                },
        )

        Spacer(Modifier.height(8.dp))

        // x-axis label
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            timeLabels.forEach { label ->
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@Preview(name = "Light - Fridge tab", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewStorageLight() {
    SmartPantryTheme {
        StorageMonitorScreen(onNavigateBack = {}, onNavigateToSensorDetail = {})
    }
}

@Preview(
    name            = "Dark - Fridge tab",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewStorageDark() {
    SmartPantryTheme(darkTheme = true) {
        StorageMonitorScreen(onNavigateBack = {}, onNavigateToSensorDetail = {})
    }
}
