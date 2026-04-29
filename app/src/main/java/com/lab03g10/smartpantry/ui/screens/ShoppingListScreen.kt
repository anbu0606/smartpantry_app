package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

private val SectionHeaderGreen  = Color(0xFF0F6E56)
private val ClearAllRed         = Color(0xFFB71C1C)
private val CheckedGreen        = Color(0xFF2E7D32)
private val ChipBg              = Color(0xFFC8E6C9)
private val ChipText            = Color(0xFF085041)
private val ChipCheckedBg       = Color(0xFFE8E8E8)
private val FabBg               = Color(0xFFC8E6C9)
private val FabIcon             = Color(0xFF085041)
private val SubtitleGrey        = Color(0xFF888888)
private val LowStockAmber       = Color(0xFF923357)

private data class ShoppingItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val quantity: String,
    val section: Section,
)

private enum class Section { OUT_OF_STOCK, RUNNING_LOW }

// sample data for prototype
private val allItems = listOf(
    ShoppingItem("milk",    "Full Cream Milk", "Dairy · 1L",              "×2", Section.OUT_OF_STOCK),
    ShoppingItem("eggs",    "Eggs",            "Dairy · 12pcs",           "×1", Section.OUT_OF_STOCK),
    ShoppingItem("chicken", "Chicken Breast",  "Meat · 500g",             "×1", Section.OUT_OF_STOCK),
    ShoppingItem("spinach", "Spinach",         "Produce · 2 remaining",   "×1", Section.RUNNING_LOW),
    ShoppingItem("yogurt",  "Greek Yogurt",    "Dairy · 1 remaining",     "×1", Section.RUNNING_LOW),
    ShoppingItem("bread",   "Bread",           "Dry Goods · 2 remaining", "×1", Section.RUNNING_LOW),
)

// Full Cream Milk starts checked to demonstrate the checked state design
private val initialChecked = setOf("milk")

/**
 * Shopping list screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListScreen(onNavigateBack: () -> Unit) {
    val items      = remember { mutableStateListOf(*allItems.toTypedArray()) }
    val checkedIds = remember { mutableStateSetOf(*initialChecked.toTypedArray()) }

    val outOfStock   = items.filter { it.section == Section.OUT_OF_STOCK }
    val runningLow   = items.filter { it.section == Section.RUNNING_LOW }
    val checkedCount = checkedIds.size
    val totalCount   = items.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Shopping List",
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
                    IconButton(onClick = { /* TODO: share list */ }) {
                        Icon(
                            imageVector        = Icons.Outlined.Share,
                            contentDescription = "Share",
                        )
                    }
                    TextButton(onClick = { checkedIds.clear() }) {
                        Text(
                            text  = "Reset",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClearAllRed,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            // Stack progress bar
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape  = CircleShape,
                    color  = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text     = "$checkedCount of $totalCount checked",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color    = MaterialTheme.colorScheme.primary,
                    )
                }

                FloatingActionButton(
                    onClick        = {
                        items.removeAll { it.id in checkedIds }
                        checkedIds.clear()
                    },
                    shape          = RoundedCornerShape(16.dp),
                    containerColor = FabBg,
                    contentColor   = FabIcon,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = "Remove purchased items",
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {

            item {
                Text(
                    text     = "Auto-generated from expired and low-stock items",
                    style    = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                    ),
                    color    = SubtitleGrey,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            stickyHeader(key = "header_oos") {
                SectionHeader(
                    title = "OUT OF STOCK",
                )
            }
            items(outOfStock, key = { it.id }) { item ->
                ShoppingItemRow(
                    item      = item,
                    checked   = item.id in checkedIds,
                    onToggle  = { id ->
                        if (id in checkedIds) checkedIds.remove(id) else checkedIds.add(id)
                    },
                    isLowStock = false,
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            stickyHeader(key = "header_low") {
                SectionHeader(title = "RUNNING LOW")
            }
            items(runningLow, key = { it.id }) { item ->
                ShoppingItemRow(
                    item       = item,
                    checked    = item.id in checkedIds,
                    onToggle   = { id ->
                        if (id in checkedIds) checkedIds.remove(id) else checkedIds.add(id)
                    },
                    isLowStock = true,
                )
            }
        }
    }
}


@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = SectionHeaderGreen,
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 1.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
        )
    }
}

//Shopping item row
@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    checked: Boolean,
    onToggle: (String) -> Unit,
    isLowStock: Boolean,
) {
    ElevatedCard(
        shape   = RoundedCornerShape(16.dp),
        colors  = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .alpha(if (checked) 0.60f else 1.00f)
            .clickable { onToggle(item.id) },
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier         = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) CheckedGreen
                        else Color.Transparent,
                    )
                    .run {
                        if (!checked) {
                            border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            )
                        } else this
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = "Checked",
                        tint               = Color.White,
                        modifier           = Modifier.size(14.dp),
                    )
                }
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text            = item.name,
                    style           = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight      = FontWeight.Medium,
                        textDecoration  = if (checked) TextDecoration.LineThrough else null,
                    ),
                    color           = if (checked) {
                        SubtitleGrey
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text  = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLowStock && !checked) {
                        LowStockAmber
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (checked) ChipCheckedBg else ChipBg,
            ) {
                Text(
                    text     = item.quantity,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style    = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color    = if (checked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        ChipText
                    },
                )
            }
        }
    }
}
@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewShoppingLight() {
    SmartPantryTheme {
        ShoppingListScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewShoppingDark() {
    SmartPantryTheme(darkTheme = true) {
        ShoppingListScreen(onNavigateBack = {})
    }
}
