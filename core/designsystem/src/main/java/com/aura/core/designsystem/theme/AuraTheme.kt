package com.aura.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AuraVioletAccent,
    onPrimary = AuraOffWhite,
    background = AuraPearlMist,
    onBackground = AuraDeepCharcoal,
    surface = AuraWarmFog,
    onSurface = AuraDeepCharcoal,
    secondary = AuraMutedTaupe,
    onSecondary = AuraPearlMist
)

private val DarkColors = darkColorScheme(
    primary = AuraVioletAccent,
    onPrimary = AuraNearBlackViolet,
    background = AuraNearBlackViolet,
    onBackground = AuraOffWhite,
    surface = AuraDarkSurface,
    onSurface = AuraOffWhite,
    secondary = AuraMutedGrey,
    onSecondary = AuraNearBlackViolet
)

/**
 * Root theme wrapper. Wrap the whole app in this once, in MainActivity.
 * Do NOT apply environment-mood colors here — those are local to the
 * Now Playing screen only, layered on top of whichever theme is active.
 */
@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
