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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.CalendarDayCell
import com.lab03g10.smartpantry.ui.viewmodel.ExpiryCalendarViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

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

private val MonthHeaderFmt   = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val PanelHeaderFmt   = DateTimeFormatter.ofPattern("MMMM d 'ITEMS'", Locale.ENGLISH)
private val EmptyPanelFmt    = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryCalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpiryCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                IconButton(onClick = viewModel::onPrevMonth) {
                    Icon(
                        imageVector        = Icons.Filled.ChevronLeft,
                        contentDescription = "Previous month",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text  = state.displayedMonth.format(MonthHeaderFmt),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = viewModel::onNextMonth) {
                    Icon(
                        imageVector        = Icons.Filled.ChevronRight,
                        contentDescription = "Next month",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Day of week header row — Monday-first to match ViewModel grid.
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
            val selectedDayNum = state.selectedDay?.dayOfMonth
            Column(
                modifier            = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                state.weeks.forEach { week ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        week.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                            ) {
                                if (cell != null) {
                                    DayCell(
                                        cell       = cell,
                                        isSelected = cell.dayOfMonth == selectedDayNum,
                                        onClick    = { viewModel.onDayTap(cell.dayOfMonth) },
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

                // selected day detail panel — driven by state.selectedDay rather
                // than the previous hardcoded aprilExpiryMap lookup.
                Spacer(modifier = Modifier.height(12.dp))

                val selected = state.selectedDay
                if (selected != null) {
                    if (selected.events.isNotEmpty()) {
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
                                    text  = selected.date.format(PanelHeaderFmt).uppercase(Locale.ENGLISH),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight    = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                    ),
                                    color = Color(0xFF0F6E56),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selected.events.forEach { food ->
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text  = food.name,
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
                                                    text     = food.category,
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
                            text     = "No items expiring on ${selected.date.format(EmptyPanelFmt)}",
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
    cell: CalendarDayCell,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dotColor: Color = cell.mostUrgentColor ?: Color.Transparent

    val bgColor: Color = when {
        isSelected   -> MaterialTheme.colorScheme.primaryContainer
        cell.isToday -> TodayGreen.copy(alpha = 0.12f)
        else         -> Color.Transparent
    }

    val border: BorderStroke? = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    val textColor: Color = when {
        isSelected   -> MaterialTheme.colorScheme.onPrimaryContainer
        cell.isToday -> TodayGreen
        else         -> MaterialTheme.colorScheme.onSurface
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
                text  = cell.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor,
            )
            if (cell.events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    // Up to 3 dots, all in the most-urgent colour (matches A2
                    // visual rhythm but driven by the real food list).
                    repeat(cell.events.size.coerceAtMost(3)) {
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
        Text("Expiry-calendar preview uses live app data at runtime.")
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
        Text("Expiry-calendar preview uses live app data at runtime.")
    }
}
