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
 * Melancholic -- Extreme Fidelity (Forest Path):
 *  1. Deep Indigo/Atmospheric Sky (Layer 1)
 *  2. Hazy Distant Forest Silhouettes (Layer 2)
 *  3. Tall Midground Trees & Stone Path (Layer 3)
 *  4. Dark Foreground Framing Branches (Layer 4)
 *  5. Forest Floor Surface & Ledge (Layer 5)
 *  6. Atmospheric God Rays (Layer 6)
 */
@Composable
fun MelancholicBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "melancholic") { colors, energy, brightness, timeMs ->
        val primary = colors[0]

        // Layer 1: Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF2B2D42).copy(alpha = 0.8f * brightness), Color(0xFFC78170).copy(alpha = 0.4f * brightness))
            )
        )

        // Layer 6: God Rays
        drawLightRays(
            center = Offset(size.width * 0.25f, 0f),
            rayCount = 4,
            angleOffset = 30f + sin(timeMs / 4500f) * 4f,
            rayLength = size.height * 1.3f,
            rayWidth = 140f,
            color = Color(0xFFFFFDE7).copy(alpha = 0.08f * brightness)
        )

        // Layer 2: Distant Forest Silhouettes
        drawRollingHills(
            yBase = size.height * 0.55f,
            amplitude = 30f,
            color = Color(0xFF3F3D56),
            brightness = 0.4f * brightness
        )

        // Layer 3: Tall Trees
        val sway = sin(timeMs / 1300f) * 1.5f
        listOf(0.12f, 0.28f, 0.78f).forEachIndexed { i, x ->
            rotate(degrees = sway * (if(i%2==0)1f else -1f), pivot = Offset(size.width * x, size.height * 0.75f)) {
                drawTree(Offset(size.width * x, size.height * 0.75f), 1.1f + i * 0.15f, Color(0xFF2B3D2C))
            }
        }

        // Layer 4 & 5: Forest Floor & Framing
        drawRollingHills(
            yBase = size.height * 0.85f,
            amplitude = 20f,
            color = Color(0xFF1E281F),
            brightness = 1.0f * brightness
        )
        
        drawForestFraming(Color(0xFF141A15), brightness)

        // Final Ledge
        drawRestingLedge()
    }
}
