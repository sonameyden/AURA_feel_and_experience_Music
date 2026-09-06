package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import com.aura.feature.nowplaying.R
import kotlin.math.sin
import kotlin.random.Random

/**
 * Ocean -- Extreme Fidelity:
 *  1. Base Image (bg_ocean.jpg)
 *  2. Water Sparkles
 */
@Composable
fun OceanBackground(
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
        label = "ocean",
        backgroundId = R.drawable.bg_ocean
    ) { colors, energy, brightness, timeMs ->
        // Layer 2: Water Sparkles
        for (i in 0 until 16) {
            val sx = Random(i).nextFloat()
            val sy = 0.55f + Random(i + 30).nextFloat() * 0.4f
            val alpha = ((sin(timeMs / 600f + i) + 1f) / 2f) * 0.7f
            drawCircle(
                color = Color.White.copy(alpha = alpha * brightness),
                radius = 1.5f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
