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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
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

        // LazyColumn allows the screen to scroll.
        // This prevents the category rows from being cut off at the bottom.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = "Waste impact from recorded pantry waste",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Summary cards give a quick overview before the charts.
            item {
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
            }

            item {
                SectionCard(
                    title = "Monthly Waste Trend",
                    subtitle = "Showing the last 4 months",
                ) {
                    MonthlyWasteChart(uiState = uiState)
                }
            }

            item {
                SectionCard(
                    title = "By Category",
                ) {
                    CategorySection(uiState = uiState)
                }
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
                style = MaterialTheme.typography.headlineLarge.copy(
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
    subtitle: String? = null,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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

    // If there are no bars, show empty message.
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        for (bar in bars) {
            MonthlyBar(
                label = bar.label,
                value = bar.value,
                maxValue = maxValue,
                color = bar.color,
                isActive = bar.isActive,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
* One bar in the monthly waste chart.
*
*/
@Composable
private fun MonthlyBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    // Scale the bar to fit inside the chart area.
    // Zero values still show a tiny line so the month is visible.
    val barHeight = if (value == 0) {
        4
    } else {
        (value * 120) / maxValue
    }

    Column(
        modifier = modifier,
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

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = if (isActive) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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
 *
 * If a month has no waste records, it shows 0.
 */
private fun getMonthlyBars(
    uiState: AnalyticsUiState,
): List<SimpleBar> {

    // Store real Room values in a simple map.
    // Example: "2026-05" -> 13
    val realMonthlyCounts = mutableMapOf<String, Int>()

    for (month in uiState.monthlyTotals) {
        realMonthlyCounts[month.yearMonth] = month.count
    }

    // Create the latest 4 months based on today's date.
    val monthsToShow = getLatestFourMonths()

    val barList = mutableListOf<SimpleBar>()

    for (index in monthsToShow.indices) {
        val yearMonth = monthsToShow[index]

        // If Room does not have this month, use 0.
        val count = realMonthlyCounts[yearMonth] ?: 0

        val bar = SimpleBar(
            label = getMonthLabel(yearMonth),
            value = count,
            color = getBarColor(index),
            isActive = index == monthsToShow.lastIndex,
        )

        barList.add(bar)
    }

    return barList
}

/**
 * This function returns the latest 4 months.
 *
 * Example:
 * If the current month is May 2026, it returns:
 * 2026-02, 2026-03, 2026-04, 2026-05
 */
private fun getLatestFourMonths(): List<String> {
    val monthList = mutableListOf<String>()

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -3)

    val formatter = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)

    for (i in 0 until 4) {
        monthList.add(formatter.format(calendar.time))
        calendar.add(Calendar.MONTH, 1)
    }

    return monthList
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
 * Convert Room month values like "2026-04"
 * into labels like "Apr 2026".
 */
private fun getMonthLabel(yearMonth: String): String {

    val parts = yearMonth.split("-")

    if (parts.size != 2) {
        return yearMonth
    }

    val year = parts[0]

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> parts[1]
    }

    return "$month $year"
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
