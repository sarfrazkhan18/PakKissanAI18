package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Warm Organic Pakistani Agriculture Theme (سبز اور سنہری)
val AgriGreenPrimary = Color(0xFF1B5E20) // Deep agricultural green
val AgriGreenDark = Color(0xFF0B300F) // Rich soil background
val AgriGreenLight = Color(0xFFE8F5E9) // Soft fertile green

val AgriGoldAccent = Color(0xFFF5B041) // Rich golden wheat
val AgriOrangeAccent = Color(0xFFD35400) // Clay/irrigation canal orange

// Neutral Surfaces
val AgriLightBackground = Color(0xFFF4F9F5) // Fresh air background
val AgriLightSurface = Color(0xFFFFFFFF)
val AgriCardUserLight = Color(0xFFDCF8C6) // Soft classic chat light green
val AgriCardModelLight = Color(0xFFFFFFFF) // Crisp white

// --- Elegant Dark Theme Specifications ---
val ElegantDarkBg = Color(0xFF0A0C0B)         // Deep dark slate black
val ElegantDarkText = Color(0xFFE1E3E1)       // Soft readable off-white
val ElegantDarkPrimary = Color(0xFFD1E8D1)    // Soft minty sage green primary
val ElegantDarkSecondary = Color(0xFF10B981)  // Vibrant emerald pulse
val ElegantDarkSurface = Color(0xFF1F2420)    // Dark forest green-black surface
val ElegantDarkBorder = Color(0xFF3E4A40)     // Forest gray outline border

// Dark surfaces
val AgriDarkBackground = Color(0xFF0C1910) // Dark night field
val AgriDarkSurface = Color(0xFF112517)
val AgriCardUserDark = Color(0xFF1D4229)
val AgriCardModelDark = Color(0xFF1B2D21)

// ---------------------------------------------------------------------------
// Semantic theme tokens (KisaanColors)
//
// The screens historically hardcoded dark hex values everywhere, which forced a
// dark UI in all conditions — unreadable in Punjab sunlight on a cheap LCD.
// These tokens replace those literals so the whole app can switch light/dark.
// LIGHT is the default (see FarmersViewModel.darkMode) because most usage is
// outdoors in bright daylight.
//
// Only *theme-dependent* colors live here (backgrounds, surfaces, text, borders,
// brand accent, gold). Purely semantic colors that read fine on both themes
// (error red, info blue, decorative pink/purple) stay as literals at call sites.
// ---------------------------------------------------------------------------
data class KisaanColors(
    val background: Color,   // app background
    val surface: Color,      // cards / panels
    val surfaceAlt: Color,   // insets / secondary panels / input fields
    val border: Color,       // outlines / dividers
    val textPrimary: Color,  // body text
    val textHeading: Color,  // headings / emphasis text
    val accent: Color,       // primary action / emerald
    val brandGreen: Color,   // deep brand green (fills with light text)
    val gold: Color          // wheat gold accent
)

val LightKisaanColors = KisaanColors(
    background = Color(0xFFF4F9F5),  // fresh pale green-white
    surface = Color(0xFFFFFFFF),     // white cards
    surfaceAlt = Color(0xFFEAF3EC),  // soft green inset
    border = Color(0xFFCBDCCF),      // light green-grey outline
    textPrimary = Color(0xFF16241A), // near-black green — AAA on light
    textHeading = Color(0xFF14532D), // deep green heading
    accent = Color(0xFF0E7A5B),      // darker emerald for contrast on white
    brandGreen = Color(0xFF1B5E20),
    gold = Color(0xFFB45309)         // darker amber — readable on white
)

val DarkKisaanColors = KisaanColors(
    background = Color(0xFF0A0C0B),
    surface = Color(0xFF1F2420),
    surfaceAlt = Color(0xFF131A15),
    border = Color(0xFF3E4A40),
    textPrimary = Color(0xFFE1E3E1),
    textHeading = Color(0xFFD1E8D1),
    accent = Color(0xFF10B981),
    brandGreen = Color(0xFF1B5E20),
    gold = Color(0xFFF5B041)
)

val LocalKisaanColors = staticCompositionLocalOf { DarkKisaanColors }

