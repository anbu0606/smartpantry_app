package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

//chart colours
private val BarJan      = Color(0xFFB4B2A9)//warm grey color
private val BarFeb      = Color(0xFF9FE1CB)   //  mint colour
private val BarMar      = Color(0xFF1D9E75)   //  teal green
private val BarApr      = Color(0xFF2E7D32)   //   green
private val SafeGreen   = Color(0xFF2E7D32)
private val StatTileBg  = Color(0xFFF5F5F5)
private val LabelGrey   = Color(0xFF888888)
private val GridLine    = Color(0xFFE0E0E0)
private val ChartSurface = Color(0xFFF3F3F3)
//options
private val periodOptions = listOf("Jan 2026", "Feb 2026", "Mar 2026", "Apr 2026", "Yearly")

// Bar chart data
private data class WasteBar(val label: String, val value: Int, val color: Color, val isActive: Boolean = false)

private val wasteData = listOf(
    WasteBar("Jan", 8, BarJan),
    WasteBar("Feb", 5, BarFeb),
    WasteBar("Mar", 3, BarMar),
    WasteBar("Apr", 2, BarApr, isActive = true),
)

// Categories
private data class CategoryRow(val name: String, val percent: Float, val color: Color)

private val categoryData = listOf(
    CategoryRow("Produce", 0.35f, BarApr),
    CategoryRow("Dairy",   0.28f, BarFeb),
    CategoryRow("Meat",    0.22f, Color(0xFFFAC775)),
    CategoryRow("Other",   0.15f, BarJan),
)


/**
 * Analytics screen — waste trend and category breakdowns
 * Layout - LazyColumn
 * Period selector LazyRow used
 *  Summary stat row
 * Monthly Waste Trend card
 * The bar chart uses drawWithCache
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onNavigateToWasteLog: () -> Unit = {}) {
    var selectedPeriod by remember { mutableStateOf("Apr 2026") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            //period selector
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(periodOptions) { period ->
                        PeriodChip(
                            label    = period,
                            selected = period == selectedPeriod,
                            onClick  = { selectedPeriod = period },
                        )
                    }
                }
            }

            // Summary stat tiles
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Hardcoded prototype values
                    StatTile(
                        value      = "8",
                        label      = "Items wasted",
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f).clickable { onNavigateToWasteLog() },
                    )
                    // Hardcoded prototype values
                    StatTile(
                        value      = "↓62%",
                        label      = "vs last month",
                        valueColor = SafeGreen,
                        modifier   = Modifier.weight(1f),
                    )

                    StatTile(
                        value      = "6",
                        label      = "Items saved",
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }

            //Monthly Waste Trend bar chart
            item {
                SectionCard(
                    title   = "Monthly Waste Trend",
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    WasteTrendChart(bars = wasteData)
                }
            }

            //Category breakdown
            item {
                SectionCard(
                    title   = "By Category",
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Column {
                        categoryData.forEachIndexed { index, row ->
                            if (index > 0) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                                )
                            }
                            CategoryBreakdownRow(
                                name    = row.name,
                                percent = row.percent,
                                color   = row.color,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Period chips

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedChipBg     = Color(0xFFC8E6C9)
    val selectedChipBorder = Color(0xFF2E7D32)
    val selectedChipText   = Color(0xFF085041)

    Surface(
        onClick = onClick,
        shape   = CircleShape,
        color   = if (selected) selectedChipBg else MaterialTheme.colorScheme.surfaceContainerLow,
        border  = if (selected) BorderStroke(1.dp, selectedChipBorder) else null,
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style    = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color    = if (selected) selectedChipText else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Stat tiles

@Composable
private fun StatTile(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier         = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StatTileBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text      = value,
                style     = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                ),
                color     = valueColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = LabelGrey,
                textAlign = TextAlign.Center,
            )
        }
    }
}

//Section cards


@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

//Bar charts
/**
 *  bar chart drawn with drawWithCache
 */
@Composable
private fun WasteTrendChart(bars: List<WasteBar>) {
    val chartHeight = 160.dp
    val maxValue    = bars.maxOf { it.value }.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .drawWithCache {
                    val w         = size.width
                    val h         = size.height
                    val barCount  = bars.size
                    val spacing   = w * 0.08f
                    val totalGap  = spacing * (barCount + 1)
                    val barW      = (w - totalGap) / barCount
                    val gridCount = 5

                    onDrawBehind {
                        //horizontal grid lines
                        repeat(gridCount) { i ->
                            val y = h * i / (gridCount - 1).toFloat()
                            drawLine(
                                color       = GridLine,
                                start       = Offset(0f, y),
                                end         = Offset(w, y),
                                strokeWidth = 1f,
                            )
                        }

                        bars.forEachIndexed { idx, bar ->
                            val barHeight  = (bar.value / maxValue) * h
                            val left       = spacing + idx * (barW + spacing)
                            val top        = h - barHeight
                            val cornerR    = barW / 2f


                            drawRoundRect(
                                color       = bar.color,
                                topLeft     = Offset(left, top),
                                size        = Size(barW, barHeight),
                                cornerRadius = CornerRadius(cornerR, cornerR),
                            )
                            if (barHeight > cornerR) {
                                drawRect(
                                    color   = bar.color,
                                    topLeft = Offset(left, top + cornerR),
                                    size    = Size(barW, barHeight - cornerR),
                                )
                            }
                        }
                    }
                },
        )

        // x-axis labels
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            bars.forEach { bar ->
                Text(
                    text      = bar.label,
                    style     = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color     = if (bar.isActive) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
            }
        }
    }
}

// Category breakdowns

@Composable
private fun CategoryBreakdownRow(
    name: String,
    percent: Float,
    color: Color,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text  = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text  = "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = LabelGrey,
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress          = { percent },
            color             = color,
            trackColor        = MaterialTheme.colorScheme.surfaceContainer,
            strokeCap         = StrokeCap.Round,
            gapSize           = 0.dp,
            drawStopIndicator = {},
            modifier          = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
        )
    }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewAnalyticsLight() {
    SmartPantryTheme {
        AnalyticsScreen()
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAnalyticsDark() {
    SmartPantryTheme(darkTheme = true) {
        AnalyticsScreen()
    }
}
