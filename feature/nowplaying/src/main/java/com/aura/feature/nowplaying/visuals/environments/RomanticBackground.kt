package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Romantic -- matches romantic.jpg's layer stack:
 *  1. Sunset sky + glow
 *  3. Midground swaying flower row (sway reacts to energy)
 *  4/5. Foreground flower field + garden path ledge
 *  6. Drifting petals
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
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: sunset sky + glow
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF5D6DE).copy(alpha = 0.5f * brightness), Color(0xFFFBEFEA).copy(alpha = 0.9f * brightness))
            )
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(secondary.copy(alpha = 0.6f * brightness), Color.Transparent)),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.5f, size.height * 0.4f)
        )

        // Layer 3/4: swaying flower row -- sway amplitude tracks energy
        val sway = sin(timeMs / 1000f) * (3f + energy * 5f)
        for (i in 0 until 6) {
            val xFrac = 0.1f + i * 0.16f
            rotate(degrees = sway * (if (i % 2 == 0) 1f else -1f), pivot = Offset(size.width * xFrac, size.height * 0.92f)) {
                drawFlower(Offset(size.width * xFrac, size.height * 0.9f), 10f, primary, tertiary)
            }
        }

        // Layer 6: drifting petals
        for (i in 0 until 8) {
            val speed = 5000f + i * 400f
            val yFrac = ((timeMs / speed) + i * 0.2f) % 1f
            val xFrac = 0.1f + ((i * 53) % 80) / 100f + sin(timeMs / 900f + i) * 0.03f
            drawCircle(
                color = tertiary.copy(alpha = 0.5f * (1f - yFrac) * brightness),
                radius = 3f,
                center = Offset(size.width * xFrac, size.height * (1f - yFrac))
            )
        }

        // Layer 5: garden path / resting ledge
        drawRestingLedge()
    }
}
