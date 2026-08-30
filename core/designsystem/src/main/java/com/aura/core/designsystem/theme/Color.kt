package com.aura.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * The "Aura" identity palette — soft pearl/mist neutral base with a signature
 * muted violet accent. Deliberately NOT stark dark-mode and NOT clinical white,
 * so it sits calmly between every environment's mood palette without competing.
 */

// ---- Light theme ----
val AuraPearlMist = Color(0xFFF4F1ED)      // Background
val AuraWarmFog = Color(0xFFE9E4DE)        // Surface / Card
val AuraDeepCharcoal = Color(0xFF211E1B)   // Primary text
val AuraMutedTaupe = Color(0xFF7A736B)     // Secondary text

// ---- Dark theme ----
val AuraNearBlackViolet = Color(0xFF1C1A22) // Background (dark)
val AuraDarkSurface = Color(0xFF26232D)     // Surface / Card (dark)
val AuraOffWhite = Color(0xFFF5F5F7)        // Primary text (dark)
val AuraMutedGrey = Color(0xFFA7AAB5)       // Secondary text (dark)

// ---- Signature accent — carries the brand identity across both themes ----
val AuraVioletAccent = Color(0xFFA79AC7)

/**
 * Per-environment atmosphere palettes. These are used ONLY inside the
 * Now Playing screen and mood tiles — never as app-wide theme colors.
 * Each list is [primary, secondary...] and is intended to be overridden
 * per-song by the real AtmosphereProfile.primaryColor / secondaryColors
 * coming from the backend; these are sensible fallbacks / previews only.
 */
object EnvironmentPalettes {
    val Heaven = listOf(Color(0xFFF7F3EE), Color(0xFFD8CFEA), Color(0xFFB9C9E0))
    val Nature = listOf(Color(0xFF8FA98C), Color(0xFF3F6C51), Color(0xFF5FA8A0))
    val Ocean = listOf(Color(0xFF63C6C9), Color(0xFF2FA3B8), Color(0xFF1B4F91))
    val Dream = listOf(Color(0xFFC5B9E8), Color(0xFF9AA6E0), Color(0xFFF3C7DE))
    val Romantic = listOf(Color(0xFFE8A6B8), Color(0xFFF0C9D6), Color(0xFFB98CB0))
    val Melancholic = listOf(Color(0xFF4B4E78), Color(0xFF6E7191), Color(0xFF564C87))
    val Hopeful = listOf(Color(0xFF9AC1E0), Color(0xFFF3C9A6), Color(0xFFE8B84B))
    val Energetic = listOf(Color(0xFFE8703F), Color(0xFFE84B7A), Color(0xFF9147E8))
}
