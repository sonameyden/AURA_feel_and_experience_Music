package com.aura.feature.nowplaying.visuals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.aura.core.model.AtmosphereProfile

/**
 * Sits between the Rive scenery and particles — a soft animated gradient that
 * smoothly cross-fades between AtmosphereProfile colors on song transitions
 * (Section 30 of the original concept: "don't abruptly replace the environment").
 */
@Composable
fun ReactiveGradientLayer(
    profile: AtmosphereProfile,
    modifier: Modifier = Modifier
) {
    val primary = runCatching { Color(profile.primaryColorHex.toColorInt()) }
        .getOrDefault(Color.DarkGray)
    val secondary = profile.secondaryColorHexes.firstOrNull()?.let {
        runCatching { Color(it.toColorInt()) }.getOrNull()
    } ?: primary.copy(alpha = 0.4f)

    val animatedPrimary by animateColorAsState(
        targetValue = primary,
        animationSpec = tween(durationMillis = 3000), // 2-5s transition window per the concept spec
        label = "gradient_primary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = secondary,
        animationSpec = tween(durationMillis = 3000),
        label = "gradient_secondary"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedPrimary.copy(alpha = 0.35f), animatedSecondary.copy(alpha = 0.15f))
                )
            )
    )
}
