package com.aura.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Soft "glass" surface used across Home for mood tiles, current-song card, etc.
 * This is one of the ways Home stays visually rich WITHOUT full-scene animation —
 * depth via subtle translucency/elevation rather than motion.
 *
 * @param accentColor optional tint (e.g. an environment's primary color) blended
 *   at low alpha into the card background — used for mood tiles / current song card.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val base = MaterialTheme.colorScheme.surface
    val background = accentColor?.let { base.copy(alpha = 0.92f).compositeOver(it, alpha = 0.12f) } ?: base

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
    ) {
        content()
    }
}

// --- small local helper, kept private to this file ---
private fun Color.compositeOver(accent: Color, alpha: Float): Color {
    return Color(
        red = red * (1 - alpha) + accent.red * alpha,
        green = green * (1 - alpha) + accent.green * alpha,
        blue = blue * (1 - alpha) + accent.blue * alpha,
        alpha = 1f
    )
}
