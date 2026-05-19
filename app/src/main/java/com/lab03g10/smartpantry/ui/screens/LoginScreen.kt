package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import com.lab03g10.smartpantry.ui.viewmodel.LoginViewModel

private const val WEB_CLIENT_ID =
    "218320707597-27fs0ivqlp1jatdg01ae9ur9ga1d3gam.apps.googleusercontent.com"

// Card-level colour constants
private val LogoIconBg    = Color(0xFFE1F5EE)   // pale green box behind the leaf icon
private val LogoIconFg    = Color(0xFF0F6E56)   // dark teal leaf
private val ActionGreen   = Color(0xFF0F6E56)   // "Forgot password?" / "Create one" links
private val TaglineColor  = Color(0xFF888888)   // "Reduce waste, eat smart"
private val GoogleBorder  = Color(0xFFCCCCCC)
private val GoogleText    = Color(0xFF1A1A1A)

// Google "G" icon used in the UI
private val GoogleIcon: ImageVector by lazy {
    ImageVector.Builder(
        name           = "Google",
        defaultWidth   = 20.dp,
        defaultHeight  = 20.dp,
        viewportWidth  = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92" +
                "c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57" +
                "c2.08-1.92 3.28-4.74 3.28-8.09z",
            ).toNodes(),
            fill = SolidColor(Color(0xFF4285F4)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77" +
                "c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53" +
                "H2.18v2.84C3.99 20.53 7.7 23 12 23z",
            ).toNodes(),
            fill = SolidColor(Color(0xFF34A853)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09" +
                "V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z",
            ).toNodes(),
            fill = SolidColor(Color(0xFFFBBC05)),
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15" +
                "C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07" +
                "l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z",
            ).toNodes(),
            fill = SolidColor(Color(0xFFEA4335)),
        )
    }.build()
}

/**
 * Sign-in screen for the Smart Pantry app.
 *
 * @param onLoginSuccess  -> goes to dashboard after login when tapped
 * @param onNavigateToRegister -> opens registration screen when tapped
 * @param onNavigateToForgotPassword -> opens forgot password screen when tapped
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe      by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(72.dp))

            // 1. Logo area
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LogoIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Eco,
                    contentDescription = null,
                    tint               = LogoIconFg,
                    modifier           = Modifier.size(40.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text  = "Smart Pantry",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "Reduce waste, eat smart",
                style = MaterialTheme.typography.bodySmall,
                color = TaglineColor,
            )

            Spacer(Modifier.height(48.dp))

            // 2. Email field
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine      = true,
                shape           = RoundedCornerShape(12.dp),
                enabled         = !isLoading,
                isError         = error != null && error!!.contains("email", ignoreCase = true)
            )

            Spacer(Modifier.height(16.dp))

            // 3. Password field
            OutlinedTextField(
                value               = password,
                onValueChange       = { password = it },
                modifier            = Modifier.fillMaxWidth(),
                label               = { Text("Password") },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector        = if (passwordVisible) {
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine      = true,
                shape           = RoundedCornerShape(12.dp),
                enabled         = !isLoading,
                isError         = error != null && error!!.contains("password", ignoreCase = true)
            )

            // "Remember me" (left) + "Forgot password?" (right) in one row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(
                        checked         = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled         = !isLoading,
                        colors          = CheckboxDefaults.colors(
                            checkedColor   = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                    Text(
                        text  = "Remember me",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextButton(
                    onClick        = onNavigateToForgotPassword,
                    enabled        = !isLoading,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = "Forgot password?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = ActionGreen,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 4. Sign In button
            Button(
                onClick  = { viewModel.signIn(email, password, onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "Sign In",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 5. Divider row with centred "or"
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 1.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f),
                )
                Text(
                    text     = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style    = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color    = MaterialTheme.colorScheme.outline,
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 1.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // 6. Google button
            OutlinedButton(
                onClick        = {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleSignInOption = GetSignInWithGoogleOption
                                .Builder(WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleSignInOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val googleCredential = GoogleIdTokenCredential
                                .createFrom(result.credential.data)
                            viewModel.signInWithGoogle(googleCredential.idToken, onLoginSuccess)
                        } catch (_: GetCredentialCancellationException) {
                            // user dismissed the picker — no error shown
                        } catch (e: Exception) {
                            viewModel.setError(e.message ?: "Google sign-in failed")
                        }
                    }
                },
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape          = RoundedCornerShape(12.dp),
                border         = BorderStroke(1.dp, GoogleBorder),
                colors         = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor   = GoogleText,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp),
                enabled        = !isLoading
            ) {
                Icon(
                    imageVector        = GoogleIcon,
                    contentDescription = "Google",
                    tint               = Color.Unspecified,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = "Continue with Google",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = GoogleText,
                )
            }

            Spacer(Modifier.height(48.dp))

            // 7. Sign-up footer row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick        = onNavigateToRegister,
                    enabled        = !isLoading,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    Text(
                        text  = "Create one",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = ActionGreen,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
