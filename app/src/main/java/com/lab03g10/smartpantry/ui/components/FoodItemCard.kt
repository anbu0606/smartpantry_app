package com.lab03g10.smartpantry.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// -- Background colours
private val DairyBg    = Color(0xFFE1F5EE)
private val MeatBg     = Color(0xFFFBE9F0)
private val ProduceBg  = Color(0xFFEAF3DE)
private val DryGoodsBg = Color(0xFFFEF3E2)
private val FallbackBg = Color(0xFFEEEEEE)

// -- Public composable ---------------------------------------------------------

/**
 * swipe-to-delete food item card
 *
 * @param foodName       Display name of the food
 * @param category
 * @param quantity       Amount in stock
 * @param expiryDate     Expiry string.
 * @param daysUntilExpiry Days remaining.
 * @param onEdit         Called when the card is tapped.
 * @param onDelete       Called when gesture completes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemCard(
    foodName: String,
    category: String,
    quantity: String,
    expiryDate: String,
    daysUntilExpiry: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: replace single onDelete with a two-option
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        // Require 40% swipe
        positionalThreshold = { totalDistance -> totalDistance * 0.40f },
    )

    SwipeToDismissBox(
        state                    = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent        = { DeleteReveal() },
        modifier                 = modifier.fillMaxWidth(),
    ) {
        CardContent(
            foodName        = foodName,
            category        = category,
            quantity        = quantity,
            expiryDate      = expiryDate,
            daysUntilExpiry = daysUntilExpiry,
            onClick         = onEdit,
        )
    }
}

// -- Internal composables

/** Red background and delete icon revealed on swipe. */
@Composable
private fun DeleteReveal() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector        = Icons.Filled.Delete,
            contentDescription = "Delete item",
            tint               = MaterialTheme.colorScheme.error,
            modifier           = Modifier.padding(end = 20.dp),
        )
    }
}

/** The visible card face */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardContent(
    foodName: String,
    category: String,
    quantity: String,
    expiryDate: String,
    daysUntilExpiry: Int,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        shape   = RoundedCornerShape(28.dp),
        colors  = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            // -- Category icon square
            CategoryIcon(category = category)

            Spacer(modifier = Modifier.width(16.dp))

            // -- Centre: name + data
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text     = foodName,
                    style    = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,   // weight 500 per spec
                    ),
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text     = "$category · $quantity · $expiryDate",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // -- Trailing: expiry status
            ExpiryBadge(daysUntilExpiry = daysUntilExpiry)
        }
    }
}

/**
 * 44×44dp rounded square showing a category emoji.
 */
@Composable
private fun CategoryIcon(category: String) {
    val (bgColor, emoji) = when (category) {
        "Dairy"                -> DairyBg    to "🥛"
        "Meat"                 -> MeatBg     to "🥩"
        "Produce"              -> ProduceBg  to "🥦"
        "Dry Goods", "DryGoods" -> DryGoodsBg to "🌾"
        else                   -> FallbackBg  to "🍽️"
    }

    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text     = emoji,
            fontSize = 22.sp,
        )
    }
}

/**
 * Expiry status badge.
 */
@Composable
private fun ExpiryBadge(daysUntilExpiry: Int) {
    val containerColor = when {
        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.error
        daysUntilExpiry <= 3 -> MaterialTheme.colorScheme.tertiary
        else                 -> MaterialTheme.colorScheme.primary
    }
    val textColor = when {
        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.onError
        daysUntilExpiry <= 3 -> MaterialTheme.colorScheme.onTertiary
        else                 -> MaterialTheme.colorScheme.onPrimary
    }
    val label = when {
        daysUntilExpiry <= 0 -> "TODAY"
        daysUntilExpiry <= 3 -> "${daysUntilExpiry}D"
        else                 -> "SAFE"
    }

    Surface(
        color  = containerColor,
        shape  = CircleShape,
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            ),
            color    = textColor,
        )
    }
}

// -- Previews ------------------------------------------------------------------

@Preview(name = "Light · Dairy · Expired", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewExpiredLight() {
    SmartPantryTheme {
        FoodItemCard(
            foodName        = "Milk",
            category        = "Dairy",
            quantity        = "1L",
            expiryDate      = "Apr 17",
            daysUntilExpiry = 0,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(name = "Light · Meat · Warning 1D", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewWarning1DLight() {
    SmartPantryTheme {
        FoodItemCard(
            foodName        = "Chicken Breast",
            category        = "Meat",
            quantity        = "500g",
            expiryDate      = "Apr 18",
            daysUntilExpiry = 1,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(name = "Light · Produce · Warning 3D", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewWarning3DLight() {
    SmartPantryTheme {
        FoodItemCard(
            foodName        = "Spinach",
            category        = "Produce",
            quantity        = "200g",
            expiryDate      = "Apr 20",
            daysUntilExpiry = 3,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(name = "Light · Dry Goods · Safe", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewSafeLight() {
    SmartPantryTheme {
        FoodItemCard(
            foodName        = "Pasta",
            category        = "Dry Goods",
            quantity        = "1kg",
            expiryDate      = "Jun 30",
            daysUntilExpiry = 74,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(
    name            = "Dark · Dairy · Expired",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewExpiredDark() {
    SmartPantryTheme(darkTheme = true) {
        FoodItemCard(
            foodName        = "Eggs",
            category        = "Dairy",
            quantity        = "6 units",
            expiryDate      = "Apr 17",
            daysUntilExpiry = 0,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(
    name            = "Dark · Meat · Warning",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWarningDark() {
    SmartPantryTheme(darkTheme = true) {
        FoodItemCard(
            foodName        = "Salmon",
            category        = "Meat",
            quantity        = "300g",
            expiryDate      = "Apr 19",
            daysUntilExpiry = 2,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}

@Preview(
    name            = "Dark · Produce · Safe",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSafeDark() {
    SmartPantryTheme(darkTheme = true) {
        FoodItemCard(
            foodName        = "Apples",
            category        = "Produce",
            quantity        = "4 units",
            expiryDate      = "May 10",
            daysUntilExpiry = 23,
            onEdit          = {},
            onDelete        = {},
            modifier        = Modifier.padding(12.dp),
        )
    }
}
