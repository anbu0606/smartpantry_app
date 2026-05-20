
package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.model.EnvironmentType
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.SensorDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// chart colour
private val NormalGreen     = Color(0xFF2E7D32)
private val AlertRed        = Color(0xFFBA1A1A)
private val AlertRedHalo    = Color(0x33BA1A1A)
private val ThresholdOrange = Color(0xFFF57F17)
private val GridLine        = Color(0xFFE0E0E0)

private val SectionGreen = Color(0xFF0F6E56)

// Display formatters — JVM-local TZ (gap-closure-checklist §10.2).
private val chartAxisFmt   = SimpleDateFormat("MMM dd", Locale.ENGLISH)
private val alertTimeFmt   = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
private val pairedDateFmt  = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

// Humidity is always reported as a percentage; lux/temperature bounds are
// derived from the data + the sensor's configured thresholds.
private const val HUMIDITY_MIN = 0f
private const val HUMIDITY_MAX = 100f


/**
 * Detail screen for a single paired sensor, showing current readings, a 7-day
 * history chart (temperature, humidity, or light), and the alert history
 * filtered to this sensor.
 *
 * Driven by [SensorDetailViewModel] which reads `sensorId` from the nav arg
 * via SavedStateHandle, so the screen no longer needs that parameter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: SensorDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedChartTab by remember { mutableIntStateOf(0) }

    val config        = state.sensorConfig
    val readings      = state.recentReadings
    val latest        = readings.lastOrNull()
    val environment   = config?.environmentType?.let { name ->
        runCatching { EnvironmentType.valueOf(name) }.getOrNull()
    }
    val title = config?.displayName ?: if (state.isLoading) "Loading…" else "Sensor"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = title,
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
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            imageVector        = Icons.Outlined.Settings,
                            contentDescription = "Edit sensor",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
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
        if (state.isLoading && config == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
            }
            return@Scaffold
        }
        if (config == null) {
            EmptyState(
                icon           = Icons.Outlined.Thermostat,
                title          = "Sensor not found",
                supportingText = "This sensor may have been removed. Pair a new one from the Dashboard.",
                modifier       = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.padding(innerPadding),
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            //current readings
            item {
                TemperatureCard(
                    locationName = config.displayName,
                    temperature  = latest?.temperatureC ?: 0f,
                    humidity     = latest?.humidity ?: 0f,
                    lux          = latest?.lux ?: 0f,
                    // breachFlag 0 = clean; null reading shows as normal placeholder.
                    isNormal     = (latest?.breachFlag ?: 0) == 0,
                )
            }

            // chart metric
            item {
                TabRow(
                    selectedTabIndex = selectedChartTab,
                    indicator        = { tabPositions ->
                        SecondaryIndicator(
                            modifier  = Modifier.tabIndicatorOffset(tabPositions[selectedChartTab]),
                            color     = SectionGreen,
                        )
                    },
                ) {
                    listOf("🌡 Temperature", "💧 Humidity", "☀ Light")
                        .forEachIndexed { index, label ->
                            Tab(
                                selected = selectedChartTab == index,
                                onClick  = { selectedChartTab = index },
                                text     = {
                                    Text(
                                        text  = label,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                            )
                        }
                }
            }

            //7-day chart section label
            item {
                val chartSectionTitle = when (selectedChartTab) {
                    0    -> "7-DAY TEMPERATURE HISTORY"
                    1    -> "7-DAY HUMIDITY HISTORY"
                    else -> "7-DAY LIGHT HISTORY"
                }
                Text(
                    text  = chartSectionTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.Medium,
                        letterSpacing = 1.sp,
                    ),
                    color = SectionGreen,
                )
            }

            // Chart card OR empty state (a freshly-paired sensor has no readings yet).
            item {
                if (readings.isEmpty()) {
                    ElevatedCard(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    ) {
                        EmptyState(
                            icon           = Icons.Outlined.ShowChart,
                            title          = "No readings yet",
                            supportingText = "Once the next sensor tick fires, the 7-day chart will populate here.",
                        )
                    }
                } else {
                    ChartCard(
                        readings          = readings,
                        thresholdC        = config.thresholdC,
                        humidityThreshold = config.humidityThreshold,
                        maxLux            = config.maxLux,
                        selectedTab       = selectedChartTab,
                    )
                }
            }

            // alert history
            item {
                Text(
                    text  = "ALERT HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.Medium,
                        letterSpacing = 1.sp,
                    ),
                    color = SectionGreen,
                )
            }
            item {
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    val dividerColor =
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)

                    if (state.alertHistory.isEmpty()) {
                        EmptyState(
                            icon           = Icons.Outlined.Thermostat,
                            title          = "No alerts logged",
                            supportingText = "Breach events for this sensor will appear here.",
                            compact        = true,
                        )
                    } else {
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            state.alertHistory.forEachIndexed { index, notif ->
                                AlertHistoryRow(
                                    title       = notif.title,
                                    description = notif.body,
                                    timestamp   = alertTimeFmt.format(Date(notif.createdAt)),
                                )
                                if (index < state.alertHistory.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color     = dividerColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // sensor info section
            item {
                Text(
                    text  = "SENSOR INFO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.Medium,
                        letterSpacing = 1.sp,
                    ),
                    color = SectionGreen,
                )
            }
            item {
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    val dividerColor =
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)

                    DetailRow(label = "Device ID",        value = config.deviceId)
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(
                        label = "Environment",
                        value = environment?.let { "${it.emoji} ${it.label}" } ?: config.environmentType,
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Alert Threshold",  value = "${config.thresholdC.toInt()} °C")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Max Light",        value = "${config.maxLux.toInt()} lx")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(
                        label = "Paired On",
                        value = pairedDateFmt.format(Date(config.pairedAt)),
                    )
                }
            }
        }
    }
}

/**
 * Shared chart card — derives series, threshold, and y-axis bounds for the
 * selected metric from the real reading list.
 */
