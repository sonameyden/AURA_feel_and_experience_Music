package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile

/**
 * Heaven -- Extreme Fidelity:
 *  1. Base Sky Gradient (Layer 1)
 *  2. Far Soft Cloud Silhouettes (Layer 2)
 *  3. Midground Shaded Cumulus Clusters (Layer 3/5)
 *  4. Golden God Rays (Layer 6)
 *  5. Foreground Ledge Aligned with Cat
 */
@Composable
fun HeavenBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "heaven") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val tertiary = colors.getOrElse(2) { colors.last() }

        // Layer 1: Base Gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(primary.copy(alpha = 0.45f * brightness), Color.White.copy(alpha = 0.85f * brightness))
            )
        )

        // Layer 6: Radiant God Rays
        drawLightRays(
            center = Offset(size.width * 0.5f, size.height * 0.75f),
            rayCount = 7,
            angleOffset = (timeMs / 1400f) % 360f,
            rayLength = size.height * 0.9f,
            rayWidth = 70f,
            color = Color(0xFFFDEFD0).copy(alpha = (0.12f + energy * 0.1f) * brightness)
        )

        // Layer 2: Hazy background clouds
        for (i in 0 until 4) {
            val x = ((timeMs / 12000f) + i * 0.25f) % 1.2f - 0.1f
            drawVolumetricCloud(
                center = Offset(size.width * x, size.height * 0.35f),
                scale = 0.8f,
                color = Color.White.copy(alpha = 0.25f),
                brightness = brightness
            )
        }

        // Layer 3/5: Foreground volumetric clouds
        for (i in 0 until 3) {
            val x = ((timeMs / 7000f) + i * 0.4f) % 1.4f - 0.2f
            drawVolumetricCloud(
                center = Offset(size.width * x, size.height * 0.65f),
                scale = 1.3f,
                color = Color.White,
                brightness = brightness
            )
        }

        // Large foreground bank (Platform area)
        drawVolumetricCloud(
            center = Offset(size.width * 0.5f, size.height * 0.85f),
            scale = 2.4f,
            color = Color.White,
            brightness = brightness
        )

        // Final Ledge
        drawRestingLedge()
    }
}
