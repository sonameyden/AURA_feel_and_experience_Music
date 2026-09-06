package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Romantic -- Extreme Fidelity:
 *  1. Deep Sunset Sky & Glow (Layer 1)
 *  2. Distant Garden Structures (Layer 2)
 *  3. Midground Curving Path & Trees (Layer 3)
 *  4. Foreground Rose Garden (Layer 4/5)
 *  5. Drifting Petals (Layer 6)
 */
@Composable
fun RomanticBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "romantic") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { colors.last() }

        // Layer 1: Sunset sky
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFD47A95).copy(alpha = 0.6f * brightness), Color(0xFFFFD4A9).copy(alpha = 0.85f * brightness))
            )
        )
        // Sunset Sun
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f * brightness), Color.Transparent)),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.5f, size.height * 0.35f)
        )

        // Layer 2: Distant structures
        drawRollingHills(
            yBase = size.height * 0.58f,
            amplitude = 35f,
            color = Color(0xFF8B5E6D),
            brightness = 0.45f * brightness
        )

        // Layer 3: Midground soft hills
        drawRollingHills(
            yBase = size.height * 0.72f,
            amplitude = 50f,
            color = secondary.copy(alpha = 0.75f),
            brightness = 0.8f * brightness
        )

        // Layer 4 & 5: Foreground Flower Garden
        val sway = sin(timeMs / 1000f) * (3f + energy * 6f)
        for (i in 0 until 9) {
            val x = size.width * (0.05f + i * 0.11f)
            val y = size.height * (0.84f + (i % 3) * 0.03f)
            rotate(degrees = sway * (if(i%2==0)1f else -1f), pivot = Offset(x, y)) {
                drawFlower(Offset(x, y), 14f, secondary, primary)
            }
        }

        // Layer 6: Drifting Petals
        for (i in 0 until 12) {
            val phase = timeMs / (4500f + i * 400f)
            val px = size.width * (0.1f + ((i * 31) % 80) / 100f + sin(phase * 1.5f) * 0.04f)
            val py = size.height * (1f - (phase % 1.1f))
            drawOval(
                color = secondary.copy(alpha = 0.6f * (1f - (phase % 1.1f)) * brightness),
                topLeft = Offset(px, py),
                size = Size(8f, 5f)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
