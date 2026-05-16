package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.WasteLogRowUi
import com.lab03g10.smartpantry.ui.viewmodel.WasteLogUiState
import com.lab03g10.smartpantry.ui.viewmodel.WasteLogViewModel

// Main colour used for wasted item count
private val WastedRed = Color(0xFFBA1A1A)

// Main colour used for CO2e impact value
private val ImpactOrange = Color(0xFFE65100)

// Grey colour used for small labels
private val LabelGrey = Color(0xFF888888)

// Colours used for Expired badge
private val ExpiredBadgeFg = Color(0xFFE65100)
private val ExpiredBadgeBg = Color(0xFFFFE0B2)

// Colours used for Spoiled badge
private val SpoiledBadgeFg = Color(0xFF6A1B9A)
private val SpoiledBadgeBg = Color(0xFFE1BEE7)

// Colours used for Other badge
private val OtherBadgeFg = Color(0xFF455A64)
private val OtherBadgeBg = Color(0xFFCFD8DC)

// Colours used when filter chip is selected
private val SelectedChipBg = Color(0xFFC8E6C9)
private val SelectedChipBorder = Color(0xFF2E7D32)
private val SelectedChipText = Color(0xFF085041)

// Border colour for chips that are not selected
private val UnselectedChipBorder = Color(0xFFCCCCCC)

/**
 * This route connects the WasteLogViewModel to the WasteLogScreen.
 *
 * AppNavigation should call this route, not WasteLogScreen directly.
 */
@Composable
fun WasteLogRoute(
    onNavigateBack: () -> Unit,
    viewModel: WasteLogViewModel = hiltViewModel()
) {
    WasteLogScreen(
        onNavigateBack = onNavigateBack,
        uiState = viewModel.uiState,
        onFilterSelected = viewModel::onFilterSelected
    )
}

/**
 * This is the main Waste Log screen.
 *
 * It shows summary cards, filter chips and waste log records.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WasteLogScreen(
    onNavigateBack: () -> Unit,
    uiState: WasteLogUiState = WasteLogUiState(),
    onFilterSelected: (String) -> Unit = {}
) {
    // Scaffold gives the basic page structure with top bar and content
    Scaffold(
        topBar = {
            // Top app bar shows the title and back button
            TopAppBar(
                title = {
                    Text(
                        text = "Waste Log",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // LazyColumn is used because the waste log list can scroll
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // This section shows the summary cards at the top
                SummaryCards(uiState = uiState)
            }

            item {
                // This section shows the filter buttons
                FilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }

            // If there is no data, show an empty message
            if (uiState.rows.isEmpty()) {
                item {
                    EmptyWasteLogMessage()
                }
            }

            // Loop through each month group
            uiState.groupOrder.forEach { group ->

                // Get the waste entries for this month group
                val entries = uiState.groupedRows[group] ?: return@forEach

                // stickyHeader keeps the month heading visible while scrolling
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF0F6E56)
                        )
                    }
                }

                // Show each waste entry under the month heading
                items(entries) { entry ->
                    WasteEntryRow(entry = entry)
                }
            }
        }
    }
}

/**
 * This function shows the two summary cards at the top.
 *
 * It shows total wasted items and total CO2e impact.
 */
@Composable
private fun SummaryCards(uiState: WasteLogUiState) {
    // Row is used to show two cards side by side
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // This card shows total number of wasted items
        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show wasted item count from uiState
                Text(
                    text = uiState.wastedCount.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = WastedRed
                )

                Text(
                    text = "Items wasted",
                    style = MaterialTheme.typography.labelSmall,
                    color = LabelGrey
                )
            }
        }

        // This card shows total CO2e impact
        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show CO2e label from uiState, for example 500 g or 1.2 kg
                Text(
                    text = uiState.totalCo2eLabel,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = ImpactOrange
                )

                Text(
                    text = "Est. CO2e impact",
                    style = MaterialTheme.typography.labelSmall,
                    color = LabelGrey
                )
            }
        }
    }
}

/**
 * This function shows the filter chips.
 *
 * The user can filter waste records by All, Expired, Spoiled or Other.
 */
@Composable
private fun FilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // These are the filter options shown to the user
        listOf("All", "Expired", "Spoiled", "Other").forEach { filter ->

            // Create one filter chip for each option
            WasteFilterChip(
                label = filter,

                // Check whether this chip is selected
                selected = filter == selectedFilter,

                // When chip is clicked, update selected filter
                onClick = {
                    onFilterSelected(filter)
                }
            )
        }
    }
}

/**
 * This function shows one filter chip.
 *
 * The selected chip has a different colour.
 */
@Composable
private fun WasteFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,

        // Change background colour based on selected state
        color = if (selected) SelectedChipBg else MaterialTheme.colorScheme.surface,

        border = BorderStroke(
            width = 1.dp,

            // Change border colour based on selected state
            color = if (selected) SelectedChipBorder else UnselectedChipBorder
        )
    ) {
        // Show the filter name inside the chip
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected) SelectedChipText else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * This function shows one waste log row.
 *
 * It shows the reason badge, food name, category, CO2e value and date.
 */
@Composable
private fun WasteEntryRow(entry: WasteLogRowUi) {
    // Get badge colour based on the reason
    val badgeColors = getReasonBadgeColors(entry.reason)

    // Card is used to show one waste record
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main row contains left side food details and right side date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Badge shows the waste reason like Expired, Spoiled or Other
                Surface(
                    shape = CircleShape,
                    color = badgeColors.background
                ) {
                    Text(
                        text = entry.reason,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = badgeColors.foreground
                    )
                }

                Column {
                    // Show food name
                    Text(
                        text = entry.foodName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Show category and CO2e value below food name
                    Text(
                        text = "${entry.category} - ${entry.quantity} - ${entry.co2eLabel} CO2e",
                        style = MaterialTheme.typography.labelSmall,
                        color = LabelGrey
                    )
                }
            }

            // Show wasted date on the right side
            Text(
                text = entry.dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey
            )
        }
    }
}

/**
 * This function shows a message when there are no waste records.
 *
 * It is shown when the Room database has no waste log data.
 */
@Composable
private fun EmptyWasteLogMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No waste records yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * This class stores the badge colours.
 *
 * Each badge has a foreground colour and background colour.
 */
private data class ReasonBadgeColors(
    val foreground: Color,
    val background: Color
)

/**
 * This function returns badge colours based on the waste reason.
 *
 * Expired, Spoiled and Other have different colours.
 */
private fun getReasonBadgeColors(reason: String): ReasonBadgeColors {
    return when (reason) {

        // Return orange colour for expired items
        "Expired" -> ReasonBadgeColors(
            foreground = ExpiredBadgeFg,
            background = ExpiredBadgeBg
        )

        // Return purple colour for spoiled items
        "Spoiled" -> ReasonBadgeColors(
            foreground = SpoiledBadgeFg,
            background = SpoiledBadgeBg
        )

        // Return grey colour for other reasons
        else -> ReasonBadgeColors(
            foreground = OtherBadgeFg,
            background = OtherBadgeBg
        )
    }
}

/**
 * This preview shows the Waste Log screen in light mode.
 */
@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewWasteLogLight() {
    SmartPantryTheme {
        WasteLogScreen(onNavigateBack = {})
    }
}

/**
 * This preview shows the Waste Log screen in dark mode.
 */
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF191C18,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewWasteLogDark() {
    SmartPantryTheme(darkTheme = true) {
        WasteLogScreen(onNavigateBack = {})
    }
}