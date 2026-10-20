package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.RegistrationViewModel

// Design-system colour constants
private val WarningAmber  = Color(0xFFF57F17)   // strength "Medium" — amber per HTML spec
private val SignInGreen   = Color(0xFF0F6E56)   // "Sign in" footer link
private val SubtitleGrey  = Color(0xFF888888)   // subtitle + helper text

// Password-strength model

/**
 * Password strength levels:
 * - Weak: less than 6 characters
 * - Medium: 6–9 characters (or 10+ characters without full complexity)
 * - Strong: 10+ characters with uppercase, number, and special character
 */
private enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }

private fun passwordStrength(password: String): PasswordStrength = when {
    password.isEmpty()    -> PasswordStrength.NONE
    password.length < 6   -> PasswordStrength.WEAK
    password.length < 10  -> PasswordStrength.MEDIUM
    else -> {
        val hasUpper   = password.any { it.isUpperCase() }
        val hasDigit   = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (hasUpper && hasDigit && hasSpecial) PasswordStrength.STRONG else PasswordStrength.MEDIUM
    }
}

// Public composable

/**
 * Registration screen for the Smart Pantry app.
 *
 * @param onRegistrationSuccess -> Called when registration is completed
 * @param onNavigateToLogin -> used to go back to login screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    // Field state
    var fullName               by remember { mutableStateOf("") }
    var email                  by remember { mutableStateOf("") }
    var password               by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPassword        by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    // Tracks whether the confirm field has ever lost focus (used to defer error display)
    var confirmTouched         by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Derived UI state
    val strength           = passwordStrength(password)
    val showConfirmError   = confirmTouched && confirmPassword != password
    val isFormValid        = fullName.isNotBlank()
            && email.isNotBlank() && email.contains("@")
            && password.length >= 8
            && confirmPassword == password

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Create Account",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,   // weight 400 per spec
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor  = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {

            // Subtitle
            Text(
                text     = "Join Smart Pantry and start reducing food waste",
                style    = MaterialTheme.typography.bodyMedium,
                color    = SubtitleGrey,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(8.dp))

            // Form
            Column(
                modifier            = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                // Legend
                Text(
                    text  = "* Required fields",
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtitleGrey,
                )

                // Full name
                OutlinedTextField(
                    value         = fullName,
                    onValueChange = { fullName = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Full name *") },
                    leadingIcon   = {
                        Icon(
                            imageVector        = Icons.Outlined.Person,
                            contentDescription = null,
                        )
                    },
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    enabled         = !isLoading
                )

                // Email
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Email address *") },
                    leadingIcon   = {
                        Icon(
                            imageVector        = Icons.Outlined.Email,
                            contentDescription = null,
                        )
                    },
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled         = !isLoading,
                    isError         = error?.contains("email", ignoreCase = true) == true
                )

                // Password + strength indicator
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value                = password,
                        onValueChange        = { password = it },
                        modifier             = Modifier.fillMaxWidth(),
                        label                = { Text("Password *") },
                        leadingIcon          = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Outlined.Visibility
                                    } else {
                                        Icons.Outlined.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    },
                                )
                            }
                        },
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled         = !isLoading,
                        isError         = error?.contains("password", ignoreCase = true) == true
                    )

                    // Strength indicator — appears after the first character is typed
                    if (strength != PasswordStrength.NONE) {
                        PasswordStrengthRow(strength = strength)
                    }
                }

                // Confirm password
                OutlinedTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            // Mark field as touched when user leaves it. This avoids showing error while typing
                            if (!focusState.isFocused && confirmPassword.isNotEmpty()) {
                                confirmTouched = true
                            }
                        },
                    label       = { Text("Confirm password *") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null)
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                },
                                contentDescription = if (confirmPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                },
                            )
                        }
                    },
                    isError         = showConfirmError,
                    supportingText  = if (showConfirmError) {
                        {
                            Text(
                                text  = "Passwords do not match",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        null
                    },
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled         = !isLoading
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Submit
                Button(
                    onClick  = { viewModel.signUp(email, fullName, password, onRegistrationSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = isFormValid && !isLoading,
                    shape   = RoundedCornerShape(12.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        // Disabled: desaturated sage green — matches HTML's `bg-secondary-fixed-dim/60`
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            .copy(alpha = 0.60f),
                        disabledContentColor   = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.38f),
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text  = "Create Account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer: sign-in link
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick        = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    enabled        = !isLoading
                ) {
                    Text(
                        text  = "Sign in",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = SignInGreen,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// Password strength row

/**
 * Password strength indicator
 *
 * Shows a progress bar, label, and helper text
 * Appears when user starts typing password
 *
 * Changes color based on strength:
 * Weak - red color (#BA1A1A)
 * Medium - orange color (#F57F17)
 * Strong - green color (#0D631B)
 *
 **/
@Composable
private fun PasswordStrengthRow(strength: PasswordStrength) {
    val (progress, color, label) = when (strength) {
        PasswordStrength.NONE   -> Triple(0f,    Color.Transparent, "")
        PasswordStrength.WEAK   -> Triple(0.33f, MaterialTheme.colorScheme.error, "Weak")
        PasswordStrength.MEDIUM -> Triple(0.66f, WarningAmber,                    "Medium")
        PasswordStrength.STRONG -> Triple(1.00f, MaterialTheme.colorScheme.primary, "Strong")
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 4dp tall pill-shaped progress bar
        LinearProgressIndicator(
            progress          = { progress },
            color             = color,
            trackColor        = MaterialTheme.colorScheme.surfaceContainer,
            strokeCap         = StrokeCap.Round,
            gapSize           = 0.dp,
            drawStopIndicator = {},
            modifier          = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
        )

        // Helper text (left) + strength label (right) on one row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text     = "Min. 8 characters · Uppercase · Number · Special character",
                style    = MaterialTheme.typography.labelSmall,
                color    = SubtitleGrey,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Strength: $label",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = color,
            )
        }
    }
}

// Previews

@Preview(name = "Light · Empty (button disabled)", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewRegistrationEmptyLight() {
    SmartPantryTheme {
        RegistrationScreen(
            onRegistrationSuccess = {},
            onNavigateToLogin     = {},
        )
    }
}

@Preview(
    name            = "Dark · Empty",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewRegistrationEmptyDark() {
    SmartPantryTheme(darkTheme = true) {
        RegistrationScreen(
            onRegistrationSuccess = {},
            onNavigateToLogin     = {},
        )
    }
}
