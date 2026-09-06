package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Hopeful -- Extreme Fidelity:
 *  1. Sunrise Sky Gradient (Layer 1)
 *  2. Distant Horizon Glow (Layer 2)
 *  3. Midground Rolling Landscape & Path (Layer 3 - Bezier)
 *  4. Foreground Soft Hill (Layer 4)
 *  5. Radiating God Rays & Motes (Layer 6)
 */
@Composable
fun HopefulBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "hopeful") { colors, energy, brightness, timeMs ->
        // Layer 1: Sky
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF7DA8D8).copy(alpha = 0.55f * brightness), Color(0xFFF4DAB0).copy(alpha = 0.85f * brightness))
            )
        )

        val cycle = (timeMs / 10000f) % 1f
        val horizonY = size.height * 0.64f
        val glowColors = listOf(Color(0xFF9BC1E0), Color(0xFFF3C9A6), Color(0xFFE8B84B))
        val scaled = cycle * (glowColors.size - 1)
        val lowIndex = scaled.toInt().coerceIn(0, glowColors.size - 2)
        val blended = lerpColor(glowColors[lowIndex], glowColors[lowIndex + 1], scaled - lowIndex)

        // Layer 2: Distant Horizon Glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(blended.copy(alpha = (0.45f + cycle * 0.35f) * brightness), Color.Transparent),
                center = Offset(size.width * 0.5f, horizonY),
                radius = size.width * (0.35f + cycle * 0.2f)
            ),
            radius = size.width,
            center = Offset(size.width * 0.5f, horizonY)
        )

        // Layer 3: Midground Landscape
        drawRollingHills(
            yBase = size.height * 0.68f,
            amplitude = 45f,
            color = Color(0xFFD6B998).copy(alpha = 0.65f),
            brightness = brightness
        )

        // Layer 5: Dynamic Light Rays
        drawLightRays(
            center = Offset(size.width * 0.5f, horizonY),
            rayCount = 6,
            angleOffset = -15f + energy * 8f,
            rayLength = size.height * 0.7f,
            rayWidth = 45f,
            color = Color.White.copy(alpha = (0.06f + cycle * 0.1f + energy * 0.05f) * brightness)
        )

        // Layer 4: Foreground Hill
        drawRollingHills(
            yBase = size.height * 0.86f,
            amplitude = 30f,
            color = Color(0xFFB59A73),
            brightness = brightness
        )

        // Floating Motes
        for (i in 0 until 12) {
            val sx = ((i * 67) % 100) / 100f
            val sy = 0.25f + ((i * 41) % 55) / 100f
            val alpha = (0.15f + energy * 0.35f) * ((sin(timeMs / 1200f + i) + 1f) / 2f)
            drawCircle(
                color = Color.White.copy(alpha = alpha * brightness),
                radius = 1.3f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
