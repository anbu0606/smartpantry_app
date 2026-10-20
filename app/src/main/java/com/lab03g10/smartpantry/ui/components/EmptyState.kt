package com.lab03g10.smartpantry.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


/**
 * @(#) EmptyState.kt 1.0 2026-05-17
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Shared empty-state slot — centred icon + title + supporting line. Used
 * across Dashboard / Inventory / StorageMonitor / Notifications / ShoppingList
 * so the copy and visual rhythm stay consistent.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    supportingText: String? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        // Inline horizontal — for LazyRow slots / section gaps where the
        // empty state must match the height of the data cards it replaces.
        // Outer Row centers; inner Row keeps icon+text together as one
        // cluster (spacedBy on the outer Row would put gaps at the edges).
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(24.dp),
                )
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    // Full-size centred — for screens where the empty state IS the content.
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(48.dp),
        )
        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color     = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(top = 16.dp),
        )
        if (supportingText != null) {
            Text(
                text      = supportingText,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp),
            )
        }
    }
}
