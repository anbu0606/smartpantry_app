package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// Design tokens
private val AvatarGreen       = Color(0xFF1D9E75)
private val SectionLabelGreen = Color(0xFF0F6E56)
private val IconTileBg        = Color(0xFFF5F5F5)
private val SliderTrackFill   = Color(0xFFC8E6C9)
private val SignOutRed        = Color(0xFFB71C1C)
private val SubtitleGrey      = Color(0xFF888888)
private val SliderActiveGreen = Color(0xFF2E7D32)

// Dietary preference data
private data class DietPref(val id: String, val emoji: String, val title: String, val subtitle: String)

private val dietaryPrefs = listOf(
    DietPref("veg",    "🌿", "Vegetarian",  "Exclude meat and fish"),
    DietPref("vegan",  "🌱", "Vegan",        "Exclude all animal products"),
    DietPref("gluten", "🌾", "Gluten-Free",  "Exclude gluten-containing items"),
    DietPref("dairy",  "🥛", "Dairy-Free",   "Exclude dairy products"),
)

// Gluten-Free is ON by default
private val defaultSwitchStates = mapOf(
    "veg"    to false,
    "vegan"  to false,
    "gluten" to true,
    "dairy"  to false,
)

// Expiry reminder options
private val reminderOptions = listOf("24 hours before", "48 hours before", "72 hours before")

// Public composable

/**
 * Profile & Settings screen.
 *
 * Sections in this screen:
 * 1. User details (profile image, name, email)
 * 2. Dietary preferences (toggle options)
 * 3. Expiry reminder options
 * 4. Fridge temperature setting
 * 5.  Save button and Sign Out option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    // State
    val switchStates = remember {
        mutableStateMapOf(*defaultSwitchStates.entries.map { it.key to it.value }.toTypedArray())
    }
    var selectedReminder by remember { mutableStateOf("48 hours before") }
    var alertThreshold   by remember { mutableFloatStateOf(6f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: edit profile */ }) {
                        Icon(
                            imageVector        = Icons.Outlined.Edit,
                            contentDescription = "Edit profile",
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
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            // 1. User header
            item {
                UserHeader()
            }

            // 2. Dietary Preferences
            // Note: These switches currently work only in this screen
            // They are not connected to other parts of the app yet
            //
            // Future Improvements(Assignment 3):
            // - Preferences can be shared across screens
            // - Food items can be checked based on these preferences
            // - App can show warnings or filter items if needed
            item {
                SectionLabel(
                    title    = "DIETARY PREFERENCES",
                    topPad   = 20.dp,
                )
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dietaryPrefs.forEach { pref ->
                        DietaryPrefRow(
                            pref     = pref,
                            checked  = switchStates[pref.id] == true,
                            onToggle = { switchStates[pref.id] = it },
                        )
                    }
                }
            }

            // 3. Expiry Reminders
            item {
                SectionLabel(title = "EXPIRY REMINDERS")
                ElevatedCard(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text  = "Notify me before expiry",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(12.dp))
                        reminderOptions.forEachIndexed { index, option ->
                            if (index > 0) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                                )
                            }
                            Row(
                                modifier            = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clickable { selectedReminder = option },
                                verticalAlignment   = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                val isSelected = option == selectedReminder
                                Text(
                                    text  = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick  = { selectedReminder = option },
                                    colors   = RadioButtonDefaults.colors(
                                        selectedColor   = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // 4. Fridge alert threshold slider
            item {
                SectionLabel(title = "FRIDGE ALERT THRESHOLD")
                ElevatedCard(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Label + current value
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "Temperature alert above:",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = "${alertThreshold.toInt()}°C",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Light,
                                ),
                                color = SliderActiveGreen,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Slider
                        Slider(
                            value         = alertThreshold,
                            onValueChange = { alertThreshold = it },
                            valueRange    = 2f..10f,
                            steps         = 7,    // 2,3,4,5,6,7,8,9,10 → 7 interior steps
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = SliderDefaults.colors(
                                thumbColor            = SliderActiveGreen,
                                activeTrackColor      = SliderActiveGreen,
                                inactiveTrackColor    = SliderTrackFill,
                                activeTickColor       = Color.Transparent,
                                inactiveTickColor     = Color.Transparent,
                            ),
                        )

                        // Min / Max labels
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text  = "2°C",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SubtitleGrey,
                            )
                            Text(
                                text  = "10°C",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SubtitleGrey,
                            )
                        }
                    }
                }
            }

            //  5. Action buttons
            item {
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick  = { /* TODO: persist preferences */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text  = "Save Preferences",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick  = { /* TODO: sign out flow */ },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text      = "Sign Out",
                            style     = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color     = SignOutRed,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// User header

@Composable
private fun UserHeader() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 80dp avatar circle
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AvatarGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = "A",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize   = 32.sp,
                ),
                color = Color.White,
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Alex Song",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text  = "alex@example.com",
            style = MaterialTheme.typography.bodySmall,
            color = SubtitleGrey,
        )

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
        )
    }
}

// Section label

@Composable
private fun SectionLabel(
    title: String,
    topPad: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelSmall.copy(
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.5.sp,
        ),
        color    = SectionLabelGreen,
        modifier = Modifier.padding(
            start  = 16.dp,
            end    = 16.dp,
            top    = topPad,
            bottom = 8.dp,
        ),
    )
}

// Dietary preference row

/**
 * Single dietary preference row with icon, text, and switch
 */
@Composable
private fun DietaryPrefRow(
    pref: DietPref,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ElevatedCard(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 40dp icon tile
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IconTileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = pref.emoji, fontSize = 20.sp)
            }

            // Text
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text  = pref.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = pref.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtitleGrey,
                )
            }

            // Switch
            Switch(
                checked         = checked,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = Color.White,
                    checkedTrackColor    = SliderActiveGreen,
                    uncheckedThumbColor  = Color.White,
                    uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceContainerHigh,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

// Previews

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewProfileLight() {
    SmartPantryTheme {
        ProfileScreen()
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewProfileDark() {
    SmartPantryTheme(darkTheme = true) {
        ProfileScreen()
    }
}
