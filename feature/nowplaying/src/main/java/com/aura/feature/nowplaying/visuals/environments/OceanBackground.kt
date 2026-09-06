package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin
import kotlin.random.Random

/**
 * Ocean -- matches ocean.jpg's layer stack:
 *  1. Sky gradient
 *  2. Distant horizon haze
 *  3-5. Layered wave surfaces -- amplitude scales directly with music energy
 *  6/7. Rocky shoreline (the resting ledge, shore-shaped)
 *  9. Sparse sparkle motes on the water
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
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFBEEAF0).copy(alpha = 0.6f * brightness), tertiary.copy(alpha = 0.4f * brightness))
            )
        )

        // Layer 2: horizon haze
        drawRect(
            color = tertiary.copy(alpha = 0.4f * brightness),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height * 0.5f)
        )

        // Layer 3-5: layered waves -- amplitude directly tracks energy (song + live audio)
        listOf(0.55f to primary, 0.68f to secondary, 0.82f to tertiary).forEachIndexed { i, (yFrac, color) ->
            val amp = (6f + energy * 14f) * (1f + i * 0.3f)
            val speed = 1400f - i * 250f
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * yFrac)
                var x = 0f
                val step = size.width / 24f
                while (x <= size.width) {
                    val y = size.height * yFrac + sin((x / 60f) + timeMs / speed + i) * amp
                    lineTo(x, y)
                    x += step
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = color.copy(alpha = (0.55f + i * 0.15f) * brightness))
        }

        // Layer 9: sparse sparkle motes
        for (i in 0 until 10) {
            val sx = Random(i).nextFloat()
            val sparkleY = size.height * (0.35f + 0.1f * (i % 3))
            val alpha = ((sin(timeMs / 500f + i) + 1f) / 2f) * 0.5f
            drawCircle(color = Color.White.copy(alpha = alpha), radius = 1.5f, center = Offset(size.width * sx, sparkleY))
        }

        // Layer 6/7: rocky shoreline -- widened resting ledge doubling as the shore
        drawRestingLedge(widthFrac = 0.5f, yFrac = 0.88f)
    }
}