@Composable
private fun ChartCard(
    readings: List<SensorReadingEntity>,
    thresholdC: Float,
    humidityThreshold: Float,
    maxLux: Float,
    selectedTab: Int,
) {
    val series: FloatArray
    val threshold: Float
    val chartMin: Float
    val chartMax: Float
    val title: String
    val thresholdLabel: String
    val thresholdSubtitle: String

    when (selectedTab) {
        1 -> {
            series          = readings.map { it.humidity }.toFloatArray()
            threshold       = humidityThreshold
            chartMin        = HUMIDITY_MIN
            chartMax        = HUMIDITY_MAX
            title           = "Humidity trend — last 7 days"
            thresholdLabel  = "${threshold.toInt()}%"
            thresholdSubtitle = "Alert threshold: $thresholdLabel"
        }
        2 -> {
            series          = readings.map { it.lux }.toFloatArray()
            threshold       = maxLux
            chartMin        = 0f
            // Make sure the threshold line and the observed peak both fit.
            chartMax        = maxOf(maxLux * 1.5f, series.maxOrNull() ?: 0f) + 1f
            title           = "Light trend — last 7 days"
            thresholdLabel  = "${threshold.toInt()} lx"
            thresholdSubtitle = "Alert threshold: $thresholdLabel"
        }
        else -> {
            series          = readings.map { it.temperatureC }.toFloatArray()
            threshold       = thresholdC
            // Wrap the data range around the threshold with at least ±3 °C padding.
            val seriesMin   = series.minOrNull() ?: thresholdC
            val seriesMax   = series.maxOrNull() ?: thresholdC
            chartMin        = minOf(seriesMin, thresholdC) - 3f
            chartMax        = maxOf(seriesMax, thresholdC) + 3f
            title           = "Temperature trend — last 7 days"
            thresholdLabel  = "${threshold.toInt()}°C"
            thresholdSubtitle = "Alert threshold: $thresholdLabel"
        }
    }
    val thresholdFraction =
        1f - (threshold - chartMin) / (chartMax - chartMin)

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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = thresholdSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            // chart overlay
            Box(modifier = Modifier.fillMaxWidth()) {
                SevenDayChart(
                    readings  = series,
                    chartMin  = chartMin,
                    chartMax  = chartMax,
                    threshold = threshold,
                    modifier  = Modifier.fillMaxWidth(),
                )

                Text(
                    text     = thresholdLabel,
                    style    = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp,
                    ),
                    color    = ThresholdOrange,
                    modifier = Modifier.padding(top = (160.dp * thresholdFraction - 8.dp)),
                )
            }

            Spacer(Modifier.height(8.dp))

            // x-axis — pick up to 7 evenly-spaced timestamps from the data.
            val xLabels = remember(readings) {
                if (readings.isEmpty()) emptyList()
                else {
                    val target = minOf(7, readings.size)
                    val step = (readings.size - 1).coerceAtLeast(1).toFloat() / (target - 1).coerceAtLeast(1)
                    (0 until target).map { i ->
                        val idx = (i * step).toInt().coerceAtMost(readings.lastIndex)
                        chartAxisFmt.format(Date(readings[idx].timestamp))
                    }
                }
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                xLabels.forEach { label ->
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// 7-day canvas chart

/**
 * 7-day line chart rendered on a Canvas
 *
 * @param readings   Series of float values; >= 1 element required by the caller.
 * @param chartMin   Y-axis minimum value
 * @param chartMax   Y-axis maximum value
 * @param threshold  Value at which the line colour switches from green to red.
 */
@Composable
private fun SevenDayChart(
    readings:  FloatArray,
    chartMin:  Float,
    chartMax:  Float,
    threshold: Float,
    modifier:  Modifier = Modifier,
) {
    val dataRange  = (chartMax - chartMin).coerceAtLeast(1f)
    val spikeIndex = readings.indexOfFirst { it > threshold }
    val hasSpike   = spikeIndex >= 0
    val count      = readings.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .drawWithCache {
                val w     = size.width
                val h     = size.height
                // Single-point series: collapse stepX so the dot anchors at x=0.
                val stepX = if (count > 1) w / (count - 1).toFloat() else 0f

                fun valueToY(v: Float) = h * (1f - (v - chartMin) / dataRange)
                fun xAt(i: Int)        = i * stepX

                val pts = readings.mapIndexed { i, v ->
                    Offset(xAt(i), valueToY(v))
                }

                onDrawBehind {
                    repeat(5) { i ->
                        val y = h * i / 4f
                        drawLine(
                            color       = GridLine,
                            start       = Offset(0f, y),
                            end         = Offset(w, y),
                            strokeWidth = 1f,
                        )
                    }

                    // dashed threshold line
                    val threshY = valueToY(threshold)
                    drawLine(
                        color       = ThresholdOrange,
                        start       = Offset(0f, threshY),
                        end         = Offset(w, threshY),
                        strokeWidth = 2f,
                        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
                    )

                    // Per-segment colouring against the actual values, so a
                    // sawtooth crossing the threshold paints each breach run
                    // red without staining the safe stretches. See
                    // SensorLineChart for the same fix.
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

                    if (hasSpike) {
                        val spikePt = pts[spikeIndex]
                        drawCircle(AlertRedHalo, radius = 20f, center = spikePt)
                        drawCircle(AlertRed,     radius = 6f,  center = spikePt)
                    }

                    pts.forEachIndexed { i, pt ->
                        val dotColor =
                            if (readings[i] > threshold) AlertRed else NormalGreen
                        drawCircle(Color.White, radius = 5f, center = pt)
                        drawCircle(dotColor,    radius = 3f, center = pt)
                    }
                }
            },
    )
}

// alert history row

// Breach-kind palette — mirrors NotificationsScreen so the two surfaces
// stay in sync. SensorTickWorker.describeBreach() prefixes each part with
// "Temperature ", "Humidity ", or "Light "; on multi-dimension breaches
// priority temp > humidity > light puts the food-safety dimension first.
private data class BreachAssets(
    val circleBg: Color,
    val iconTint: Color,
    val icon:     ImageVector,
)

private val TempBreachAssets     = BreachAssets(Color(0xFFFFEDEA), Color(0xFFBA1A1A), Icons.Outlined.Thermostat)
private val HumidityBreachAssets = BreachAssets(Color(0xFFE3F2FD), Color(0xFF1976D2), Icons.Outlined.WaterDrop)
private val LightBreachAssets    = BreachAssets(Color(0xFFFFF9C4), Color(0xFFF57F17), Icons.Outlined.LightMode)

private fun breachAssets(body: String): BreachAssets = when {
    "Temperature" in body -> TempBreachAssets
    "Humidity" in body    -> HumidityBreachAssets
    "Light" in body       -> LightBreachAssets
    else                  -> TempBreachAssets
}

@Composable
private fun AlertHistoryRow(
    title:       String,
    description: String,
    timestamp:   String,
) {
    val assets = breachAssets(description)
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape    = CircleShape,
                color    = assets.circleBg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = assets.icon,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                        tint               = assets.iconTint,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text  = timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888),
        )
    }
}

// detailed row
@Composable
private fun DetailRow(
    label:      String,
    value:      String,
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


@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewSensorDetailLight() {
    SmartPantryTheme {
        Text("Sensor detail preview uses live app data at runtime.")
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSensorDetailDark() {
    SmartPantryTheme(darkTheme = true) {
        Text("Sensor detail preview uses live app data at runtime.")
    }
}
