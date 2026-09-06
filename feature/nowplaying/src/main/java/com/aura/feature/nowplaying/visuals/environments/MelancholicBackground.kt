package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.aura.core.model.AtmosphereProfile

/**
 * Melancholic -- rain-streaked window over an indigo/slate sky, per the
 * concept spec's Section 8 description ("window frame, rain streaks, city
 * skyline"). Rain fall-speed quickens with music energy.
 */
@Composable
fun MelancholicBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "melancholic") { colors, energy, brightness, timeMs ->
        val secondary = colors.getOrElse(1) { colors[0] }

        // Layer 1: dim indigo sky
        drawRect(
            brush = Brush.verticalGradient(
                listOf(colors[0].copy(alpha = 0.55f * brightness), Color(0xFF15131C).copy(alpha = 0.85f * brightness))
            )
        )

        // Layer 2: rain streaks -- fall speed reacts inversely to energy (heavier rain = faster)
        for (i in 0 until 26) {
            val seed = i * 71
            val xFrac = ((seed * 31) % 100) / 100f
            val speed = 700f - energy * 300f
            val fall = ((timeMs / speed) + (seed % 50) / 50f) % 1.2f
            val yStart = size.height * (fall - 0.2f)
            drawLine(
                color = secondary.copy(alpha = 0.35f * brightness),
                start = Offset(size.width * xFrac, yStart),
                end = Offset(size.width * xFrac - 6f, yStart + 26f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
        }

        // Layer 3: window frame
        val frameColor = Color(0xFF1C1A22).copy(alpha = 0.4f * brightness)
        drawLine(frameColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = 4f)
        drawLine(frameColor, Offset(0f, size.height * 0.4f), Offset(size.width, size.height * 0.4f), strokeWidth = 4f)
        drawRect(color = frameColor, topLeft = Offset.Zero, size = size, style = Stroke(width = 6f))

        // Layer 5: windowsill (resting ledge)
        drawRestingLedge()
    }
}
