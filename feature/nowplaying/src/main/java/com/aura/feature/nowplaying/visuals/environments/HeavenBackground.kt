package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.aura.core.model.AtmosphereProfile

/**
 * Heaven -- matches heaven.jpg's layer stack:
 *  1. Sunrise/pearl sky gradient
 *  2. Distant light-ray silhouette (rotates slowly, speeds slightly with energy)
 *  3. Midground drifting cumulus clouds
 *  4/5. Foreground resting ledge
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
        val secondary = colors.getOrElse(1) { primary }
        val tertiary = colors.getOrElse(2) { secondary }

        // Layer 1: sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(primary.copy(alpha = 0.55f * brightness), tertiary.copy(alpha = 0.85f * brightness))
            )
        )

        // Layer 2: soft radiating light rays, rotation speeds up a touch with energy
        val rayCount = 6
        rotate(degrees = (timeMs / (800f - energy * 200f)) % 360f) {
            for (i in 0 until rayCount) {
                rotate(degrees = 360f / rayCount * i) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width / 2f - 40f, -size.height * 0.6f)
                        lineTo(size.width / 2f + 40f, -size.height * 0.6f)
                        close()
                    }
                    drawPath(path, color = Color.White.copy(alpha = 0.05f * brightness))
                }
            }
        }

        // Layer 3: midground drifting clouds
        for (i in 0 until 4) {
            val speed = 6000f + i * 1500f
            val xFrac = ((timeMs / speed) + i * 0.27f) % 1.3f - 0.15f
            val yFrac = 0.15f + i * 0.14f
            drawCloud(
                center = Offset(size.width * xFrac, size.height * yFrac),
                scale = 0.5f + i * 0.12f,
                color = Color.White.copy(alpha = (0.5f + energy * 0.2f) * brightness)
            )
        }
        drawCircle(
            brush = Brush.radialGradient(listOf(secondary.copy(alpha = 0.35f * brightness), Color.Transparent)),
            radius = size.minDimension * 0.4f,
            center = Offset(size.width * 0.7f, size.height * 0.25f)
        )

        // Layer 4/5: foreground resting ledge
        drawRestingLedge()
    }
}
