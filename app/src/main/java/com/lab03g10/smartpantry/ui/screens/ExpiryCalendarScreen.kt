package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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

// Colors
private val ExpiredGrey  = Color(0xFF9E9E9E)
private val UrgentRed    = Color(0xFFBA1A1A)
@Suppress("unused")
private val UrgentRedBg  = Color(0xFFFFEDEA)
private val WarnOrange   = Color(0xFFF57F17)
@Suppress("unused")
private val WarnOrangeBg = Color(0xFFFFF3E0)
private val SafeGreen    = Color(0xFF2E7D32)
@Suppress("unused")
private val SafeGreenBg  = Color(0xFFE8F5E9)
private val TodayGreen   = Color(0xFF0F6E56)

// data model
private data class ExpiryEvent(
    val foodName: String,
    val category: String,
)

// Key = day of month
private val aprilExpiryMap: Map<Int, List<ExpiryEvent>> = mapOf(
    17 to listOf(ExpiryEvent("Butter", "Dairy")),               // already expire
    18 to listOf(ExpiryEvent("Full Cream Milk", "Dairy")),      // already expired
    19 to listOf(ExpiryEvent("Baby Spinach", "Produce")),       // already expired
    22 to listOf(
        ExpiryEvent("Cheddar Cheese", "Dairy"),
        ExpiryEvent("Carrots", "Produce"),
    ),                                                           // today
    23 to listOf(ExpiryEvent("Chicken Breast", "Meat")),        // 1 day
    27 to listOf(ExpiryEvent("Basmati Rice", "Dry Goods")),     // 5 days
    30 to listOf(ExpiryEvent("Orange Juice", "Beverages")),     // 8 days
)

private const val TODAY_DAY = 22  // April 22 - "today"

// April 2026 calendar grid
private val aprilWeeks: List<List<Int?>> = listOf(
    listOf(null, null, 1,    2,    3,    4,    5   ),
    listOf(6,    7,    8,    9,    10,   11,   12  ),
    listOf(13,   14,   15,   16,   17,   18,   19  ),
    listOf(20,   21,   22,   23,   24,   25,   26  ),
    listOf(27,   28,   29,   30,   null, null, null),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryCalendarScreen(onNavigateBack: () -> Unit) {

    var selectedDay by remember { mutableStateOf<Int?>(22) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Expiry Calendar",
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

            // Month navigation header
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector        = Icons.Filled.ChevronLeft,
                        contentDescription = "Previous month",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text  = "April 2026",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector        = Icons.Filled.ChevronRight,
                        contentDescription = "Next month",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Day of week header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Box(
                        modifier          = Modifier.weight(1f),
                        contentAlignment  = Alignment.Center,
                    ) {
                        Text(
                            text  = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Calendar grid
            Column(
                modifier            = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                aprilWeeks.forEach { week ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                            ) {
                                if (day != null) {
                                    val events   = aprilExpiryMap[day] ?: emptyList()
                                    val isToday  = day == TODAY_DAY
                                    val isSelected = day == selectedDay
                                    DayCell(
                                        day        = day,
                                        isSelected = isSelected,
                                        isToday    = isToday,
                                        events     = events,
                                        onClick    = { selectedDay = day },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend + detail panel
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
            ) {

                // Legend
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    LegendItem(color = UrgentRed,    label = "1–3 days")
                    LegendItem(color = WarnOrange,   label = "4–7 days")
                    LegendItem(color = SafeGreen,    label = "8+ days")
                    LegendItem(color = ExpiredGrey,  label = "Expired")
                }

                // selected day detail panel
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedDay != null) {
                    val events = aprilExpiryMap[selectedDay]
                    if (events != null) {
                        ElevatedCard(
                            shape  = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text  = "APRIL $selectedDay ITEMS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight    = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                    ),
                                    color = Color(0xFF0F6E56),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    events.forEach { event ->
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text  = event.foodName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                            ) {
                                                Text(
                                                    text     = event.category,
                                                    style    = MaterialTheme.typography.labelSmall,
                                                    color    = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical   = 4.dp,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text     = "No items expiring on April $selectedDay",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

//DayCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<ExpiryEvent>,
    onClick: () -> Unit,
) {
    // determine dot color based on urgency
    val dotColor: Color = when {
        events.isEmpty()      -> Color.Transparent
        day < TODAY_DAY       -> ExpiredGrey
        day == TODAY_DAY      -> TodayGreen
        else                  -> {
            val diff = day - TODAY_DAY
            when (diff) {
                in 1..3  -> UrgentRed
                in 4..7  -> WarnOrange
                else          -> SafeGreen
            }
        }
    }

    val bgColor: Color = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday    -> TodayGreen.copy(alpha = 0.12f)
        else       -> Color.Transparent
    }

    val border: BorderStroke? = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    val textColor: Color = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday    -> TodayGreen
        else       -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(8.dp),
        color   = bgColor,
        border  = border,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text  = day.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor,
            )
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    events.take(3).forEach { _ ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(color = dotColor, shape = CircleShape),
                        )
                    }
                }
            }
        }
    }
}

//Legend item

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape),
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewExpiryCalendarLight() {
    SmartPantryTheme {
        ExpiryCalendarScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewExpiryCalendarDark() {
    SmartPantryTheme(darkTheme = true) {
        ExpiryCalendarScreen(onNavigateBack = {})
    }
}
