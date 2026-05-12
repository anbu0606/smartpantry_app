package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
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
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

private val WastedRed     = Color(0xFFBA1A1A)
private val WastedBg      = Color(0xFFFFEDEA)
private val ImpactOrange  = Color(0xFFE65100)   // CO₂e summary card
private val LabelGrey     = Color(0xFF888888)

// Per-reason badge palette — used on each waste row to show why the item was discarded
private val ExpiredBadgeFg = Color(0xFFE65100); private val ExpiredBadgeBg = Color(0xFFFFE0B2)
private val SpoiledBadgeFg = Color(0xFF6A1B9A); private val SpoiledBadgeBg = Color(0xFFE1BEE7)
private val OtherBadgeFg   = Color(0xFF455A64); private val OtherBadgeBg   = Color(0xFFCFD8DC)

private val SelectedChipBg     = Color(0xFFC8E6C9)
private val SelectedChipBorder = Color(0xFF2E7D32)
private val SelectedChipText   = Color(0xFF085041)
private val UnselectedChipBorder = Color(0xFFCCCCCC)

// WasteLog is waste-only (consumed items live on FoodItemEntity.consumed instead).
// See day2-room-di-checklist §1.5 design decision.
private enum class WasteReason(
    val label: String,
    val badgeFg: Color,
    val badgeBg: Color,
) {
    EXPIRED("Expired", ExpiredBadgeFg, ExpiredBadgeBg),
    SPOILED("Spoiled", SpoiledBadgeFg, SpoiledBadgeBg),
    OTHER  ("Other",   OtherBadgeFg,   OtherBadgeBg),
}

private data class WasteEntry(
    val id: String,
    val foodName: String,
    val category: String,
    val reason: WasteReason,
    val date: String,
    val monthGroup: String,
)
// Hardcoded prototype sample. Abinaya Day 3-5 will replace with Flow<List<WasteLogEntity>>
// from WasteRepository. All entries are wasted; reason values mirror WasteLogEntity.reason
// ("EXPIRED" / "SPOILED" / "OTHER").
private val wasteEntries = listOf(
    WasteEntry("e1",  "Baby Spinach",    "Produce",    WasteReason.EXPIRED, "17 Apr 2026", "APRIL 2026"),
    WasteEntry("e2",  "Cheddar Cheese",  "Dairy",      WasteReason.SPOILED, "14 Apr 2026", "APRIL 2026"),
    WasteEntry("e3",  "Greek Yogurt",    "Dairy",      WasteReason.EXPIRED, "12 Apr 2026", "APRIL 2026"),
    WasteEntry("e4",  "Orange Juice",    "Beverages",  WasteReason.SPOILED, "10 Apr 2026", "APRIL 2026"),
    WasteEntry("e5",  "Bread Loaf",      "Grains",     WasteReason.EXPIRED, "08 Apr 2026", "APRIL 2026"),
    WasteEntry("e6",  "Leftover Curry",  "Other",      WasteReason.OTHER,   "05 Apr 2026", "APRIL 2026"),
    WasteEntry("e7",  "Strawberries",    "Produce",    WasteReason.SPOILED, "28 Mar 2026", "MARCH 2026"),
    WasteEntry("e8",  "Ground Beef",     "Meat",       WasteReason.SPOILED, "25 Mar 2026", "MARCH 2026"),
    WasteEntry("e9",  "Almond Milk",     "Dairy",      WasteReason.EXPIRED, "22 Mar 2026", "MARCH 2026"),
    WasteEntry("e10", "Stale Crackers",  "Grains",     WasteReason.OTHER,   "18 Mar 2026", "MARCH 2026"),
)

private val groupOrder = listOf("APRIL 2026", "MARCH 2026")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WasteLogScreen(onNavigateBack: () -> Unit) {

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredEntries = wasteEntries.filter { entry ->
        when (selectedFilter) {
            "Expired" -> entry.reason == WasteReason.EXPIRED
            "Spoiled" -> entry.reason == WasteReason.SPOILED
            "Other"   -> entry.reason == WasteReason.OTHER
            else      -> true
        }
    }

    val groupedEntries = filteredEntries.groupBy { it.monthGroup }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Waste Log",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
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
            modifier            = Modifier
                .padding(innerPadding),
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // summary stats — waste-only after the WasteLogEntity schema decision
            // (left card: total waste count; right card: estimated CO₂e impact).
            // Numbers below are hardcoded for prototype; Abinaya Day 3 wires them to
            // WasteRepository.observeAll().count() and the CO₂e lookup table aggregation.
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ElevatedCard(
                        modifier  = Modifier.weight(1f),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text  = "10",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize   = 32.sp,
                                    fontWeight = FontWeight.Light,
                                ),
                                color = WastedRed,
                            )
                            Text(
                                text  = "Items wasted",
                                style = MaterialTheme.typography.labelSmall,
                                color = LabelGrey,
                            )
                        }
                    }
                    ElevatedCard(
                        modifier  = Modifier.weight(1f),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text  = "~245 g",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize   = 32.sp,
                                    fontWeight = FontWeight.Light,
                                ),
                                color = ImpactOrange,
                            )
                            Text(
                                text  = "CO₂e impact",
                                style = MaterialTheme.typography.labelSmall,
                                color = LabelGrey,
                            )
                        }
                    }
                }
            }

            // filter
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("All", "Expired", "Spoiled", "Other").forEach { filter ->
                        WasteFilterChip(
                            label    = filter,
                            selected = filter == selectedFilter,
                            onClick  = { selectedFilter = filter },
                        )
                    }
                }
            }

            // grouped entries
            groupOrder.forEach { group ->
                val entries = groupedEntries[group] ?: return@forEach

                stickyHeader(key = "header_$group") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 6.dp),
                    ) {
                        Text(
                            text     = group,
                            style    = MaterialTheme.typography.labelSmall.copy(
                                fontWeight    = FontWeight.Medium,
                                letterSpacing = 1.sp,
                            ),
                            color    = Color(0xFF0F6E56),
                        )
                    }
                }

                items(entries, key = { it.id }) { entry ->
                    WasteEntryRow(entry = entry)
                }
            }
        }
    }
}

// Filter
@Composable
private fun WasteFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape   = CircleShape,
        color   = if (selected) SelectedChipBg else MaterialTheme.colorScheme.surface,
        border  = BorderStroke(
            width = 1.dp,
            color = if (selected) SelectedChipBorder else UnselectedChipBorder,
        ),
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style    = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
            color    = if (selected) SelectedChipText else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WasteEntryRow(entry: WasteEntry) {
    ElevatedCard(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = entry.reason.badgeBg,
                ) {
                    Text(
                        text     = entry.reason.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color    = entry.reason.badgeFg,
                    )
                }
                Column {
                    Text(
                        text  = entry.foodName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = entry.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = LabelGrey,
                    )
                }
            }
            Text(
                text  = entry.date,
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewWasteLogLight() {
    SmartPantryTheme {
        WasteLogScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWasteLogDark() {
    SmartPantryTheme(darkTheme = true) {
        WasteLogScreen(onNavigateBack = {})
    }
}
