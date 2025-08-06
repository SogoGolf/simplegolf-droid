package com.sogo.golf.msl.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// MSL Golf Light Color Scheme - WCAG AA compliant
private val MSLLightColorScheme = lightColorScheme(
    primary = MSLColors.Primary,
    onPrimary = MSLColors.TextOnPrimary,
    primaryContainer = MSLColors.PrimaryLight,
    onPrimaryContainer = MSLColors.TextPrimary,

    secondary = MSLColors.Secondary,
    onSecondary = MSLColors.TextOnSecondary,
    secondaryContainer = MSLColors.SecondaryLight,
    onSecondaryContainer = MSLColors.TextPrimary,

    tertiary = MSLColors.Fairway,
    onTertiary = MSLColors.TextOnPrimary,

    error = MSLColors.Error,
    onError = MSLColors.TextOnPrimary,
    errorContainer = MSLColors.Error,
    onErrorContainer = MSLColors.TextOnPrimary,

    background = MSLColors.BackgroundPrimary,
    onBackground = MSLColors.TextPrimary,

    surface = MSLColors.Surface,
    onSurface = MSLColors.TextPrimary,
    surfaceVariant = MSLColors.BackgroundSecondary,
    onSurfaceVariant = MSLColors.TextSecondary,

    outline = MSLColors.Border,
    outlineVariant = MSLColors.Divider,
)

// MSL Golf Dark Color Scheme (for dark mode support)
private val MSLDarkColorScheme = darkColorScheme(
    primary = MSLColors.PrimaryLight,
    onPrimary = MSLColors.TextPrimary,
    primaryContainer = MSLColors.PrimaryDark,
    onPrimaryContainer = MSLColors.TextOnPrimary,

    secondary = MSLColors.SecondaryLight,
    onSecondary = MSLColors.TextPrimary,
    secondaryContainer = MSLColors.SecondaryDark,
    onSecondaryContainer = MSLColors.TextOnPrimary,

    tertiary = MSLColors.Fairway,
    onTertiary = MSLColors.TextPrimary,

    error = MSLColors.Error,
    onError = MSLColors.TextOnPrimary,

    background = MSLColors.TextPrimary,
    onBackground = MSLColors.TextOnPrimary,

    surface = MSLColors.TextSecondary,
    onSurface = MSLColors.TextOnPrimary,
    surfaceVariant = MSLColors.TextTertiary,
    onSurfaceVariant = MSLColors.TextOnPrimary,

    outline = MSLColors.Border,
    outlineVariant = MSLColors.Divider,
)

@Composable
fun MSLGolfTheme(
    darkTheme: Boolean = false, // Always use light theme regardless of system setting
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MSLDarkColorScheme
        else -> MSLLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MSLMaterial3Typography,
        content = content
    )
}