package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.AnalyticsUiState
import com.lab03g10.smartpantry.ui.viewmodel.AnalyticsViewModel

// Colours used by the monthly chart and category rows.
// Keeping them at the top makes the screen easier to style later.
private val BarJan = Color(0xFFB4B2A9)
private val BarFeb = Color(0xFF9FE1CB)
private val BarMar = Color(0xFF1D9E75)
private val BarApr = Color(0xFF2E7D32)
private val ExtraBar = Color(0xFFFAC775)

private val SafeGreen = Color(0xFF2E7D32)
private val StatTileBg = Color(0xFFF5F5F5)
private val LabelGrey = Color(0xFF888888)

// This small class stores one monthly bar for the chart.
private data class SimpleBar(
    val label: String,
    val value: Int,
    val color: Color,
    val isActive: Boolean = false,
)

// This small class stores one category row for the category breakdown.
private data class SimpleCategory(
    val name: String,
    val progress: Float,
    val color: Color,
)

/**
 * This route connects the AnalyticsViewModel to the AnalyticsScreen.
 *
 * We keep this separate from AnalyticsScreen so previews can still open
 * AnalyticsScreen without needing Hilt.
 */
@Composable
fun AnalyticsRoute(
    onNavigateToWasteLog: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    AnalyticsScreen(
        onNavigateToWasteLog = onNavigateToWasteLog,
        uiState = viewModel.uiState,
    )
}

/**
 * Analytics screen showing waste statistics, monthly trend, and category breakdown.
 *
 * The data comes from AnalyticsUiState, which is prepared by AnalyticsViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateToWasteLog: () -> Unit = {},
    uiState: AnalyticsUiState = AnalyticsUiState(),
) {
    // The database stores CO2e in grams, but the UI displays kilograms.
    val totalCo2eKg = uiState.totalCo2eGrams / 1000f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "Waste impact from recorded pantry waste",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Summary cards give a quick overview before the charts.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(
                    value = uiState.totalWastedItems.toString(),
                    label = "Items wasted",
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onNavigateToWasteLog()
                        },
                )

                StatTile(
                    value = "%.1f".format(totalCo2eKg),
                    label = "kg CO2e",
                    valueColor = SafeGreen,
                    modifier = Modifier.weight(1f),
                )

                StatTile(
                    value = getCategoryCount(uiState).toString(),
                    label = "Waste categories",
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }

            SectionCard(
                title = "Monthly Waste Trend",
            ) {
                MonthlyWasteChart(uiState = uiState)
            }

            SectionCard(
                title = "By Category",
            ) {
                CategorySection(uiState = uiState)
            }
        }
    }
}

/**
 * Small summary card used for totals at the top of the screen.
 */
@Composable
private fun StatTile(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(StatTileBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                ),
                color = valueColor,
                textAlign = TextAlign.Center,
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Card container used for the chart sections.
 */
@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            content()
        }
    }
}

/**
 * Monthly bar chart using real monthly waste totals from Room.
 */
