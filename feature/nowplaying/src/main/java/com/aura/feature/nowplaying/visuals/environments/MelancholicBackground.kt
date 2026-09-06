package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R
import kotlin.math.sin

/**
 * Melancholic -- Extreme Fidelity:
 *  1. Base Image (bg_melancholic.jpg)
 *  2. Soft Hazy God Rays
 *  3. Atmospheric Dust Particles
 */
@Composable
fun MelancholicBackground(
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
        label = "melancholic",
        backgroundId = R.drawable.bg_melancholic
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Soft God Rays through trees
        drawLightRays(
            center = Offset(size.width * 0.2f, 0f),
            rayCount = 4,
            angleOffset = 28f + sin(timeMs / 5000f) * 3f,
            rayLength = size.height * 1.4f,
            rayWidth = 160f,
            color = Color(0xFFF3E5AB).copy(alpha = 0.08f * brightness)
        )

        // Layer 3: Atmospheric Dust
        drawFloatingMotes(
            count = 20,
            energy = energy,
            timeMs = timeMs,
            color = Color(0xFFFFF9C4),
            brightness = 0.6f * brightness
        )

        // Final Ledge
        drawRestingLedge()
    }
}
