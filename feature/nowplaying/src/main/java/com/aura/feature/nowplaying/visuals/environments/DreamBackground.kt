package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dream -- Extreme Fidelity:
 *  1. Base Image (bg_dream.jpg)
 *  2. Twinkling Stars
 *  3. Glowing Floating Orbs
 */
@Composable
fun DreamBackground(
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
        label = "dream",
        backgroundId = R.drawable.bg_dream
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Twinkling stars
        for (i in 0 until 30) {
            val seed = i * 137
            val sx = ((seed * 53) % 100) / 100f
            val sy = ((seed * 89) % 70) / 100f
            val twinkle = (sin(timeMs / 900f + seed) + 1f) / 2f
            drawCircle(
                color = Color.White.copy(alpha = (0.2f + twinkle * 0.5f) * brightness),
                radius = 1.2f + twinkle * 1.5f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Layer 3: Glowing Floating Orbs
        for (i in 0 until 5) {
            val phase = timeMs / (1800f + i * 400f)
            val px = size.width * (0.2f + i * 0.15f + sin(phase) * 0.08f)
            val py = size.height * (0.4f + cos(phase * 1.1f) * 0.15f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.4f * brightness), Color.Transparent),
                    center = Offset(px, py),
                    radius = 14f
                ),
                radius = 14f,
                center = Offset(px, py)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
