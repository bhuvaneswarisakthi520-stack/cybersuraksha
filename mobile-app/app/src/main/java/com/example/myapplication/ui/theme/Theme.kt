package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SafetyDarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = DarkNavy,
    primaryContainer = AccentTeal.copy(alpha = 0.2f),
    onPrimaryContainer = AccentTeal,
    secondary = AccentCyan,
    onSecondary = DarkNavy,
    secondaryContainer = AccentCyan.copy(alpha = 0.2f),
    onSecondaryContainer = AccentCyan,
    tertiary = WarningAmber,
    onTertiary = DarkNavy,
    error = EmergencyRed,
    onError = TextPrimary,
    errorContainer = EmergencyRed.copy(alpha = 0.2f),
    onErrorContainer = EmergencyRed,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SafetyDarkColorScheme,
        typography = Typography,
        content = content
    )
}