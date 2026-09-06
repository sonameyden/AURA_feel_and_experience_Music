package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.LightingStyle
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun EnvironmentCanvas(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    beatPulse: Boolean,
    modifier: Modifier = Modifier,
    label: String,
    backgroundId: Int? = null,
    content: DrawScope.(colors: List<Color>, energy: Float, brightness: Float, timeMs: Float) -> Unit
) {
    val colors = remember(profile.primaryColorHex, profile.secondaryColorHexes) {
        parseEnvColors(profile.primaryColorHex, profile.secondaryColorHexes)
    }

    val animatedIntensity by animateFloatAsState(
        targetValue = profile.energy,
        animationSpec = tween(durationMillis = 2000),
        label = "${label}_intensity"
    )

    var timeMs by remember { mutableFloatStateOf(0f) }
    var bloom by remember { mutableFloatStateOf(0f) }
    val currentBeat by rememberUpdatedState(beatPulse)
    val currentLiveEnergy by rememberUpdatedState(liveAudioEnergy)

    LaunchedEffect(Unit) {
        var last = -1L
        while (isActive) {
            withFrameMillis { frameTime ->
                val dt = if (last >= 0) (frameTime - last).toFloat() else 0f
                last = frameTime
                timeMs += dt

                val target = if (currentBeat) 1f else 0f
                bloom += (target - bloom) * (if (target > bloom) 0.35f else 0.06f)
            }
        }
    }

    val baseBrightness = when (profile.lightingStyle) {
        LightingStyle.Soft -> 0.85f
        LightingStyle.Bright -> 1.15f
        LightingStyle.Dramatic -> 1f
        LightingStyle.Fading -> 0.7f + 0.3f * ((sin(timeMs / 1400f) + 1f) / 2f)
    }
    val brightness = baseBrightness * (1f + bloom * 0.15f)

    Box(modifier = modifier.fillMaxSize()) {
        if (backgroundId != null) {
            Image(
                painter = painterResource(id = backgroundId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(brightness.coerceIn(0.6f, 1.2f))
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val energy = (animatedIntensity * 0.6f + currentLiveEnergy * 0.4f).coerceIn(0f, 1f)
            content(colors, energy, brightness, timeMs)
        }
    }
}

fun parseEnvColors(primaryHex: String, secondaryHexes: List<String>): List<Color> {
    val parsed = (listOf(primaryHex) + secondaryHexes).mapNotNull {
        runCatching { Color(it.toColorInt()) }.getOrNull()
    }
    return when {
        parsed.isEmpty() -> listOf(Color(0xFFA79AC7), Color(0xFFE9E4DE), Color(0xFF211E1B))
        parsed.size < 3 -> parsed + List(3 - parsed.size) { parsed.last() }
        else -> parsed
    }
}

fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t
)

/**
 * Procedural Helpers for Atmospheric Effects
 */

/**
 * Draws floating motes/pollen (match Heaven, Hopeful, Melancholic).
 */
fun DrawScope.drawFloatingMotes(count: Int, energy: Float, timeMs: Float, color: Color, brightness: Float) {
    for (i in 0 until count) {
        val phase = timeMs / (2000f + i * 300f)
        val x = size.width * ((i * 13) % 100 / 100f + sin(phase) * 0.05f)
        val y = size.height * ((i * 17) % 100 / 100f + cos(phase * 0.8f) * 0.1f)
        val alpha = (0.15f + energy * 0.3f) * ((sin(phase * 1.2f) + 1f) / 2f)
        drawCircle(
            color = color.copy(alpha = alpha * brightness),
            radius = 1.2.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

/**
 * Draws falling elements (leaves, petals).
 */
fun DrawScope.drawFallingElements(
    count: Int,
    energy: Float,
    timeMs: Float,
    color: Color,
    brightness: Float,
    isLeaf: Boolean = false
) {
    for (i in 0 until count) {
        val speed = 6000f - energy * 2500f
        val phase = timeMs / speed + i * 0.2f
        val loop = phase % 1.2f - 0.1f
        val x = size.width * (((i * 23) % 100) / 100f + sin(phase * 5f) * 0.06f)
        val y = size.height * loop
        val alpha = (0.8f - loop.coerceIn(0f, 1f) * 0.4f) * brightness
        
        rotate(degrees = phase * 180f, pivot = Offset(x, y)) {
            if (isLeaf) {
                // Leaf shape (pointed oval)
                val path = Path().apply {
                    moveTo(x, y - 6.dp.toPx())
                    quadraticTo(x + 4.dp.toPx(), y, x, y + 6.dp.toPx())
                    quadraticTo(x - 4.dp.toPx(), y, x, y - 6.dp.toPx())
                }
                drawPath(path, color = color.copy(alpha = alpha.coerceIn(0f, 1f)))
            } else {
                // Petal shape (softer oval)
                drawOval(
                    color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(x - 5.dp.toPx(), y - 3.dp.toPx()),
                    size = Size(10.dp.toPx(), 6.dp.toPx())
                )
            }
        }
    }
}

/**
 * Draws the consistent pale stone shelf for the cat.
 * Now aligned closer to the center/bottom as per user request.
 */
fun DrawScope.drawRestingLedge() {
    val widthFrac = 0.52f
    val yFrac = 0.82f 
    val w = size.width * widthFrac
    val h = w * 0.28f
    val topLeft = Offset(size.width * (0.5f - widthFrac / 2f), size.height * yFrac)
    
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFFF3F0EC), Color(0xFFD8D2C9))),
        topLeft = topLeft,
        size = Size(w, h)
    )
    
    val detailColor = Color.Black.copy(alpha = 0.12f)
    val path = Path().apply {
        moveTo(topLeft.x + w * 0.15f, topLeft.y + h * 0.4f)
        quadraticTo(topLeft.x + w * 0.3f, topLeft.y + h * 0.7f, topLeft.x + w * 0.25f, topLeft.y + h * 0.9f)
        moveTo(topLeft.x + w * 0.8f, topLeft.y + h * 0.35f)
        quadraticTo(topLeft.x + w * 0.7f, topLeft.y + h * 0.6f, topLeft.x + w * 0.75f, topLeft.y + h * 0.85f)
    }
    drawPath(path, color = detailColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
}

fun DrawScope.drawLightRays(
    center: Offset,
    rayCount: Int,
    angleOffset: Float,
    rayLength: Float,
    rayWidth: Float,
    color: Color
) {
    rotate(degrees = angleOffset, pivot = center) {
        for (i in 0 until rayCount) {
            rotate(degrees = 360f / rayCount * i, pivot = center) {
                val path = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x - rayWidth / 2, center.y - rayLength)
                    lineTo(center.x + rayWidth / 2, center.y - rayLength)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        listOf(color, Color.Transparent),
                        startY = center.y,
                        endY = center.y - rayLength
                    )
                )
            }
        }
    }
}

fun DrawScope.drawHexagon(center: Offset, radius: Float, angle: Float, color: Color, strokeWidth: Float) {
    val path = Path()
    for (i in 0..6) {
        val a = angle + (i * PI.toFloat() / 3f)
        val x = center.x + cos(a) * radius
        val y = center.y + sin(a) * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, color = color, style = Stroke(width = strokeWidth))
}
