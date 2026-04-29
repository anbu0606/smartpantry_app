package com.lab03g10.smartpantry.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

/**
 * A pill-shaped linear progress bar that visualises time remaining before expiry.
 *
 * @param daysUntilExpiry  Days remaining;
 * @param totalShelfDays   Expected total shelf life used to compute fill ratio.
 */
@Composable
fun ExpiryProgressBar(
    daysUntilExpiry: Int,
    totalShelfDays: Int,
    modifier: Modifier = Modifier,
) {
    // -- Progress fraction
    val safeDenominator = totalShelfDays.coerceAtLeast(1)
    val progress = (daysUntilExpiry.toFloat() / safeDenominator.toFloat()).coerceIn(0f, 1f)

    // -- Colour tokens
    val indicatorColor = when {
        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.error
        daysUntilExpiry <= 3 -> MaterialTheme.colorScheme.tertiary
        else                 -> MaterialTheme.colorScheme.primary
    }
    val trackColor = when {
        daysUntilExpiry <= 0 -> MaterialTheme.colorScheme.errorContainer
        daysUntilExpiry <= 3 -> MaterialTheme.colorScheme.tertiaryContainer
        else                 -> MaterialTheme.colorScheme.primaryContainer
    }

    // -- Label text
    val label = when {
        daysUntilExpiry < 0  -> "Expired"
        daysUntilExpiry == 0 -> "Expired today"
        daysUntilExpiry == 1 -> "1 day left"
        else                 -> "$daysUntilExpiry days left"
    }

    // -- Layout
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LinearProgressIndicator(
            progress          = { progress },
            color             = indicatorColor,
            trackColor        = trackColor,
            strokeCap         = StrokeCap.Round,
            gapSize           = 0.dp,            // no gap between fill and track
            drawStopIndicator = {},              // suppress the end-cap stop dot
            modifier          = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),   // full pill shape
        )

        Text(
            text  = label,
            // labelSmall = "Expiration tags, Low Stock alerts" — DESIGN.md §3
            style = MaterialTheme.typography.labelSmall,
            color = indicatorColor,              // label echoes bar colour for cohesion
        )
    }
}

// -- Previews

@Preview(name = "Light · Safe (10 / 30 d)", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewSafeLight() {
    SmartPantryTheme {
        ExpiryProgressBar(
            daysUntilExpiry = 10,
            totalShelfDays  = 30,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light · Warning 3D (3 / 7 d)", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewWarning3DLight() {
    SmartPantryTheme {
        ExpiryProgressBar(
            daysUntilExpiry = 3,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light · Warning 1D (1 / 7 d)", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewWarning1DLight() {
    SmartPantryTheme {
        ExpiryProgressBar(
            daysUntilExpiry = 1,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light · Expired Today (0 / 7 d)", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewExpiredTodayLight() {
    SmartPantryTheme {
        ExpiryProgressBar(
            daysUntilExpiry = 0,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light · Expired (−3 / 7 d)", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewExpiredLight() {
    SmartPantryTheme {
        ExpiryProgressBar(
            daysUntilExpiry = -3,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name            = "Dark · Safe (20 / 30 d)",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSafeDark() {
    SmartPantryTheme(darkTheme = true) {
        ExpiryProgressBar(
            daysUntilExpiry = 20,
            totalShelfDays  = 30,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name            = "Dark · Warning 2D (2 / 7 d)",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWarningDark() {
    SmartPantryTheme(darkTheme = true) {
        ExpiryProgressBar(
            daysUntilExpiry = 2,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name            = "Dark · Expired Today",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewExpiredTodayDark() {
    SmartPantryTheme(darkTheme = true) {
        ExpiryProgressBar(
            daysUntilExpiry = 0,
            totalShelfDays  = 7,
            modifier        = Modifier.padding(16.dp),
        )
    }
}
