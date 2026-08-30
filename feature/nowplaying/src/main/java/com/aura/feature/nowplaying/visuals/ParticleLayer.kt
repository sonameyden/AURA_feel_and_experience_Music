package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aura.core.model.ParticleStyle
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    var alpha: Float
)

/**
 * Custom Compose Canvas particle system — deliberately NOT a Rive asset.
 * Particle count/speed/spread scale directly off `energy`, which is exactly
 * why this is code-driven rather than an illustrated/rigged asset
 * (see the "particles vs Rive" split decided during planning).
 */
@Composable
fun ParticleLayer(
    style: ParticleStyle,
    energy: Float, // 0f..1f — from AtmosphereProfile.energy blended with live AudioAnalyzer amplitude
    tintHex: String,
    modifier: Modifier = Modifier
) {
    if (style == ParticleStyle.None) return

    val particleCount = (20 + energy * 80).toInt() // Low tier would clamp this lower — see Settings/performance tiers
    var particles by remember(style) {
        mutableStateOf(List(particleCount) { randomParticle() })
    }

    val tint = runCatching { Color(android.graphics.Color.parseColor(tintHex)) }.getOrDefault(Color.White)

    Canvas(modifier = modifier.fillMaxSize()) {
        particles = particles.map { p ->
            val updatedY = p.y - (p.speed * (0.5f + energy))
            if (updatedY < 0f) randomParticle().copy(y = size.height) else p.copy(y = updatedY)
        }
        particles.forEach { p ->
            drawCircle(
                color = tint.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x * size.width, p.y)
            )
        }
    }
}

private fun randomParticle(): Particle = Particle(
    x = Random.nextFloat(),
    y = Random.nextFloat() * 1000f,
    speed = Random.nextFloat() * 2f + 0.5f,
    size = Random.nextFloat() * 4f + 1f,
    alpha = Random.nextFloat() * 0.5f + 0.2f
)
