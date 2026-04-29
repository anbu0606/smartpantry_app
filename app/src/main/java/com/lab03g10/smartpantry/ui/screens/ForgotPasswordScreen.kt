package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme

// Public composable

/**
 * Forgot Password screen for the Smart Pantry app.
 *
 * This screen has 2 parts:
 * 1. Form - user enters email and clicks "Send Reset Link"
 * 2. Success - shows message and "Return to Sign In" button
 *
 * Animation is used when switching between these two states
 *
 * @param onNavigateBack Called when user clicks the back or "Back to Sign In" / "Return to Sign In" option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onNavigateBack: () -> Unit) {

    var email     by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }
    val isValid   = email.contains("@") && email.contains(".")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            AnimatedContent(
                targetState    = emailSent,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label          = "ForgotPassword_StateTransition",
            ) { sent ->

                if (!sent) {
                    // FORM state

                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Spacer(Modifier.height(40.dp))

                        // Icon circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.LockReset,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier           = Modifier.size(36.dp),
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text      = "Forgot your password?",
                            style     = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color     = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text      = "Enter your email address and we'll send you a link to reset your password.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(32.dp))

                        OutlinedTextField(
                            value         = email,
                            onValueChange = { email = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Email address") },
                            leadingIcon   = {
                                Icon(
                                    imageVector        = Icons.Outlined.Email,
                                    contentDescription = null,
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Done,
                            ),
                            singleLine = true,
                            shape      = RoundedCornerShape(12.dp),
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick  = { if (isValid) emailSent = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = isValid,
                            shape   = RoundedCornerShape(50),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor         = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                    .copy(alpha = 0.60f),
                                disabledContentColor   = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.38f),
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Text(
                                text  = "Send Reset Link",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        TextButton(
                            onClick  = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text  = "Back to Sign In",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                } else {
                    // SUCCESS state

                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Spacer(Modifier.height(80.dp))

                        // Green checkmark circle
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color(0xFFE8F5E9),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint               = Color(0xFF2E7D32),
                                modifier           = Modifier.size(44.dp),
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text      = "Check your email",
                            style     = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color     = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text      = "We've sent a password reset link to $email",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick        = onNavigateBack,
                            modifier       = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape          = RoundedCornerShape(50),
                            colors         = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Text(
                                text  = "Return to Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Previews

@Preview(name = "Light — Form", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewForgotPasswordLight() {
    SmartPantryTheme {
        ForgotPasswordScreen(onNavigateBack = {})
    }
}

@Preview(
    name            = "Dark — Form",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewForgotPasswordDark() {
    SmartPantryTheme(darkTheme = true) {
        ForgotPasswordScreen(onNavigateBack = {})
    }
}

@Preview(name = "Light — Sent", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewForgotPasswordSent() {
    SmartPantryTheme {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFE8F5E9),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint               = Color(0xFF2E7D32),
                    modifier           = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text      = "Check your email",
                style     = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "We've sent a password reset link to user@example.com",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick        = {},
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape          = RoundedCornerShape(50),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    text  = "Return to Sign In",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
