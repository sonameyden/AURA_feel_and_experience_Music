package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Energetic -- Extreme Fidelity:
 *  1. Deep Atmospheric Gradient (Layer 1)
 *  2. Distant High-Energy Geometry (Layer 2)
 *  3. Midground Hexagonal Tunnel & Flowing Light (Layer 3)
 *  4. Foreground Geometric Ledge (Layer 4/5)
 *  5. Reactive Light Overlays & Sparkles (Layer 6)
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
        val secondary = colors.getOrElse(1) { colors.last() }

        // Layer 1: Atmospheric Gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF2E1A47).copy(alpha = 0.85f * brightness), primary.copy(alpha = 0.55f * brightness))
            )
        )

        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringCount = 10
        val rotationSpeed = 3000f - energy * 2200f

        // Layers 2 & 3: Hexagonal Tunnel
        for (i in 0 until ringCount) {
            val progress = ((timeMs / rotationSpeed) + (i.toFloat() / ringCount)) % 1f
            val radius = progress * size.width * 0.9f
            val alpha = (1f - progress) * (0.45f + energy * 0.55f) * brightness
            val color = if (i % 2 == 0) primary else secondary
            
            drawHexagon(
                center = Offset(cx, cy),
                radius = radius,
                angle = timeMs / 3500f + i * 0.15f,
                color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                strokeWidth = (2.dp.toPx() + energy * 6.dp.toPx()) * (1f - progress)
            )
            
            // Layer 3: Flowing Light Paths
            if (energy > 0.35f) {
                rotate(degrees = (timeMs / 800f) * 60f + i * 36f, pivot = Offset(cx, cy)) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.12f * energy * brightness * (1f - progress)),
                        startAngle = 0f,
                        sweepAngle = 45f + energy * 30f,
                        useCenter = false,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        // Layer 6: Radiant Light Overlay
        drawLightRays(
            center = Offset(cx, cy),
            rayCount = 7,
            angleOffset = (timeMs / 1000f) % 360f,
            rayLength = size.width * 0.6f,
            rayWidth = 50f,
            color = Color.White.copy(alpha = 0.04f * energy * brightness)
        )

        // Sparkle Particles
        for (i in 0 until 20) {
            val sx = ((i * 149) % 100) / 100f
            val sy = ((i * 373) % 100) / 100f
            val scale = (sin(timeMs / 500f + i) + 1f) / 2f
            drawCircle(
                color = Color.White.copy(alpha = 0.25f * scale * brightness),
                radius = 1.5f + energy * 4f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Layer 4/5: Platform
        drawRestingLedge()
    }
}
