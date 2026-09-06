package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R

/**
 * Heaven -- Extreme Fidelity:
 *  1. Base Image (bg_heaven.jpg)
 *  2. Golden God Rays
 *  3. Subtle Floating Motes
 */
@Composable
fun HeavenBackground(
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
        label = "heaven",
        backgroundId = R.drawable.bg_heaven
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Radiant God Rays
        drawLightRays(
            center = Offset(size.width * 0.5f, size.height * 0.8f),
            rayCount = 8,
            angleOffset = (timeMs / 1600f) % 360f,
            rayLength = size.height * 0.95f,
            rayWidth = 80f,
            color = Color(0xFFFFFDE7).copy(alpha = (0.1f + energy * 0.12f) * brightness)
        )

        // Layer 3: Subtle golden floating motes
        drawFloatingMotes(
            count = 15,
            energy = energy,
            timeMs = timeMs,
            color = Color(0xFFFFF9C4),
            brightness = brightness
        )

        // Final Ledge
        drawRestingLedge()
    }
}
