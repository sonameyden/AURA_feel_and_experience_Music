package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.aura.core.model.AtmosphereProfile
import kotlin.math.sin

/**
 * Nature -- matches nature.jpg's layer stack:
 *  1. Sky gradient
 *  2. Distant mountain haze / sun glow
 *  3. Midground rolling hill bands
 *  4. Foreground scenery: swaying trees (sway speed/amplitude reacts to energy)
 *  5. Foreground resting ledge with grass
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
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(primary.copy(alpha = 0.5f * brightness), Color(0xFFEFF7EE).copy(alpha = 0.9f * brightness))
            )
        )

        // Layer 2: distant sun glow / haze
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFFFE9B0).copy(alpha = 0.5f * brightness), Color.Transparent)),
            radius = size.minDimension * 0.3f,
            center = Offset(size.width * 0.8f, size.height * 0.2f)
        )

        // Layer 3: rolling hill bands (back to front)
        listOf(tertiary.copy(alpha = 0.9f), secondary.copy(alpha = 0.95f), primary).forEachIndexed { i, color ->
            val baseY = size.height * (0.62f + i * 0.13f)
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, baseY)
                cubicTo(
                    size.width * 0.3f, baseY - size.height * 0.06f,
                    size.width * 0.7f, baseY + size.height * 0.05f,
                    size.width, baseY - size.height * 0.03f
                )
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = color.copy(alpha = color.alpha * brightness))
        }

        // Layer 4: swaying trees -- sway amplitude tracks music energy
        val sway = sin(timeMs / 900f) * (2f + energy * 6f)
        listOf(0.2f, 0.35f, 0.82f).forEachIndexed { i, xFrac ->
            rotate(degrees = sway * (if (i % 2 == 0) 1f else -1f), pivot = Offset(size.width * xFrac, size.height * 0.78f)) {
                drawTree(Offset(size.width * xFrac, size.height * 0.78f), 0.7f + i * 0.15f, secondary)
            }
        }

        // Layer 5: foreground resting ledge
        drawRestingLedge()
    }
}
