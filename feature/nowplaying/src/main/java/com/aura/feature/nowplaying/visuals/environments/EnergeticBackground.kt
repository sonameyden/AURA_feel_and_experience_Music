package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R
import kotlin.math.sin

/**
 * Energetic -- Extreme Fidelity:
 *  1. Base Image (bg_energetic.jpg)
 *  2. Hexagonal Light Tunnel Overlay
 *  3. Reactive Geometric Particles
 */
@Composable
fun EnergeticBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(
        profile = profile,
        liveAudioEnergy = liveAudioEnergy,
        beatPulse = beatPulse,
        modifier = modifier,
        label = "energetic",
        backgroundId = R.drawable.bg_energetic
    ) { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { colors.last() }

        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringCount = 8
        val rotationSpeed = 3200f - energy * 2000f

        // Overlay Tunnel
        for (i in 0 until ringCount) {
            val progress = ((timeMs / rotationSpeed) + (i.toFloat() / ringCount)) % 1f
            val radius = progress * size.width * 0.85f
            val alpha = (1f - progress) * (0.4f + energy * 0.6f) * brightness
            val color = if (i % 2 == 0) primary else secondary
            
            drawHexagon(
                center = Offset(cx, cy),
                radius = radius,
                angle = timeMs / 4000f + i * 0.2f,
                color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                strokeWidth = (1.5.dp.toPx() + energy * 5.dp.toPx()) * (1f - progress)
            )
        }

        // Geometric Particles
        for (i in 0 until 18) {
            val sx = ((i * 157) % 100) / 100f
            val sy = ((i * 383) % 100) / 100f
            val scale = (sin(timeMs / 600f + i) + 1f) / 2f
            drawCircle(
                color = Color.White.copy(alpha = 0.2f * scale * brightness),
                radius = 1.2f + energy * 4f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
