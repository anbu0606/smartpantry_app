package com.lab03g10.smartpantry.ui.screens

// TODO: Assessment 3: replace hardcoded list with NotificationRepository backed by FCM push tokens + Room; unread count should drive the badge on the Dashboard bell icon

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// Design tokens
private val SectionLabelGreen = Color(0xFF0F6E56)
private val LabelGrey         = Color(0xFF888888)

private val TempAlertCircleBg  = Color(0xFFFFEDEA)
private val TempAlertIcon      = Color(0xFFBA1A1A)
private val LightAlertCircleBg = Color(0xFFFFF9C4)   // warm yellow
private val LightAlertIcon     = Color(0xFFF57F17)    // amber
private val ExpiryCircleBg     = Color(0xFFFFF3E0)
private val ExpiryIcon         = Color(0xFFF57F17)
private val LowStockCircleBg   = Color(0xFFE8F5E9)
private val LowStockIcon       = Color(0xFF2E7D32)

// Notification type
private enum class NotifType { TEMP_ALERT, LIGHT_ALERT, EXPIRY, LOW_STOCK }

// Notification model
private data class NotifItem(
    val type: NotifType,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val isUnread: Boolean = true,
)

// Hardcoded notification data
private val todayNotifs = listOf(
    NotifItem(
        type     = NotifType.LIGHT_ALERT,
        title    = "Pantry Light Exposure Alert",
        subtitle = "Pantry reached 420 lx — above 400 lx threshold",
        timeAgo  = "1h ago",
        isUnread = true,
    ),
    NotifItem(
        type     = NotifType.TEMP_ALERT,
        title    = "Pantry Temperature Alert",
        subtitle = "Pantry reached 29.5 °C — above 28 °C threshold",
        timeAgo  = "2h ago",
        isUnread = true,
    ),
    NotifItem(
        type     = NotifType.EXPIRY,
        title    = "Milk expiring soon",
        subtitle = "Expires today — use or discard immediately",
        timeAgo  = "5h ago",
        isUnread = true,
    ),
    NotifItem(
        type     = NotifType.EXPIRY,
        title    = "Butter expiring soon",
        subtitle = "Expires today — 250g remaining",
        timeAgo  = "5h ago",
        isUnread = true,
    ),
)

private val yesterdayNotifs = listOf(
    NotifItem(
        type     = NotifType.LOW_STOCK,
        title    = "Running low: Chicken Breast",
        subtitle = "Only 500g remaining in Fridge",
        timeAgo  = "1 day ago",
        isUnread = false,
    ),
    NotifItem(
        type     = NotifType.EXPIRY,
        title    = "Spinach expiring soon",
        subtitle = "Expires in 2 days — 200g in Fridge",
        timeAgo  = "1 day ago",
        isUnread = false,
    ),
)

private val thisWeekNotifs = listOf(
    NotifItem(
        type     = NotifType.TEMP_ALERT,
        title    = "Fridge Temperature Spike",
        subtitle = "Fridge briefly reached 8.5 °C at 20:00",
        timeAgo  = "3 days ago",
        isUnread = false,
    ),
    NotifItem(
        type     = NotifType.LOW_STOCK,
        title    = "Running low: Basmati Rice",
        subtitle = "Less than 500g remaining in Pantry",
        timeAgo  = "5 days ago",
        isUnread = false,
    ),
)

// Public composable

/**
 * Notification screen
 *
 * Shows notifications grouped as:
 * - TODAY
 * - YESTERDAY
 * - THIS WEEK
 *
 * @param onNavigateBack -> used when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Notifications",
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
                    TextButton(
                        // TODO: Assessment 3: replace hardcoded list with NotificationRepository backed by FCM push tokens + Room; unread count should drive the badge on the Dashboard bell icon
                        onClick = {},
                    ) {
                        Text(
                            text  = "Mark all read",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = SectionLabelGreen,
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
        // Forward-compat: once Abinaya replaces the hardcoded lists with VM
        // state, an empty inbox renders the "all caught up" hint instead of
        // three orphan sticky headers.
        val allEmpty = todayNotifs.isEmpty() && yesterdayNotifs.isEmpty() && thisWeekNotifs.isEmpty()
        if (allEmpty) {
            EmptyState(
                icon           = Icons.Outlined.CheckCircle,
                title          = "All caught up",
                supportingText = "New alerts about temperature, expiry, or low stock will appear here.",
                modifier       = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            stickyHeader {
                GroupHeader(label = "TODAY")
            }
            items(todayNotifs) { notif ->
                NotifItemRow(item = notif)
                if (notif != todayNotifs.last()) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }

            stickyHeader {
                GroupHeader(label = "YESTERDAY")
            }
            items(yesterdayNotifs) { notif ->
                NotifItemRow(item = notif)
                if (notif != yesterdayNotifs.last()) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }

            stickyHeader {
                GroupHeader(label = "THIS WEEK")
            }
            items(thisWeekNotifs) { notif ->
                NotifItemRow(item = notif)
                if (notif != thisWeekNotifs.last()) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

// Group header

@Composable
private fun GroupHeader(label: String) {
    Text(
        text     = label,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.sp,
        ),
        color = SectionLabelGreen,
    )
}

// Notification row

@Composable
private fun NotifItemRow(item: NotifItem) {
    val (circleBg, iconTint, icon) = notifTypeAssets(item.type)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(20.dp),
            )
        }

        Column(
            modifier            = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text  = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text     = item.subtitle,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text  = item.timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey,
            )
            if (item.isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

// Type asset resolver

private data class NotifAssets(
    val circleBg: Color,
    val iconTint: Color,
    val icon: ImageVector,
)

@Composable
private fun notifTypeAssets(type: NotifType): NotifAssets = when (type) {
    NotifType.TEMP_ALERT  -> NotifAssets(TempAlertCircleBg,  TempAlertIcon,  Icons.Outlined.Thermostat)
    NotifType.LIGHT_ALERT -> NotifAssets(LightAlertCircleBg, LightAlertIcon, Icons.Outlined.LightMode)
    NotifType.EXPIRY      -> NotifAssets(ExpiryCircleBg,     ExpiryIcon,     Icons.Outlined.Schedule)
    NotifType.LOW_STOCK   -> NotifAssets(LowStockCircleBg,   LowStockIcon,   Icons.Outlined.Inventory2)
}

// Previews

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewNotificationsLight() {
    SmartPantryTheme {
        NotificationsScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewNotificationsDark() {
    SmartPantryTheme(darkTheme = true) {
        NotificationsScreen(onNavigateBack = {})
    }
}
