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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.ui.components.TemperatureCard
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.DashboardViewModel
import com.lab03g10.smartpantry.ui.viewmodel.SensorCardUi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private val AvatarGreen    = Color(0xFF1D9E75)
private val SeeAllGreen    = Color(0xFF0F6E56)
private val WarningAmber   = Color(0xFFF57F17)
private val StatTileBg     = Color(0xFFF5F5F5)
private val CardBorder     = Color(0xFFEBEBEB)
private val LabelGrey      = Color(0xFF888888)
private val ShortcutChipBg = Color(0xFFC8E6C9)
private val ShortcutIcon   = Color(0xFF085041)

// Lightweight in-screen DTO for the "Use Today" LazyRow — derived from
// FoodItemEntity at render time so the screen doesn't leak entity coupling
// into the card composable.
private data class UrgencyItem(
    val emoji: String,
    val name: String,
    val badge: String,
    val isErrorBadge: Boolean,
)

private fun FoodItemEntity.toUrgencyItem(nowMillis: Long): UrgencyItem {
    val daysLeft = TimeUnit.MILLISECONDS.toDays(effectiveExpiryDate - nowMillis)
    val badge = when {
        daysLeft <= 0L -> "Expires today"
        daysLeft == 1L -> "1 day left"
        else           -> "$daysLeft days left"
    }
    val emoji = when (category) {
        "Dairy"     -> "🥛"
        "Meat"      -> "🍗"
        "Produce"   -> "🥬"
        "Grains", "Dry Goods" -> "🍞"
        "Frozen"    -> "🧊"
        else        -> "🥫"
    }
    return UrgencyItem(emoji = emoji, name = name, badge = badge, isErrorBadge = daysLeft <= 0L)
}


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
    onNavigateToInventory: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pageCount      = state.sensorCards.size + 1
    val pagerState     = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    val addPageIndex   = state.sensorCards.size

    Scaffold(
        topBar         = {
            DashboardTopBar(
                unreadCount = state.unreadNotificationCount,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile,
            )
        },
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
                TimeGreeting(
                    useTodayCount = state.useTodayItems.size,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            //Swipeable sensor cards - sensory context
            // Each page will have one storage location. Tapping navigates to the full Storage Monitor screen for detailed historical charts.
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state    = pagerState,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) { page ->
                        if (page < state.sensorCards.size) {
                            val card = state.sensorCards[page]
                            TemperatureCard(
                                locationName = card.displayName,
                                temperature  = card.temperatureC,
                                humidity     = card.humidity,
                                lux          = card.lux,
                                isNormal     = card.isNormal,
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
                        repeat(state.sensorCards.size) { index ->
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
                        val addSelected = pagerState.currentPage == addPageIndex
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
                                        pagerState.animateScrollToPage(addPageIndex)
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

            item {
                UseTodaySection(
                    items = state.useTodayItems,
                    onNavigateToInventory = onNavigateToInventory,
                )
            }

            // Inventory stats
            item {
                InventoryOverviewSection(
                    totalItemCount     = state.totalItemCount,
                    expiringTodayCount = state.expiringTodayCount,
                    expiringWeekCount  = state.expiringThisWeekCount,
                )
            }

            // Shopping list shortcuts
            item { ShoppingListShortcut(onClick = onNavigateToShoppingList) }

            // Expiry calendar shortcut
            item {
                ExpiryCalendarShortcut(
                    expiringWeekCount = state.expiringThisWeekCount,
                    onClick = onNavigateToExpiryCalendar,
                )
            }
        }
    }
}

//  Time-aware greeting
@Composable
private fun TimeGreeting(
    useTodayCount: Int,
    modifier: Modifier = Modifier,
) {
    val (greeting, subtitle) = remember(useTodayCount) {
        val hour = LocalTime.now().hour
        val day  = LocalDate.now().dayOfWeek

        val g = when (hour) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Good night"
        }

        // Pluralise around the live count so "1 item" / "0 items" reads naturally.
        val noun = if (useTodayCount == 1) "item" else "items"

        val s = when {
            day == DayOfWeek.FRIDAY && hour >= 15               -> "Friday evening — great time to restock"
            day in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekend — $useTodayCount $noun expiring soon"
            hour in 6..9                                        -> "$useTodayCount $noun need using today"
            hour in 17..20                                      -> "Dinner time — check what to cook"
            else                                                -> "$useTodayCount $noun need attention today"
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
    unreadCount: Int,
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
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Notifications,
                        contentDescription = if (unreadCount > 0) {
                            "Notifications, $unreadCount unread"
                        } else {
                            "Notifications"
                        },
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
private fun UseTodaySection(
    items: List<FoodItemEntity>,
    onNavigateToInventory: () -> Unit,
) {
    val now = remember(items) { System.currentTimeMillis() }
    val urgencyItems = remember(items, now) { items.map { it.toUrgencyItem(now) } }

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
            items(urgencyItems) { item ->
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
private fun InventoryOverviewSection(
    totalItemCount: Int,
    expiringTodayCount: Int,
    expiringWeekCount: Int,
) {
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
            StatTile(totalItemCount.toString(),     "Total Items",    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatTile(expiringTodayCount.toString(), "Expiring Today", MaterialTheme.colorScheme.error,   Modifier.weight(1f))
            StatTile(expiringWeekCount.toString(),  "This Week",      WarningAmber,                      Modifier.weight(1f))
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
private fun ExpiryCalendarShortcut(
    expiringWeekCount: Int,
    onClick: () -> Unit,
) {
    val noun = if (expiringWeekCount == 1) "item" else "items"
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
                        text  = "$expiringWeekCount $noun expiring this week",
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
        Text("Dashboard preview uses live app data at runtime.")
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
        Text("Dashboard preview uses live app data at runtime.")
    }
}
