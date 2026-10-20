package com.lab03g10.smartpantry.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                = Primary,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,
    secondary              = Secondary,
    onSecondary            = OnSecondary,
    secondaryContainer     = SecondaryContainer,
    onSecondaryContainer   = OnSecondaryContainer,
    tertiary               = Tertiary,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,
    error                  = ErrorColor,
    onError                = OnError,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,
    background             = Background,
    onBackground           = OnBackground,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    inverseSurface         = InverseSurface,
    inverseOnSurface       = InverseOnSurface,
    inversePrimary         = InversePrimary,
    outline                = Outline,
    outlineVariant         = OutlineVariant,
    scrim                  = Scrim,
    surfaceBright          = SurfaceBright,
    surfaceDim             = SurfaceDim,
    surfaceContainer       = SurfaceContainer,
    surfaceContainerHigh   = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow    = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
)

private val DarkColorScheme = darkColorScheme(
    primary                = PrimaryDark,
    onPrimary              = OnPrimaryDark,
    primaryContainer       = PrimaryContainerDark,
    onPrimaryContainer     = OnPrimaryContainerDark,
    secondary              = SecondaryDark,
    onSecondary            = OnSecondaryDark,
    secondaryContainer     = SecondaryContainerDark,
    onSecondaryContainer   = OnSecondaryContainerDark,
    tertiary               = TertiaryDark,
    onTertiary             = OnTertiaryDark,
    tertiaryContainer      = TertiaryContainerDark,
    onTertiaryContainer    = OnTertiaryContainerDark,
    surface                = SurfaceDark,
    onSurface              = OnSurfaceDark,
    surfaceVariant         = SurfaceVariantDark,
    onSurfaceVariant       = OnSurfaceVariantDark,
    inverseSurface         = InverseSurfaceDark,
    inverseOnSurface       = InverseOnSurfaceDark,
    inversePrimary         = InversePrimaryDark,
    outline                = OutlineDark,
    outlineVariant         = OutlineVariantDark,
    surfaceBright          = SurfaceBrightDark,
    surfaceDim             = SurfaceDimDark,
    surfaceContainer       = SurfaceContainerDark,
    surfaceContainerHigh   = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceContainerLow    = SurfaceContainerLowDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
)

@Composable
fun SmartPantryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
