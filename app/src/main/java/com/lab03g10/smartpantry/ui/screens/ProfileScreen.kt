package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.BuildConfig
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.ProfileViewModel

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

private val defaultSwitchStates = mapOf(
    "veg"    to false,
    "vegan"  to false,
    "gluten" to true,
    "dairy"  to false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Observe DataStore-backed state
    val isDarkMode      by viewModel.isDarkMode.collectAsState()
    val notifyDaysVm    by viewModel.notifyDaysBefore.collectAsState()
    val quietStartVm    by viewModel.quietHoursStart.collectAsState()
    val quietEndVm      by viewModel.quietHoursEnd.collectAsState()
    val userNameVm      by viewModel.userName.collectAsState()
    val currentUser     by viewModel.currentUser.collectAsState()

    // Prefer DataStore name; fall back to Firebase displayName
    val resolvedName = userNameVm.ifEmpty { currentUser?.displayName.orEmpty() }

    // Local editable state — name initialises once when a non-empty value arrives
    var localName       by remember { mutableStateOf("") }
    LaunchedEffect(resolvedName) {
        if (localName.isEmpty() && resolvedName.isNotEmpty()) localName = resolvedName
    }
    // Slider states reset to the persisted value whenever DataStore emits (i.e. after Save)
    var localNotifyDays by remember(notifyDaysVm) { mutableIntStateOf(notifyDaysVm) }
    var localQuietStart by remember(quietStartVm)  { mutableIntStateOf(quietStartVm) }
    var localQuietEnd   by remember(quietEndVm)    { mutableIntStateOf(quietEndVm) }

    // Local-only state (not persisted via DataStore)
    val switchStates = remember {
        mutableStateMapOf(*defaultSwitchStates.entries.map { it.key to it.value }.toTypedArray())
    }
    var alertThreshold by remember { mutableFloatStateOf(6f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    )
                },
//                actions = {
//                    IconButton(onClick = { /* TODO: edit profile */ }) {
//                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit profile")
//                    }
//                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            // 1. User header
            item {
                UserHeader(
                    name  = localName.ifEmpty { currentUser?.displayName ?: "Guest User" },
                    email = currentUser?.email ?: "Not logged in",
                )
            }

            // 2. Account — editable display name (persisted on Save)
            item {
                SectionLabel(title = "ACCOUNT", topPad = 20.dp)
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        OutlinedTextField(
                            value         = localName,
                            onValueChange = { localName = it },
                            label         = { Text("Display name") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // 3. Appearance — dark mode (persists immediately on toggle)
            item {
                SectionLabel(title = "APPEARANCE")
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text  = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = "Override system theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtitleGrey,
                            )
                        }
                        Switch(
                            checked         = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) },
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

            // 4. Dietary Preferences (local only)
            item {
                SectionLabel(title = "DIETARY PREFERENCES")
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

            // 5. Expiry Notifications — notify-days-before slider 1–7 (persisted on Save)
            item {
                SectionLabel(title = "EXPIRY NOTIFICATIONS")
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "Notify me before expiry:",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = if (localNotifyDays == 1) "1 day" else "$localNotifyDays days",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light),
                                color = SliderActiveGreen,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // 7 discrete positions (1–7) → steps = 5
                        Slider(
                            value         = localNotifyDays.toFloat(),
                            onValueChange = { localNotifyDays = it.toInt() },
                            valueRange    = 1f..7f,
                            steps         = 5,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = presetSliderColors(),
                        )
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text  = "1 day",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SubtitleGrey,
                            )
                            Text(
                                text  = "7 days",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SubtitleGrey,
                            )
                        }
                    }
                }
            }

            // 6. Quiet Hours — do-not-disturb window (persisted on Save)
            item {
                SectionLabel(title = "QUIET HOURS")
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text  = "Do Not Disturb",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(16.dp))
                        HourSliderRow(
                            label    = "From",
                            hour     = localQuietStart,
                            onChange = { localQuietStart = it },
                        )
                        Spacer(Modifier.height(12.dp))
                        HourSliderRow(
                            label    = "Until",
                            hour     = localQuietEnd,
                            onChange = { localQuietEnd = it },
                        )
                    }
                }
            }

            // 7. Fridge alert threshold (local only — not persisted)
            item {
                SectionLabel(title = "FRIDGE ALERT THRESHOLD")
                ElevatedCard(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "Temperature alert above:",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = "${alertThreshold.toInt()}°C",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light),
                                color = SliderActiveGreen,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // 9 discrete positions (2–10) → steps = 7
                        Slider(
                            value         = alertThreshold,
                            onValueChange = { alertThreshold = it },
                            valueRange    = 2f..10f,
                            steps         = 7,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = presetSliderColors(),
                        )
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

            // 8. Action buttons
            item {
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // TODO-TEST: remove this button before submission
                    Button(
                        onClick  = { viewModel.triggerTestNotification() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE65100),
                        ),
                    ) {
                        Text(
                            text  = "🔔 Test Expiry Notification",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    // TODO-TEST: end of test button

                    Button(
                        onClick  = {
                            viewModel.setUserName(localName)
                            viewModel.setNotifyDaysBefore(localNotifyDays)
                            viewModel.setQuietHours(localQuietStart, localQuietEnd)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text  = "Save Preferences",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick  = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text      = "Sign Out",
                            style     = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color     = SignOutRed,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Debug affordance — calls the real production worker on
                    // demand so demo + manual verification don't have to wait
                    // the 15-minute periodic interval. Hidden in release
                    // builds via BuildConfig.DEBUG. See docs/bug-log.md
                    // BUG-009 for the full rationale (and why this is
                    // deliberately distinct from the BUG-005 fake-alarm
                    // button being removed).
                    if (BuildConfig.DEBUG) {
                        OutlinedButton(
                            onClick  = { viewModel.triggerSensorTickNow() },
                            modifier = Modifier.fillMaxWidth(),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Sensors,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = "Trigger Sensor Tick (debug)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Hour slider row used by the Quiet Hours section

@Composable
private fun HourSliderRow(label: String, hour: Int, onChange: (Int) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = "%02d:00".format(hour),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
            color = SliderActiveGreen,
        )
    }
    // 24 discrete positions (0–23) → steps = 22
    Slider(
        value         = hour.toFloat(),
        onValueChange = { onChange(it.toInt()) },
        valueRange    = 0f..23f,
        steps         = 22,
        modifier      = Modifier.fillMaxWidth(),
        colors        = presetSliderColors(),
    )
}

// Shared slider colour preset to avoid repeating the same 5 colour args

@Composable
private fun presetSliderColors(): SliderColors = SliderDefaults.colors(
    thumbColor         = SliderActiveGreen,
    activeTrackColor   = SliderActiveGreen,
    inactiveTrackColor = SliderTrackFill,
    activeTickColor    = Color.Transparent,
    inactiveTickColor  = Color.Transparent,
)

// User header

@Composable
private fun UserHeader(name: String, email: String) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier         = Modifier.size(80.dp).clip(CircleShape).background(AvatarGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = if (name.isNotEmpty()) name.take(1).uppercase() else "G",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light, fontSize = 32.sp),
                color = Color.White,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text  = name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = email, style = MaterialTheme.typography.bodySmall, color = SubtitleGrey)
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
        )
    }
}

// Section label

@Composable
private fun SectionLabel(title: String, topPad: Dp = 20.dp) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelSmall.copy(
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.5.sp,
        ),
        color    = SectionLabelGreen,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = topPad, bottom = 8.dp),
    )
}

// Dietary preference row

@Composable
private fun DietaryPrefRow(pref: DietPref, checked: Boolean, onToggle: (Boolean) -> Unit) {
    ElevatedCard(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(IconTileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = pref.emoji, fontSize = 20.sp)
            }
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text  = pref.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(text = pref.subtitle, style = MaterialTheme.typography.bodySmall, color = SubtitleGrey)
            }
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
        ProfileScreen(onSignOut = {})
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
        ProfileScreen(onSignOut = {})
    }
}
