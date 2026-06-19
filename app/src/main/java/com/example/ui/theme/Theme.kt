package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantDarkPrimary,
    primaryContainer = ElegantDarkSurface,
    secondary = ElegantDarkSecondary,
    secondaryContainer = ElegantDarkBorder,
    tertiary = ElegantDarkBorder,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    surfaceVariant = ElegantDarkSurface,
    onPrimary = ElegantDarkBg,
    onPrimaryContainer = ElegantDarkText,
    onSecondary = ElegantDarkBg,
    onSecondaryContainer = ElegantDarkPrimary,
    onBackground = ElegantDarkText,
    onSurface = ElegantDarkText,
    onSurfaceVariant = ElegantDarkText.copy(alpha = 0.7f),
    outline = ElegantDarkBorder,
    outlineVariant = ElegantDarkBorder
  )

private val LightColorScheme = DarkColorScheme // Enforce same elegant dark palette across all view contexts for strict design consistency

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors to preserve our beautiful custom agricultural brand themes
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
