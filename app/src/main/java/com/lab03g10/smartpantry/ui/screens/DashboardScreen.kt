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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

private val AvatarGreen    = Color(0xFF1D9E75)
private val SeeAllGreen    = Color(0xFF0F6E56)
private val WarningAmber   = Color(0xFFF57F17)
private val StatTileBg     = Color(0xFFF5F5F5)
private val CardBorder     = Color(0xFFEBEBEB)
private val LabelGrey      = Color(0xFF888888)
private val ShortcutChipBg = Color(0xFFC8E6C9)
private val ShortcutIcon   = Color(0xFF085041)

private data class SensorLocation(
    val name: String,
    val temperature: Float,
    val humidity: Float,
    val lux: Float,
    val isNormal: Boolean,
)

private val sensorLocations = listOf(
    SensorLocation("Fridge",    4.2f,  58f,  12f,  true),
    SensorLocation("Pantry",   26.5f,  63f, 285f,  false),
    SensorLocation("Cupboard", 21.0f,  45f,  38f,  true),
)

// urgency card data
private data class UrgencyItem(
    val emoji: String,
    val name: String,
    val badge: String,
    val isErrorBadge: Boolean,
)

private val sampleUrgencyItems = listOf(
    UrgencyItem("🥛", "Milk",           "Expires today", true),
    UrgencyItem("🍗", "Chicken Breast", "1 day left",    false),
    UrgencyItem("🥬", "Spinach",        "2 days left",   false),
)


/**
 * Dashboard - landing view after sign in
 * Context-awareness:
 * Combined with the sensor page , the dashboard
 * Layout:
 * CenterAlignedTopAppBar  "Smart Pantry"
 * LazyColumn

 * ```
 * @param onNavigateToStorageMonitor  Tap any sensor card moves to Storage Monitor screen.
 * @param onNavigateToShoppingList    Tap shortcut card moves toShopping List screen.
 * @param onNavigateToAddSensor       Tap the "+" add card or dot goes to Add New Sensor screen.
 * @param onNavigateToNotifications   Tap bell icon opens the Notifications screen.
 * @param onNavigateToExpiryCalendar  Tap calendar shortcut goes to Expiry Calendar screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToStorageMonitor: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToAddSensor: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExpiryCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val pagerState    = rememberPagerState(pageCount = { sensorLocations.size + 1 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar         = { DashboardTopBar(onNavigateToNotifications = onNavigateToNotifications, onNavigateToProfile) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            // Time-aware greeting
            item {
                TimeGreeting(modifier = Modifier.padding(horizontal = 16.dp))
            }

            //Swipeable sensor cards - sensory context
            // Each page will have one storage location. Tapping navigates to the full Storage Monitor screen for detailed historical charts.
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state    = pagerState,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) { page ->
                        if (page < sensorLocations.size) {
                            val loc = sensorLocations[page]
                            TemperatureCard(
                                locationName = loc.name,
                                temperature  = loc.temperature,
                                humidity     = loc.humidity,
                                lux          = loc.lux,
                                isNormal     = loc.isNormal,
                                modifier     = Modifier.clickable { onNavigateToStorageMonitor() },
                            )
                        } else {
                            // Last page - pair a new sensor
                            AddSensorCard(onClick = onNavigateToAddSensor)
                        }
                    }

                    // dots for sensor pages + "+" for the add pages
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        // One dot per real sensor location
                        repeat(sensorLocations.size) { index ->
                            val selected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (selected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    ),
                            )
                        }

                        Box(modifier = Modifier.padding(horizontal = 4.dp))

                        // "+" circle actives when on the add-sensor page
                        val addSelected = pagerState.currentPage == sensorLocations.size
                        Box(
                            modifier         = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (addSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(sensorLocations.size)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Add,
                                contentDescription = "Add sensor",
                                tint               = if (addSelected) {
                                    androidx.compose.ui.graphics.Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            }

            item { UseTodaySection(onNavigateToInventory) }

            // Inventory stats
            item { InventoryOverviewSection() }

            // Shopping list shortcuts
            item { ShoppingListShortcut(onClick = onNavigateToShoppingList) }

            // Expiry calendar shortcut
            item { ExpiryCalendarShortcut(onClick = onNavigateToExpiryCalendar) }
        }
    }
}

//  Time-aware greeting
@Composable
private fun TimeGreeting(modifier: Modifier = Modifier) {
    val (greeting, subtitle) = remember {
        val hour = LocalTime.now().hour
        val day  = LocalDate.now().dayOfWeek

        val g = when (hour) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Good night"
        }

        val s = when {
            day == DayOfWeek.FRIDAY && hour >= 15        -> "Friday evening - great time to restock"
            day in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekend - 3 items expiring soon"
            hour in 6..9                                 -> "3 items need using today"
            hour in 17..20                               -> "Dinner time - check what to cook"
            else                                         -> "3 items need attention today"
        }

        g to s
    }

    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text  = greeting,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Light,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

//Top app bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text  = "Smart Pantry",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    imageVector        = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier         = Modifier
                    .clickable{
                        onNavigateToProfile()
                    }
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AvatarGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "A",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

//Use Today" section
@Composable
private fun UseTodaySection(onNavigateToInventory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text  = "⚠",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text  = "Use Today",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            TextButton(
                onClick        = { onNavigateToInventory() },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    text  = "See all",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = SeeAllGreen,
                )
            }
        }

        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sampleUrgencyItems) { item ->
                UrgencyCard(
                    emoji      = item.emoji,
                    name       = item.name,
                    badge      = item.badge,
                    badgeColor = if (item.isErrorBadge) MaterialTheme.colorScheme.error
                                 else WarningAmber,
                )
            }
        }
    }
}

@Composable
private fun UrgencyCard(
    emoji: String,
    name: String,
    badge: String,
    badgeColor: Color,
) {
    Surface(
        modifier       = Modifier.size(width = 140.dp, height = 88.dp),
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerLowest,
        border         = BorderStroke(0.5.dp, CardBorder),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text     = name,
                style    = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(color = badgeColor, shape = CircleShape) {
                Text(
                    text     = badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style    = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}

//Inventory Overview section
@Composable
private fun InventoryOverviewSection() {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text  = "Inventory Overview",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile("24", "Total Items",    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatTile("3",  "Expiring Today", MaterialTheme.colorScheme.error,   Modifier.weight(1f))
            StatTile("7",  "This Week",      WarningAmber,                       Modifier.weight(1f))
        }
    }
}

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
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                ),
                color = valueColor,
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

//Shopping list shortcut card

@Composable
private fun ShoppingListShortcut(onClick: () -> Unit) {
    Surface(
        onClick        = onClick,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerLowest,
        border         = BorderStroke(0.5.dp, CardBorder),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ShortcutChipBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint               = ShortcutIcon,
                        modifier           = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = "Shopping List",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = "View shopping list",
                        style = MaterialTheme.typography.bodySmall,
                        color = LabelGrey,
                    )
                }
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to shopping list",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

//Add sensor card
/**
 * Last page of the Dashboard sensor pager
 * Tapping navigates to AddSensorScreen
 */
