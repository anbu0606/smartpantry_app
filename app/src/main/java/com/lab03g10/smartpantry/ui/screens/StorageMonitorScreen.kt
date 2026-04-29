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
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// ── Chart colour
private val NormalGreen    = Color(0xFF2E7D32)
private val AlertRed       = Color(0xFFBA1A1A)
private val AlertRedHalo   = Color(0x33BA1A1A)   // 20 % opacity
private val ThresholdOrange = Color(0xFFF57F17)
private val GridLine       = Color(0xFFE0E0E0)

private data class LocationConfig(
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

private val locations = listOf(
    LocationConfig(
        name                   = "Fridge",
        emoji                  = "🧊",
        currentTemp            = 4.2f,
        currentHumidity        = 58f,
        currentLux             = 12f,
        isNormal               = true,
        tempReadings           = floatArrayOf(3.8f, 4.0f, 4.1f, 3.9f, 4.2f, 4.0f, 8.5f, 4.1f),
        humidityReadings       = floatArrayOf(56f, 57f, 58f, 57f, 58f, 59f, 65f, 58f),
        luxReadings            = floatArrayOf(5f, 8f, 45f, 6f, 4f, 8f, 180f, 12f),
        timeLabels             = listOf("08h", "10h", "12h", "14h", "16h", "18h", "20h", "22h"),
        alertThreshold         = 6.0f,
        humidityAlertThreshold = 70f,
        luxAlertThreshold      = 50f,
        chartMinTemp           = 2.0f,
        chartMaxTemp           = 10.0f,
    ),
    LocationConfig(
        name                   = "Pantry",
        emoji                  = "🌡",
        currentTemp            = 26.5f,
        currentHumidity        = 63f,
        currentLux             = 285f,
        isNormal               = false,
        tempReadings           = floatArrayOf(22.0f, 23.5f, 24.0f, 25.2f, 26.5f, 29.5f, 28.0f, 26.5f),
        humidityReadings       = floatArrayOf(60f, 62f, 63f, 65f, 63f, 72f, 66f, 63f),
        luxReadings            = floatArrayOf(120f, 145f, 180f, 230f, 280f, 420f, 290f, 285f),
        timeLabels             = listOf("08h", "10h", "12h", "14h", "16h", "18h", "20h", "22h"),
        alertThreshold         = 28.0f,
        humidityAlertThreshold = 70f,
        luxAlertThreshold      = 400f,
        chartMinTemp           = 18.0f,
        chartMaxTemp           = 35.0f,
    ),
    LocationConfig(
        name                   = "Cupboard",
        emoji                  = "🚪",
        currentTemp            = 21.0f,
        currentHumidity        = 45f,
        currentLux             = 38f,
        isNormal               = true,
        tempReadings           = floatArrayOf(20.5f, 21.0f, 20.8f, 21.2f, 21.0f, 20.5f, 21.5f, 21.0f),
        humidityReadings       = floatArrayOf(44f, 45f, 45f, 46f, 45f, 44f, 46f, 45f),
        luxReadings            = floatArrayOf(20f, 25f, 18f, 42f, 35f, 30f, 45f, 38f),
        timeLabels             = listOf("08h", "10h", "12h", "14h", "16h", "18h", "20h", "22h"),
        alertThreshold         = 25.0f,
        humidityAlertThreshold = 60f,
        luxAlertThreshold      = 100f,
        chartMinTemp           = 18.0f,
        chartMaxTemp           = 30.0f,
    ),
)

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
    onNavigateToSensorDetail: () -> Unit = {},
) {
    var selectedTab       by remember { mutableIntStateOf(0) }
    var selectedMetricTab by remember { mutableIntStateOf(0) }
    val location = locations[selectedTab]

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
                    IconButton(onClick = onNavigateToSensorDetail) {
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
                selectedTabIndex = selectedTab,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = MaterialTheme.colorScheme.primary,
                indicator        = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                locations.forEachIndexed { index, loc ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                text  = "${loc.emoji} ${loc.name}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) {
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
                    val stepX    = w / (count - 1).toFloat()

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

                        val normalEndIdx = if (hasSpike) spikeIndex else count - 1
                        if (normalEndIdx >= 1) {
                            val normalPath = Path().apply {
                                moveTo(pts[0].x, pts[0].y)
                                for (i in 1..normalEndIdx) {
                                    lineTo(pts[i].x, pts[i].y)
                                }
                            }
                            drawPath(
                                path  = normalPath,
                                color = NormalGreen,
                                style = Stroke(width = 2.5f),
                            )
                        }

                        if (hasSpike && spikeIndex > 0) {
                            val alertPath = Path().apply {
                                moveTo(pts[spikeIndex - 1].x, pts[spikeIndex - 1].y)
                                for (i in spikeIndex until count) {
                                    lineTo(pts[i].x, pts[i].y)
                                }
                            }
                            drawPath(
                                path  = alertPath,
                                color = AlertRed,
                                style = Stroke(width = 2.5f),
                            )

                            val spikePt = pts[spikeIndex]
                            drawCircle(AlertRedHalo, radius = 20f, center = spikePt)
                            drawCircle(AlertRed,     radius = 6f,  center = spikePt)
                        }

                        pts.forEachIndexed { i, pt ->
                            val dotColor = if (hasSpike && i >= spikeIndex) AlertRed else NormalGreen
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