@Composable
private fun MonthlyWasteChart(
    uiState: AnalyticsUiState,
) {
    val bars = getMonthlyBars(uiState)

    // If there are no waste records yet, show a helpful empty message.
    if (bars.isEmpty()) {
        EmptyAnalyticsMessage(
            title = "No waste records yet",
            message = "Mark pantry items as wasted to build monthly trends.",
        )
        return
    }

    // Find the biggest monthly value so every bar can be scaled correctly.
    var maxValue = 1

    for (bar in bars) {
        if (bar.value > maxValue) {
            maxValue = bar.value
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            for (bar in bars) {
                MonthlyBar(
                    value = bar.value,
                    maxValue = maxValue,
                    color = bar.color,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            for (bar in bars) {
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (bar.isActive) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * One bar in the monthly waste chart.
 */
@Composable
private fun MonthlyBar(
    value: Int,
    maxValue: Int,
    color: Color,
) {
    // Scale the bar to fit inside the 160dp chart area.
    val barHeight = if (maxValue == 0) {
        0
    } else {
        (value * 140) / maxValue
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = LabelGrey,
        )

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(barHeight.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ),
        )
    }
}

/**
 * Category breakdown section using real category totals from Room.
 */
@Composable
private fun CategorySection(
    uiState: AnalyticsUiState,
) {
    val categoryList = getCategoryBars(uiState)

    // If no category data exists, show an empty message.
    if (categoryList.isEmpty()) {
        EmptyAnalyticsMessage(
            title = "No category data yet",
            message = "Waste categories will appear after waste records are saved.",
        )
        return
    }

    Column {
        for (index in categoryList.indices) {
            val category = categoryList[index]

            if (index > 0) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                )
            }

            CategoryBreakdownRow(
                name = category.name,
                progress = category.progress,
                color = category.color,
            )
        }
    }
}

/**
 * One category row with a percentage and progress bar.
 */
@Composable
private fun CategoryBreakdownRow(
    name: String,
    progress: Float,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(9.dp)
                        .background(color, CircleShape),
                )

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = LabelGrey,
            )
        }

        LinearProgressIndicator(
            progress = {
                progress
            },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
        )
    }
}

/**
 * Empty message shown when there is no real waste data yet.
 */
@Composable
private fun EmptyAnalyticsMessage(
    title: String,
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Convert monthly database totals into simple chart bars.
 */
private fun getMonthlyBars(
    uiState: AnalyticsUiState,
): List<SimpleBar> {
    val barList = mutableListOf<SimpleBar>()

    for (index in uiState.monthlyTotals.indices) {
        val month = uiState.monthlyTotals[index]

        val bar = SimpleBar(
            label = getMonthLabel(month.yearMonth),
            value = month.count,
            color = getBarColor(index),
            isActive = index == uiState.monthlyTotals.lastIndex,
        )

        barList.add(bar)
    }

    return barList
}

/**
 * Convert category database totals into progress rows.
 */
private fun getCategoryBars(
    uiState: AnalyticsUiState,
): List<SimpleCategory> {
    var totalCount = 0

    for (category in uiState.categoryTotals) {
        totalCount += category.count
    }

    val categoryList = mutableListOf<SimpleCategory>()

    for (index in uiState.categoryTotals.indices) {
        val category = uiState.categoryTotals[index]

        var progress = 0f

        if (totalCount > 0) {
            progress = category.count.toFloat() / totalCount.toFloat()
        }

        val categoryItem = SimpleCategory(
            name = category.category,
            progress = progress,
            color = getBarColor(index),
        )

        categoryList.add(categoryItem)
    }

    return categoryList
}

private fun getCategoryCount(
    uiState: AnalyticsUiState,
): Int {
    return uiState.categoryTotals.size
}

private fun getBarColor(index: Int): Color {
    if (index == 0) return BarJan
    if (index == 1) return BarFeb
    if (index == 2) return BarMar
    if (index == 3) return BarApr

    return ExtraBar
}

/**
 * Room returns months as values like "2026-04".
 * This function converts them to short labels like "Apr".
 */
private fun getMonthLabel(yearMonth: String): String {
    if (yearMonth.endsWith("-01")) return "Jan"
    if (yearMonth.endsWith("-02")) return "Feb"
    if (yearMonth.endsWith("-03")) return "Mar"
    if (yearMonth.endsWith("-04")) return "Apr"
    if (yearMonth.endsWith("-05")) return "May"
    if (yearMonth.endsWith("-06")) return "Jun"
    if (yearMonth.endsWith("-07")) return "Jul"
    if (yearMonth.endsWith("-08")) return "Aug"
    if (yearMonth.endsWith("-09")) return "Sep"
    if (yearMonth.endsWith("-10")) return "Oct"
    if (yearMonth.endsWith("-11")) return "Nov"
    if (yearMonth.endsWith("-12")) return "Dec"

    return yearMonth
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewAnalyticsLight() {
    SmartPantryTheme {
        AnalyticsScreen()
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF191C18,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAnalyticsDark() {
    SmartPantryTheme(darkTheme = true) {
        AnalyticsScreen()
    }
}
