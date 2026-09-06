package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin
import kotlin.random.Random

/**
 * Ocean -- Extreme Fidelity:
 *  1. Base Sky Gradient (Layer 1)
 *  2. Distant Horizon Haze (Layer 2)
 *  3. expansive Ocean Surface (Layer 3)
 *  4. Parallax Wave Layers (Layer 4/5)
 *  5. Foam Caps & Wave Details
 *  6. Foreground Rocky Shoreline & Tide (Layer 6/7)
 *  7. Reflective Water Glow & Particles (Layer 8/9)
 */
@Composable
fun OceanBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier
) {
    EnvironmentCanvas(profile, liveAudioEnergy, beatPulse, modifier, "ocean") { colors, energy, brightness, timeMs ->
        val primary = colors[0]
        val secondary = colors.getOrElse(1) { colors.last() }
        val tertiary = colors.getOrElse(2) { colors.last() }

        // Layer 1: Sky
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF9DD8DE).copy(alpha = 0.6f * brightness), Color.White.copy(alpha = 0.85f * brightness))
            )
        )

        // Layer 2: Horizon Haze
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xFF6AB6BC).copy(alpha = 0.25f * brightness)),
                startY = size.height * 0.42f,
                endY = size.height * 0.55f
            ),
            topLeft = Offset(0f, size.height * 0.42f),
            size = Size(size.width, size.height * 0.13f)
        )

        // Layer 3: Base surface
        drawRect(
            color = tertiary.copy(alpha = 0.45f * brightness),
            topLeft = Offset(0f, size.height * 0.55f),
            size = Size(size.width, size.height * 0.45f)
        )

        // Layers 4 & 5: Parallax waves with Bezier curves
        listOf(0.62f to secondary, 0.75f to primary).forEachIndexed { i, (yFrac, color) ->
            val amp = (10f + energy * 15f) * (1f + i * 0.5f)
            val speed = 1600f - i * 400f
            
            drawRollingHills(
                yBase = size.height * yFrac,
                amplitude = amp,
                color = color,
                brightness = (0.55f + i * 0.25f) * brightness,
                timeOffset = timeMs / speed + i
            )
            
            // Layer 5: Foam Caps (White paths on top of wave peaks)
            val foamPath = Path().apply {
                var x = 0f
                val step = size.width / 12f
                while (x <= size.width) {
                    val y = size.height * yFrac + (sin((x / 50f) + timeMs / speed + i) * amp).toFloat()
                    if (x == 0f) moveTo(x, y) else lineTo(x, y)
                    x += step
                }
            }
            drawPath(
                path = foamPath,
                color = Color.White.copy(alpha = 0.15f * brightness),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }

        // Layer 8: Reflective Glow
        drawRect(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.18f * brightness), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.78f),
                radius = size.width * 0.45f
            ),
            topLeft = Offset(0f, size.height * 0.55f),
            size = Size(size.width, size.height * 0.45f)
        )

        // Layer 9: Sparkle particles
        for (i in 0 until 14) {
            val sx = Random(i).nextFloat()
            val sy = 0.58f + Random(i + 20).nextFloat() * 0.35f
            val alpha = ((sin(timeMs / 500f + i) + 1f) / 2f) * 0.65f
            drawCircle(
                color = Color.White.copy(alpha = alpha * brightness),
                radius = 1.3f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Layer 6/7: Rocks & Final Ledge
        drawRestingLedge()
    }
}
