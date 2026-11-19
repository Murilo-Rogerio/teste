package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ApexDarkColorScheme = darkColorScheme(
    primary = ApexPrimary,
    onPrimary = ApexOnPrimary,
    primaryContainer = ApexPrimaryContainer,
    onPrimaryContainer = ApexOnPrimaryContainer,
    secondary = ApexSecondary,
    onSecondary = ApexOnSecondary,
    secondaryContainer = ApexSecondaryContainer,
    onSecondaryContainer = ApexOnSecondaryContainer,
    tertiary = ApexTertiary,
    onTertiary = ApexOnTertiary,
    tertiaryContainer = ApexTertiaryContainer,
    onTertiaryContainer = ApexOnTertiaryContainer,
    background = ApexSurface,
    onBackground = ApexOnSurface,
    surface = ApexSurface,
    onSurface = ApexOnSurface,
    surfaceVariant = ApexSurfaceContainerHighest,
    onSurfaceVariant = ApexOnSurfaceVariant,
    surfaceContainerLowest = ApexSurfaceContainerLowest,
    surfaceContainerLow = ApexSurfaceContainerLow,
    surfaceContainer = ApexSurfaceContainer,
    surfaceContainerHigh = ApexSurfaceContainerHigh,
    surfaceContainerHighest = ApexSurfaceContainerHighest,
    outline = ApexOutline,
    outlineVariant = ApexOutlineVariant,
    error = ApexError,
    onError = ApexOnError,
    errorContainer = ApexErrorContainer,
    onErrorContainer = ApexOnErrorContainer
)

@Composable
fun ApexPerformanceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography = ApexTypography,
        content = content
    )
}
