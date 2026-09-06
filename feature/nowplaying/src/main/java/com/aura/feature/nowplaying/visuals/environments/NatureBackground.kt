package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R
import kotlin.math.sin

/**
 * Nature -- Extreme Fidelity:
 *  1. Base Image (bg_nature.jpg)
 *  2. Subtle Falling Leaves (Green/Forest-tone)
 *  3. Firefly Motes (Near the ledge)
 */
@Composable
fun NatureBackground(
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
        label = "nature",
        backgroundId = R.drawable.bg_nature
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Falling Leaves (Music-reactive speed)
        drawFallingElements(
            count = 12,
            energy = energy,
            timeMs = timeMs,
            color = Color(0xFF558B2F), // Forest green
            brightness = brightness,
            isLeaf = true
        )

        // Layer 3: Fireflies
        for (i in 0 until 8) {
            val phase = timeMs / (1400f + i * 250f)
            val px = size.width * (0.2f + i * 0.12f + sin(phase) * 0.06f)
            val py = size.height * (0.75f + sin(phase * 1.5f) * 0.08f)
            drawCircle(
                color = Color(0xFFDCEDC8).copy(alpha = (0.25f + energy * 0.45f) * brightness),
                radius = 2.5f,
                center = Offset(px, py)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
