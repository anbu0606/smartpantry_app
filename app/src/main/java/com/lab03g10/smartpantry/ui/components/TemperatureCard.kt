package com.lab03g10.smartpantry.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

private val CardBg   = Color(0xFF0F6E56)
private val White100 = Color.White
private val White80  = Color.White.copy(alpha = 0.80f)
private val White70  = Color.White.copy(alpha = 0.70f)
private val White60  = Color.White.copy(alpha = 0.60f)
private val White10  = Color.White.copy(alpha = 0.10f)
private val White05  = Color.White.copy(alpha = 0.05f)

private val HeroStyle = TextStyle(
    fontSize      = 40.sp,
    fontWeight    = FontWeight.Light,   // weight 300 per spec
    letterSpacing = (-0.5).sp,
    lineHeight    = 44.sp,
)

/**
 * Live sensor data hero card for the Fridge Monitor screen.
 *
 * @param locationName Name of the storage location shown in the header,
 * @param temperature  Current temperature
 * @param humidity     Current humidity as a percentage (0–100).
 * @param isNormal     green dot/red dot
 * @param lux          Current illuminance in lux.
 * @param isNormal     `true` → green dot + "Normal" message;
 *                     `false` → red dot + "Alert" message.
 */
@Composable
fun TemperatureCard(
    locationName: String,
    temperature: Float,
    humidity: Float,
    lux: Float,
    isNormal: Boolean,
    modifier: Modifier = Modifier,
) {
    // Semantic dot colours from colorScheme
    val dotColor = if (isNormal) {
        MaterialTheme.colorScheme.inversePrimary   // #82DA80 — light green on dark bg
    } else {
        MaterialTheme.colorScheme.error            // #BA1A1A — red alert
    }

    val statusText = if (isNormal) {
        "Normal — all readings within range"
    } else {
        "Alert — Check $locationName"
    }

    // Tailwind animate-pulse equivalent: opacity 1 → 0.4 → 1
    val pulseTrans = rememberInfiniteTransition(label = "live-dot")
    val dotAlpha by pulseTrans.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.40f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot-alpha",
    )

    // Card container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg),
    ) {
        // -- Decorative circle
        Box(
            modifier = Modifier
                .size(192.dp)
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-48).dp)
                .clip(CircleShape)
                .background(White05),
        )

        // -- Card content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            //location name + status tag
            Text(
                text  = "${locationName.uppercase()} · LIVE SENSOR DATA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                ),
                color = White60,
            )

            Spacer(Modifier.height(16.dp))

            // Two-column metric block
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom,
            ) {
                MetricColumn(
                    value     = "%.1f°C".format(temperature),
                    label     = "Temperature",
                    alignment = Alignment.Start,
                )
                MetricColumn(
                    value     = "%.0f%%".format(humidity),
                    label     = "Humidity",
                    alignment = Alignment.CenterHorizontally,
                )
                MetricColumn(
                    value     = "%.0f lx".format(lux),
                    label     = "Light",
                    alignment = Alignment.End,
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White10),
            )

            Spacer(Modifier.height(16.dp))

            // Status row: pulsing dot + status message
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = dotAlpha)),
                )
                Text(
                    text  = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White80,
                )
            }
        }
    }
}

// -- Metric column -------------------------------------------------------------

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    alignment: Alignment.Horizontal,
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text  = value,
            style = HeroStyle,
            color = White100,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,   // `font-medium` in HTML
            ),
            color = White70,
        )
    }
}

// -- Previews ------------------------------------------------------------------

@Preview(name = "Light · Normal", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewNormalLight() {
    SmartPantryTheme {
        TemperatureCard(
            locationName = "Fridge",
            temperature  = 4.2f,
            humidity     = 58f,
            lux          = 12f,
            isNormal     = true,
            modifier     = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light · Alert", showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun PreviewAlertLight() {
    SmartPantryTheme {
        TemperatureCard(
            locationName = "Pantry",
            temperature  = 8.7f,
            humidity     = 72f,
            lux          = 420f,
            isNormal     = false,
            modifier     = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name            = "Dark · Normal",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewNormalDark() {
    SmartPantryTheme(darkTheme = true) {
        TemperatureCard(
            locationName = "Fridge",
            temperature  = 4.2f,
            humidity     = 58f,
            lux          = 12f,
            isNormal     = true,
            modifier     = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name            = "Dark · Alert",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAlertDark() {
    SmartPantryTheme(darkTheme = true) {
        TemperatureCard(
            locationName = "Pantry",
            temperature  = 8.7f,
            humidity     = 72f,
            lux          = 420f,
            isNormal     = false,
            modifier     = Modifier.padding(16.dp),
        )
    }
}
