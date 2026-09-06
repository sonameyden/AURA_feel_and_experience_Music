package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dream -- Extreme Fidelity:
 *  1. Deep Lavender/Star Field Sky (Layer 1)
 *  2. Distant Floating Haze & Island Silhouettes (Layer 2)
 *  3. Midground Floating Islands & Fragments (Layer 3 - Bobbing)
 *  4. Foreground Floating Island Platform (Layer 4/5)
 *  5. Glowing Orbs & Particles (Layer 5)
 *  6. Atmospheric Mist Overlays (Layer 6)
 */
@Composable
fun DreamBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "dream") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { colors.last() }
        val tertiary = colors.getOrElse(2) { colors.last() }

        // Layer 1: Sky & Stars
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF2C2450).copy(alpha = 0.85f * brightness), primary.copy(alpha = 0.5f * brightness))
            )
        )
        // Twinkling stars
        for (i in 0 until 35) {
            val seed = i * 137
            val sx = ((seed * 53) % 100) / 100f
            val sy = ((seed * 89) % 75) / 100f
            val twinkle = (sin(timeMs / 900f + seed) + 1f) / 2f
            drawCircle(
                color = Color.White.copy(alpha = (0.25f + twinkle * 0.6f) * brightness),
                radius = 1.1f + twinkle * 1.6f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Layer 6: Atmospheric Mist (Depth haze)
        drawRect(
            brush = Brush.radialGradient(
                listOf(Color(0xFFBAA3D9).copy(alpha = 0.18f * brightness), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.55f),
                radius = size.width * 0.75f
            )
        )

        // Layer 2: Distant hazy shapes
        listOf(0.25f to 0.42f, 0.55f to 0.38f, 0.82f to 0.45f).forEachIndexed { i, (x, y) ->
            val bob = sin(timeMs / 2800f + i) * (12f + energy * 8f)
            drawOval(
                color = Color(0xFF6A76AD).copy(alpha = 0.35f * brightness),
                topLeft = Offset(size.width * x - 40f, size.height * y + bob),
                size = Size(80f, 35f)
            )
        }

        // Layer 3: Midground Islands & Fragments
        listOf(0.18f to 0.62f, 0.48f to 0.55f, 0.88f to 0.65f).forEachIndexed { i, (x, y) ->
            val bob = sin(timeMs / 1900f + i * 2.5f) * (18f + energy * 15f)
            val center = Offset(size.width * x, size.height * y + bob)
            
            // Island base
            drawOval(
                brush = Brush.verticalGradient(listOf(secondary, secondary.copy(alpha = 0.8f))),
                topLeft = Offset(center.x - 65f, center.y),
                size = Size(130f, 50f)
            )
            // Floating fragment detail
            for (j in 0 until 3) {
                drawCircle(
                    color = tertiary.copy(alpha = 0.75f * brightness),
                    radius = 4.5f,
                    center = center + Offset(sin(timeMs/1100f + j)*25f, -45f - j*18f)
                )
            }
        }

        // Layer 5: Glowing Floating Orbs
        for (i in 0 until 6) {
            val phase = timeMs / (1500f + i * 250f)
            val px = size.width * (0.25f + i * 0.13f + sin(phase) * 0.07f)
            val py = size.height * (0.45f + cos(phase * 1.3f) * 0.12f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.45f * brightness), Color.Transparent),
                    center = Offset(px, py),
                    radius = 15f
                ),
                radius = 15f,
                center = Offset(px, py)
            )
        }

        // Final Ledge
        drawRestingLedge()
    }
}
