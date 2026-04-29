
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// chart colour
private val NormalGreen     = Color(0xFF2E7D32)
private val AlertRed        = Color(0xFFBA1A1A)
private val AlertRedHalo    = Color(0x33BA1A1A)
private val ThresholdOrange = Color(0xFFF57F17)
private val GridLine        = Color(0xFFE0E0E0)

private val SectionGreen = Color(0xFF0F6E56)

// chart data - temperature
private val weekReadings    = floatArrayOf(22.5f, 23.0f, 24.5f, 25.0f, 25.5f, 29.5f, 26.5f)
private const val weekThreshold = 28.0f
private const val weekChartMin  = 18.0f
private const val weekChartMax  = 35.0f

// chart data - humidity
private val weekHumidityReadings  = floatArrayOf(60f, 62f, 63f, 64f, 63f, 72f, 66f)
private const val weekHumidityThreshold = 70f
private const val weekHumidityMin  = 40f
private const val weekHumidityMax  = 100f

// chart data
private val weekLuxReadings       = floatArrayOf(220f, 240f, 250f, 280f, 285f, 420f, 290f)
private const val weekLuxThreshold = 400f
private const val weekLuxMin       = 0f
private const val weekLuxMax       = 500f


/**
 * Detail screen for a single paired sensor, showing current readings, a 7-day
 * history chart (temperature, humidity, or light)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
) {
    var selectedChartTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Pantry Sensor",
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
        LazyColumn(
            modifier            = Modifier.padding(innerPadding),
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            //current readings
            item {
                TemperatureCard(
                    locationName = "Pantry",
                    temperature  = 26.5f,
                    humidity     = 63f,
                    lux          = 285f,
                    isNormal     = false,
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
                        .forEachIndexed { index, title ->
                            Tab(
                                selected = selectedChartTab == index,
                                onClick  = { selectedChartTab = index },
                                text     = {
                                    Text(
                                        text  = title,
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
            item {
                val thresholdLabel = when (selectedChartTab) {
                    0    -> "${weekThreshold.toInt()}°C"
                    1    -> "${weekHumidityThreshold.toInt()}%"
                    else -> "${weekLuxThreshold.toInt()} lx"
                }
                val thresholdSubtitle = when (selectedChartTab) {
                    0    -> "Alert threshold: ${weekThreshold.toInt()}°C"
                    1    -> "Alert threshold: ${weekHumidityThreshold.toInt()}%"
                    else -> "Alert threshold: ${weekLuxThreshold.toInt()} lx"
                }
                val activeThreshold = when (selectedChartTab) {
                    0    -> weekThreshold
                    1    -> weekHumidityThreshold
                    else -> weekLuxThreshold
                }
                val activeChartMin = when (selectedChartTab) {
                    0    -> weekChartMin
                    1    -> weekHumidityMin
                    else -> weekLuxMin
                }
                val activeChartMax = when (selectedChartTab) {
                    0    -> weekChartMax
                    1    -> weekHumidityMax
                    else -> weekLuxMax
                }
                val thresholdFraction =
                    1f - (activeThreshold - activeChartMin) / (activeChartMax - activeChartMin)

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
                            text  = "Temperature trend — last 7 days",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
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
                            when (selectedChartTab) {
                                0    -> SevenDayChart(
                                    readings  = weekReadings,
                                    chartMin  = weekChartMin,
                                    chartMax  = weekChartMax,
                                    threshold = weekThreshold,
                                    modifier  = Modifier.fillMaxWidth(),
                                )
                                1    -> SevenDayChart(
                                    readings  = weekHumidityReadings,
                                    chartMin  = weekHumidityMin,
                                    chartMax  = weekHumidityMax,
                                    threshold = weekHumidityThreshold,
                                    modifier  = Modifier.fillMaxWidth(),
                                )
                                else -> SevenDayChart(
                                    readings  = weekLuxReadings,
                                    chartMin  = weekLuxMin,
                                    chartMax  = weekLuxMax,
                                    threshold = weekLuxThreshold,
                                    modifier  = Modifier.fillMaxWidth(),
                                )
                            }

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

                        // x-axis
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            listOf(
                                "Apr 17", "Apr 18", "Apr 19", "Apr 20",
                                "Apr 21", "Apr 22", "Apr 23",
                            ).forEach { label ->
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

                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        AlertHistoryRow(
                            title       = "Temperature Spike",
                            description = "Reached 29.5 °C",
                            timestamp   = "22 Apr 2026, 20:00",
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = dividerColor,
                        )
                        AlertHistoryRow(
                            title       = "Temperature Spike",
                            description = "Reached 29.0 °C",
                            timestamp   = "18 Apr 2026, 15:30",
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = dividerColor,
                        )
                        AlertHistoryRow(
                            title       = "Temperature Spike",
                            description = "Reached 28.5 °C",
                            timestamp   = "10 Apr 2026, 11:00",
                        )
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

                    DetailRow(label = "Device ID",        value = "TempHumid-B891")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Environment",      value = "🌡 Pantry")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Alert Threshold",  value = "28 °C")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(label = "Paired On",        value = "5 Apr 2026")
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    DetailRow(
                        label      = "Battery",
                        value      = "78%",
                        valueColor = Color(0xFF2E7D32),   // battery ≥ 50 %
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
 * @param readings   Array of exactly 7 float values
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
    val dataRange  = chartMax - chartMin
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
                val stepX = w / (count - 1).toFloat()

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
                        val dotColor =
                            if (hasSpike && i >= spikeIndex) AlertRed else NormalGreen
                        drawCircle(Color.White, radius = 5f, center = pt)
                        drawCircle(dotColor,    radius = 3f, center = pt)
                    }
                }
            },
    )
}

// alert history row

@Composable
private fun AlertHistoryRow(
    title:       String,
    description: String,
    timestamp:   String,
) {
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
                color    = Color(0xFFFFEDEA),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Outlined.Thermostat,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                        tint               = Color(0xFFBA1A1A),
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
        SensorDetailScreen(
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
private fun PreviewSensorDetailDark() {
    SmartPantryTheme(darkTheme = true) {
        SensorDetailScreen(
            onNavigateBack   = {},
            onNavigateToEdit = {},
        )
    }
}