@Composable
private fun AddSensorCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(28.dp),
        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border   = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        ),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(30.dp),
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(14.dp))
            Text(
                text  = "Add New Sensor",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
            Text(
                text      = "Pair a new storage environment monitor",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

//expiry calendar shortcut card

@Composable
private fun ExpiryCalendarShortcut(onClick: () -> Unit) {
    Surface(
        onClick        = onClick,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerLowest,
        border         = BorderStroke(0.5.dp, CardBorder),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ShortcutChipBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint               = ShortcutIcon,
                        modifier           = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = "Expiry Calendar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = "2 items expiring this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = LabelGrey,
                    )
                }
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to expiry calendar",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}



// ==== Previews ==========================================================

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewDashboardLight() {
    SmartPantryTheme {
        DashboardScreen(
            onNavigateToStorageMonitor = {},
            onNavigateToShoppingList   = {},
            onNavigateToAddSensor      = {},
            onNavigateToNotifications  = {},
            onNavigateToExpiryCalendar = {},
            onNavigateToProfile = {},
            onNavigateToInventory = {},
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
private fun PreviewDashboardDark() {
    SmartPantryTheme(darkTheme = true) {
        DashboardScreen(
            onNavigateToStorageMonitor = {},
            onNavigateToShoppingList   = {},
            onNavigateToAddSensor      = {},
            onNavigateToNotifications  = {},
            onNavigateToExpiryCalendar = {},
            onNavigateToProfile = {},
            onNavigateToInventory = {},
        )
    }
}
