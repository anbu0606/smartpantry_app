package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

private val CameraBg         = Color(0xFF1A1A1A)
private val SheetHandleColor = Color(0xFFEEEEEE)
private val SubtitleGrey     = Color(0xFF888888)

// scan mode options
private val scanModes = listOf("Barcode", "Expiry Label")

/**
 * barcode expiry scanner screen, the dark background simulates a viewfinder.
 *
 * Layout: TopAppBar, ViewfinderBox
 *
 * @param onNavigateToAddFood called by Enter Manually Instead calls AddEditFood
 * @param onNavigateToBarcodeResult called by "Simulate Scan" calls barcodeScanResultScreen
 * @param onNavigateBack called by the back arrow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateToAddFood: () -> Unit = {},
    onNavigateToBarcodeResult: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    var selectedMode by remember { mutableStateOf("Barcode") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraBg),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(bottom = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // Viewfinder with corner brackets and scan line
            ViewfinderBox()

            Spacer(Modifier.height(16.dp))

            Text(
                text      = "Point at a barcode or expiry date label",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.White.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 48.dp),
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                scanModes.forEach { mode ->
                    ScanModeChip(
                        label    = mode,
                        selected = mode == selectedMode,
                        onClick  = { selectedMode = mode },
                    )
                }
            }
        }

        // TopAppBar - transparent
        TopAppBar(
            title = {
                Text(
                    text  = "Scan Item",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color.White,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White,
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: toggle torch */ }) {
                    Icon(
                        imageVector        = Icons.Outlined.FlashOff,
                        contentDescription = "Flash",
                        tint               = Color.White,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            modifier = Modifier.align(Alignment.TopCenter),
        )

        BottomSheetStrip(
            onSimulateScan  = onNavigateToBarcodeResult,
            onEnterManually = onNavigateToAddFood,
            modifier        = Modifier.align(Alignment.BottomCenter),
        )
    }
}

//Viewfinder
@Composable
private fun ViewfinderBox() {
    val bracketLen   = 24.dp
    val bracketStroke = 3.dp

    Box(
        modifier         = Modifier
            .size(260.dp)
            .drawWithCache {
                val len   = bracketLen.toPx()
                val stroke = bracketStroke.toPx()
                val w     = size.width
                val h     = size.height

                onDrawBehind {
                    val paint = Stroke(width = stroke, cap = StrokeCap.Square)
                    val c     = Color.White

                    // top left
                    drawLine(c, Offset(0f, 0f), Offset(len, 0f), stroke, StrokeCap.Square)
                    drawLine(c, Offset(0f, 0f), Offset(0f, len), stroke, StrokeCap.Square)
                    // top right
                    drawLine(c, Offset(w, 0f), Offset(w - len, 0f), stroke, StrokeCap.Square)
                    drawLine(c, Offset(w, 0f), Offset(w, len), stroke, StrokeCap.Square)
                    // bottom left
                    drawLine(c, Offset(0f, h), Offset(len, h), stroke, StrokeCap.Square)
                    drawLine(c, Offset(0f, h), Offset(0f, h - len), stroke, StrokeCap.Square)
                    // bottom right
                    drawLine(c, Offset(w, h), Offset(w - len, h), stroke, StrokeCap.Square)
                    drawLine(c, Offset(w, h), Offset(w, h - len), stroke, StrokeCap.Square)
                }

            },
        contentAlignment = Alignment.Center,
    ) {
        // Horizontal scan line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.70f),
                            Color.White,
                            Color.White.copy(alpha = 0.70f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ScanModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape   = CircleShape,
        color   = if (selected) Color.White else Color.Transparent,
        border  = if (selected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.50f)),
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style    = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color    = if (selected) CameraBg else Color.White,
        )
    }
}

@Composable
private fun BottomSheetStrip(
    onSimulateScan: () -> Unit,
    onEnterManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color    = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(SheetHandleColor, CircleShape),
            )
            Text(
                text      = "Scanning for product information!!!",
                style     = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color     = Color(0xFF1A1C1C),
                textAlign = TextAlign.Center,
            )

            Text(
                text      = "Barcode will auto-fill the food name and nutritional data",
                style     = MaterialTheme.typography.bodySmall,
                color     = SubtitleGrey,
                textAlign = TextAlign.Center,
            )

            // simulate a successful scan prototype design
            androidx.compose.material3.Button(
                onClick  = onSimulateScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D631B),
                ),
            ) {
                Text(
                    text  = "Simulate Scan Result",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                )
            }

            HorizontalDivider(
                color = Color(0xFFEEEEEE),
            )

            // manual button
            OutlinedButton(
                onClick  = onEnterManually,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.dp, Color(0xFF0D631B)),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF0D631B),
                ),
            ) {
                Text(
                    text  = "Enter Manually Instead",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Scanner", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PreviewScanner() {
    SmartPantryTheme {
        ScannerScreen(
            onNavigateToAddFood       = {},
            onNavigateToBarcodeResult = {},
            onNavigateBack            = {},
        )
    }
}
