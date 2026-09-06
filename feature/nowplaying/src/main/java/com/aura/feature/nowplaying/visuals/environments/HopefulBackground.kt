package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile

/**
 * Hopeful -- matches hopeful.jpg's layer stack:
 *  1. Sunrise sky gradient
 *  2. Distant mountain-silhouette glow that brightens over a slow cycle
 *     (swap `cycle` for real song-position progress once section timestamps
 *     are wired -- same idea KaleidoscopeLayer's `progress` param uses)
 *  4/5. Foreground hill / resting ledge
 *  6. Light rays -- intensity gets a boost from live music energy
 */
@Composable
fun HopefulBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "hopeful") { _, energy, brightness, timeMs ->
        // Layer 1: sunrise sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF7EA8D8).copy(alpha = 0.5f * brightness), Color(0xFFF3D9A6).copy(alpha = 0.85f * brightness))
            )
        )

        val cycle = (timeMs / 12000f) % 1f
        val horizonY = size.height * 0.62f
        val glowColors = listOf(Color(0xFF9AC1E0), Color(0xFFF3C9A6), Color(0xFFE8B84B))
        val scaled = cycle * (glowColors.size - 1)
        val lowIndex = scaled.toInt().coerceIn(0, glowColors.size - 2)
        val blended = lerpColor(glowColors[lowIndex], glowColors[lowIndex + 1], scaled - lowIndex)

        // Layer 2: distant horizon glow that brightens over the cycle
        drawCircle(
            brush = Brush.radialGradient(listOf(blended.copy(alpha = (0.4f + cycle * 0.4f) * brightness), Color.Transparent)),
            radius = size.minDimension * (0.35f + cycle * 0.25f),
            center = Offset(size.width * 0.5f, horizonY)
        )
        drawLine(
            color = blended.copy(alpha = 0.6f * brightness),
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = 2f
        )

        // Layer 6: light rays -- alpha gets a boost from live music energy on top of the cycle
        for (i in 0 until 5) {
            val rayAlpha = (0.08f + cycle * 0.12f + energy * 0.1f) * brightness
            drawLine(
                color = Color.White.copy(alpha = rayAlpha),
                start = Offset(size.width * 0.5f, horizonY),
                end = Offset(size.width * (0.2f + i * 0.15f), 0f),
                strokeWidth = 3f
            )
        }

        // Layer 4/5: foreground hill / resting ledge
        drawRestingLedge()
    }
}
