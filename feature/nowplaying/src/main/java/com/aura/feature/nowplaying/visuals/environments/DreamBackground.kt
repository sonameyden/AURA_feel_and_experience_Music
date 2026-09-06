package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Dream -- matches dream.jpg's layer stack:
 *  1. Deep lavender sky + star field (twinkle speed is time-based, not music,
 *     since it should feel ambient/independent -- stars just exist)
 *  2/3. Distant + midground floating islands/fragments -- bob amplitude
 *       reacts to music energy
 *  4. Foreground floating island platform (resting ledge)
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
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: deep lavender sky
        drawRect(
            brush = Brush.verticalGradient(
                listOf(primary.copy(alpha = 0.6f * brightness), Color(0xFF2A2450).copy(alpha = 0.7f * brightness))
            )
        )

        // Layer 1b: star field
        for (i in 0 until 22) {
            val seed = i * 97
            val sx = ((seed * 37) % 100) / 100f
            val sy = ((seed * 53) % 60) / 100f
            val twinkle = (sin(timeMs / 700f + seed) + 1f) / 2f
            drawCircle(
                color = Color.White.copy(alpha = (0.2f + twinkle * 0.5f) * brightness),
                radius = 1.2f + twinkle * 1.2f,
                center = Offset(size.width * sx, size.height * sy)
            )
        }

        // Layer 2/3: floating islands + fragments -- bob amplitude tracks energy
        listOf(0.25f to 0.6f, 0.65f to 0.75f, 0.85f to 0.5f).forEachIndexed { i, (xFrac, yFrac) ->
            val bob = sin(timeMs / 1600f + i * 2f) * (6f + energy * 6f)
            val center = Offset(size.width * xFrac, size.height * yFrac + bob)
            val islandWidth = size.width * (0.16f + i * 0.03f)
            drawOval(
                color = secondary.copy(alpha = 0.85f * brightness),
                topLeft = Offset(center.x - islandWidth / 2f, center.y),
                size = Size(islandWidth, islandWidth * 0.35f)
            )
            drawTree(Offset(center.x, center.y), 0.5f, tertiary)
        }

        // Layer 4: foreground floating island platform (resting ledge)
        drawRestingLedge()
    }
}
