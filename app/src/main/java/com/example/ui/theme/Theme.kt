package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

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

// Real light scheme for outdoor/sunlight readability (the app default). High-contrast
// green-on-white; text colors meet WCAG AAA against the light surfaces.
private val LightColorScheme =
  lightColorScheme(
    primary = LightKisaanColors.accent,
    primaryContainer = LightKisaanColors.surfaceAlt,
    secondary = LightKisaanColors.brandGreen,
    secondaryContainer = LightKisaanColors.surfaceAlt,
    tertiary = LightKisaanColors.gold,
    background = LightKisaanColors.background,
    surface = LightKisaanColors.surface,
    surfaceVariant = LightKisaanColors.surfaceAlt,
    onPrimary = Color.White,
    onPrimaryContainer = LightKisaanColors.textHeading,
    onSecondary = Color.White,
    onSecondaryContainer = LightKisaanColors.textHeading,
    onBackground = LightKisaanColors.textPrimary,
    onSurface = LightKisaanColors.textPrimary,
    onSurfaceVariant = LightKisaanColors.textPrimary.copy(alpha = 0.75f),
    outline = LightKisaanColors.border,
    outlineVariant = LightKisaanColors.border
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep the custom agricultural brand colors — never Material You dynamic color.
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val kisaanColors = if (darkTheme) DarkKisaanColors else LightKisaanColors

  // Provide the semantic tokens the screens read via LocalKisaanColors.current, so a
  // theme switch repaints every migrated surface, not just Material components.
  CompositionLocalProvider(LocalKisaanColors provides kisaanColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
