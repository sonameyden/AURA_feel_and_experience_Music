package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R

/**
 * Romantic -- Extreme Fidelity:
 *  1. Base Image (bg_romantic.jpg)
 *  2. Falling Rose Petals
 */
@Composable
fun RomanticBackground(
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
        label = "romantic",
        backgroundId = R.drawable.bg_romantic
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Falling Petals
        drawFallingElements(
            count = 15,
            energy = energy,
            timeMs = timeMs,
            color = Color(0xFFF06292), // Rose pink
            brightness = brightness
        )

        // Final Ledge
        drawRestingLedge()
    }
}
