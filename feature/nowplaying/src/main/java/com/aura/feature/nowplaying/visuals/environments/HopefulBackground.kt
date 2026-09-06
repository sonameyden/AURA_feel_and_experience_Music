package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R

/**
 * Hopeful -- Extreme Fidelity:
 *  1. Base Image (bg_hope.jpg)
 *  2. Radiant Sun Rays
 *  3. Floating Pollen/Motes
 */
@Composable
fun HopefulBackground(
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
        label = "hopeful",
        backgroundId = R.drawable.bg_hope
    ) { colors, energy, brightness, timeMs ->
        val horizonY = size.height * 0.65f

        // Layer 2: Radiant Light Rays
        drawLightRays(
            center = Offset(size.width * 0.5f, horizonY),
            rayCount = 6,
            angleOffset = -18f + energy * 12f,
            rayLength = size.height * 0.75f,
            rayWidth = 55f,
            color = Color.White.copy(alpha = (0.08f + energy * 0.08f) * brightness)
        )

        // Layer 3: Warm Floating Motes
        drawFloatingMotes(
            count = 12,
            energy = energy,
            timeMs = timeMs,
            color = Color(0xFFFFECB3),
            brightness = brightness
        )

        // Final Ledge
        drawRestingLedge()
    }
}
