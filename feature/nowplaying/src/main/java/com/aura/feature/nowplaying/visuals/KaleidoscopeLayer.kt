package com.aura.feature.nowplaying.visuals

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.aura.core.model.KaleidoscopeStyle

/**
 * Math-driven kaleidoscope — the one visual element that MUST be code-generated
 * rather than an asset, since it needs continuous, precise sync to BPM/beat/
 * amplitude rather than a looping clip (see planning discussion on why this
 * layer specifically can't be a Rive/Lottie asset).
 *
 * Draws one "wedge" of shapes, mirrored/rotated N times around the center.
 */
@Composable
fun KaleidoscopeLayer(
    style: KaleidoscopeStyle,
    energy: Float,          // drives complexity / expansion
    beatPulse: Boolean,     // brief true on detected beat — drives the pulse
    tintHex: String,
    modifier: Modifier = Modifier
) {
    val symmetry = when (style) {
        KaleidoscopeStyle.SharpGeometric -> 10
        KaleidoscopeStyle.WaveLike -> 8
        KaleidoscopeStyle.Floral -> 8
        KaleidoscopeStyle.FluidSymmetry -> 6
        KaleidoscopeStyle.SoftOrganic -> 6
        KaleidoscopeStyle.DarkReflective -> 5
    }
    val shapeCount = (2 + energy * 6).toInt()
    val tint = runCatching { Color(android.graphics.Color.parseColor(tintHex)) }.getOrDefault(Color.Magenta)

    val infiniteTransition = rememberInfiniteTransition(label = "kaleidoscope_rotation")
    val rotationSpeedMs = (6000 - (energy * 4000)).toInt().coerceAtLeast(800) // faster rotation at higher energy
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale = if (beatPulse) 1.15f else 1f

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 4f) * pulseScale

        for (i in 0 until symmetry) {
            rotate(degrees = rotation + (360f / symmetry) * i, pivot = center) {
                repeat(shapeCount) { shapeIndex ->
                    val distance = baseRadius * (shapeIndex + 1) / shapeCount
                    drawCircle(
                        color = tint.copy(alpha = 0.25f),
                        radius = 6f + energy * 10f,
                        center = Offset(center.x, center.y - distance)
                    )
                }
            }
        }
    }
}
