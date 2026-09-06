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
 * Nature -- Extreme Fidelity:
 *  1. Base Sky Gradient (Layer 1)
 *  2. Distant Mountains & Haze (Layer 2)
 *  3. Midground Rolling Meadow Hills (Layer 3 - Bezier curves)
 *  4. Tall Midground Trees (Layer 4)
 *  5. Foreground Grass & Detailed Ledge (Layer 5)
 */
@Composable
fun NatureBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "nature") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { colors.last() }

        // Layer 1: sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFCEE0F4).copy(alpha = 0.6f * brightness), Color.White.copy(alpha = 0.8f * brightness))
            )
        )

        // Layer 2: Distant mountain haze
        drawRollingHills(
            yBase = size.height * 0.48f,
            amplitude = 40f,
            color = Color(0xFFAAB8C2),
            brightness = 0.4f * brightness
        )

        // Layer 3: Midground soft rolling hills
        drawRollingHills(
            yBase = size.height * 0.62f,
            amplitude = 60f,
            color = secondary.copy(alpha = 0.85f),
            brightness = 0.8f * brightness,
            timeOffset = timeMs / 10000f // Very slow drift
        )

        // Layer 4: Tall Trees
        val treeSway = sin(timeMs / 1200f) * (1.5f + energy * 3f)
        listOf(0.18f, 0.35f, 0.85f).forEachIndexed { i, xFrac ->
            rotate(degrees = treeSway * (if(i%2==0)1f else -1f), pivot = Offset(size.width * xFrac, size.height * 0.72f)) {
                drawTree(Offset(size.width * xFrac, size.height * 0.72f), 0.9f + i * 0.1f, primary)
            }
        }

        // Layer 5: Foreground Scenery Framing (Bottom grass silhouette)
        drawRollingHills(
            yBase = size.height * 0.82f,
            amplitude = 25f,
            color = primary,
            brightness = 1.0f * brightness
        )
        
        // Optional Framing (Layer 4 from moodboard)
        drawForestFraming(primary, brightness)

        // Final Ledge
        drawRestingLedge()
    }
}
