package com.lab03g10.smartpantry.ui.screens

// TODO: replace hardcoded list with NotificationRepository backed by FCM push tokens + Room; unread count should drive the badge on the Dashboard bell icon

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab03g10.smartpantry.data.local.entity.NotificationEntity
import com.lab03g10.smartpantry.ui.components.EmptyState
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.NotificationsViewModel
import java.util.concurrent.TimeUnit

// Design tokens
private val SectionLabelGreen = Color(0xFF0F6E56)
private val LabelGrey         = Color(0xFF888888)

private val TempAlertCircleBg     = Color(0xFFFFEDEA)
private val TempAlertIcon         = Color(0xFFBA1A1A)
private val LightAlertCircleBg    = Color(0xFFFFF9C4)   // warm yellow
private val LightAlertIcon        = Color(0xFFF57F17)    // amber
private val HumidityAlertCircleBg = Color(0xFFE3F2FD)   // pale blue
private val HumidityAlertIcon     = Color(0xFF1976D2)    // blue 700
private val ExpiryCircleBg        = Color(0xFFFFF3E0)
private val ExpiryIcon            = Color(0xFFF57F17)
private val LowStockCircleBg      = Color(0xFFE8F5E9)
private val LowStockIcon          = Color(0xFF2E7D32)

// Notification type — mapped from NotificationEntity.type string in toIconBucket().
private enum class NotifType { TEMP_ALERT, HUMIDITY_ALERT, LIGHT_ALERT, EXPIRY, LOW_STOCK, SHOPPING }

private fun NotificationEntity.toIconBucket(): NotifType = when (type) {
    "EXPIRY_SOON"   -> NotifType.EXPIRY
    "LOW_STOCK"     -> NotifType.LOW_STOCK
    "SHOPPING"      -> NotifType.SHOPPING
    "SENSOR_BREACH" -> sensorBreachBucket(body)
    else            -> NotifType.EXPIRY
}

// SensorTickWorker.describeBreach() prefixes each part with "Temperature ",
// "Humidity ", or "Light "; multi-dimension breaches join parts with "; ".
// Priority order temp > humidity > light keeps the food-safety-critical
// dimension on the icon when more than one breach fires at once.
private fun sensorBreachBucket(body: String): NotifType = when {
    "Temperature" in body -> NotifType.TEMP_ALERT
    "Humidity" in body    -> NotifType.HUMIDITY_ALERT
    "Light" in body       -> NotifType.LIGHT_ALERT
    else                  -> NotifType.TEMP_ALERT
}

/**
 * "1h ago" / "3d ago" / "just now" style relative time. Falls back to "Nd ago"
 * for anything older than a day; we don't bother with absolute dates since the
 * Today / Earlier section split already establishes whether the row is recent.
 */
private fun formatTimeAgo(nowMillis: Long, createdAt: Long): String {
    val deltaMs = (nowMillis - createdAt).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(deltaMs)
    val hours   = TimeUnit.MILLISECONDS.toHours(deltaMs)
    val days    = TimeUnit.MILLISECONDS.toDays(deltaMs)
    return when {
        minutes < 1L -> "just now"
        minutes < 60L -> "${minutes}m ago"
        hours < 24L -> "${hours}h ago"
        days == 1L -> "1 day ago"
        else -> "$days days ago"
    }
}

// Public composable

/**
 * Notification screen — driven by NotificationsViewModel.
 *
 * Two sections: TODAY and EARLIER. Tap-on-row marks the row read and (if the
 * notification has a deep-link target) navigates to the related food or
 * sensor screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFood: (String) -> Unit = {},
    onNavigateToSensor: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Snapshot once per composition so every row in the same pass uses the
    // same "now" for relative-time labels.
    val now = remember(state) { System.currentTimeMillis() }

    val onRowTap: (NotificationEntity) -> Unit = remember(viewModel) {
        { notif ->
            viewModel.markOneRead(notif.id)
            // Deep-link priority: food first, then sensor. Notifications that
            // carry neither just get marked read in place.
            when {
                notif.relatedFoodId != null   -> onNavigateToFood(notif.relatedFoodId)
                notif.relatedSensorId != null -> onNavigateToSensor(notif.relatedSensorId)
            }
        }
    }

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
                    val hasUnread = state.unreadCount > 0
                    TextButton(
                        onClick = { viewModel.markAllRead() },
                        enabled = hasUnread,
                    ) {
                        Text(
                            text  = "Mark all read",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = if (hasUnread) SectionLabelGreen
                                    else SectionLabelGreen.copy(alpha = 0.4f),
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
        if (state.today.isEmpty() && state.earlier.isEmpty()) {
            EmptyState(
                icon           = Icons.Outlined.CheckCircle,
                title          = "All caught up",
                supportingText = "New alerts about temperature, expiry, or low stock will appear here.",
                modifier       = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.today.isNotEmpty()) {
                stickyHeader { GroupHeader(label = "TODAY") }
                items(state.today, key = { it.id }) { notif ->
                    NotifItemRow(
                        notif    = notif,
                        timeAgo  = formatTimeAgo(now, notif.createdAt),
                        onClick  = { onRowTap(notif) },
                    )
                    if (notif != state.today.last()) {
                        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    }
                }
            }

            if (state.earlier.isNotEmpty()) {
                stickyHeader { GroupHeader(label = "EARLIER") }
                items(state.earlier, key = { it.id }) { notif ->
                    NotifItemRow(
                        notif    = notif,
                        timeAgo  = formatTimeAgo(now, notif.createdAt),
                        onClick  = { onRowTap(notif) },
                    )
                    if (notif != state.earlier.last()) {
                        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    }
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
private fun NotifItemRow(
    notif: NotificationEntity,
    timeAgo: String,
    onClick: () -> Unit,
) {
    val (circleBg, iconTint, icon) = notifTypeAssets(notif.toIconBucket())
    val isUnread = !notif.read

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                text  = notif.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text     = notif.body,
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
                text  = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey,
            )
            if (isUnread) {
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
    NotifType.TEMP_ALERT     -> NotifAssets(TempAlertCircleBg,     TempAlertIcon,     Icons.Outlined.Thermostat)
    NotifType.HUMIDITY_ALERT -> NotifAssets(HumidityAlertCircleBg, HumidityAlertIcon, Icons.Outlined.WaterDrop)
    NotifType.LIGHT_ALERT    -> NotifAssets(LightAlertCircleBg,    LightAlertIcon,    Icons.Outlined.LightMode)
    NotifType.EXPIRY         -> NotifAssets(ExpiryCircleBg,        ExpiryIcon,        Icons.Outlined.Schedule)
    NotifType.LOW_STOCK      -> NotifAssets(LowStockCircleBg,      LowStockIcon,      Icons.Outlined.Inventory2)
    NotifType.SHOPPING       -> NotifAssets(LowStockCircleBg,      LowStockIcon,      Icons.Filled.ShoppingCart)
}

// Previews

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewNotificationsLight() {
    SmartPantryTheme {
        Text("Notifications preview uses live app data at runtime.")
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
        Text("Notifications preview uses live app data at runtime.")
    }
}
