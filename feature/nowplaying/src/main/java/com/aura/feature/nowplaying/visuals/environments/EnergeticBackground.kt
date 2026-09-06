package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.aura.core.model.AtmosphereProfile
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Energetic -- matches energetic.jpg's layer stack:
 *  1. Deep atmospheric gradient
 *  2/3. Layered hexagonal light-tunnel rings, rotating -- speed and alpha are
 *       the most music-reactive of any environment (energy directly speeds
 *       rotation, beatPulse brightens via EnvironmentCanvas's bloom)
 *  5. Foreground platform
 */
@Composable
fun EnergeticBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "energetic") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: deep atmospheric gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF2A1B4A).copy(alpha = 0.7f * brightness), primary.copy(alpha = 0.5f * brightness))
            )
        )

        // Layer 2/3: rotating hexagonal light-tunnel rings -- rotation speed tracks energy directly
        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringCount = 6
        val rotationSpeed = 4000f - energy * 2800f
        for (ring in 0 until ringCount) {
            val progress = ((timeMs / rotationSpeed) + ring.toFloat() / ringCount) % 1f
            val radius = progress * size.minDimension * 0.65f
            val alpha = ((1f - progress) * (0.5f + energy * 0.4f) * brightness).coerceIn(0f, 1f)
            val color = if (ring % 2 == 0) primary else secondary
            val sides = 6
            val path = Path()
            for (s in 0..sides) {
                val angle = (2 * PI.toFloat() / sides) * s + timeMs / 3000f
                val x = cx + cos(angle) * radius
                val y = cy + sin(angle) * radius
                if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color.copy(alpha = alpha), style = Stroke(width = 3f))
        }

        // Core glow -- radius pulses with energy, brightness already pulses with beatPulse
        drawCircle(
            brush = Brush.radialGradient(listOf(tertiary.copy(alpha = 0.7f * brightness), Color.Transparent)),
            radius = size.minDimension * (0.12f + energy * 0.08f),
            center = Offset(cx, cy)
        )

        // Layer 5: foreground platform
        drawRestingLedge()
    }
}
