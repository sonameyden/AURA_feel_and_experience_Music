package com.aura.feature.nowplaying.visuals.environments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

    Canvas(modifier = modifier.fillMaxSize()) {
        val energy = (animatedIntensity * 0.6f + currentLiveEnergy * 0.4f).coerceIn(0f, 1f)
        content(colors, energy, brightness, timeMs)
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
 * Advanced Painterly Helpers
 */

/**
 * Draws soft, organic rolling hills using Bezier curves (match Nature/Hopeful boards).
 */
fun DrawScope.drawRollingHills(
    yBase: Float,
    amplitude: Float,
    color: Color,
    brightness: Float,
    timeOffset: Float = 0f
) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, yBase)
        
        val segments = 3
        val segmentWidth = size.width / segments
        for (i in 0 until segments) {
            val startX = i * segmentWidth
            val endX = (i + 1) * segmentWidth
            val midX = startX + segmentWidth / 2f
            
            // Subtle wave motion if needed
            val hStart = (sin((startX / size.width) * 2 * PI + timeOffset) * amplitude).toFloat()
            val hEnd = (sin((endX / size.width) * 2 * PI + timeOffset) * amplitude).toFloat()
            
            cubicTo(
                midX - segmentWidth * 0.3f, yBase + hStart - amplitude * 0.8f,
                midX + segmentWidth * 0.3f, yBase + hEnd - amplitude * 0.8f,
                endX, yBase + hEnd
            )
        }
        
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color = color.copy(alpha = color.alpha * brightness))
}

/**
 * Draws a fluffy, volumetric cloud with inner shading.
 */
fun DrawScope.drawVolumetricCloud(center: Offset, scale: Float, color: Color, brightness: Float) {
    val r = 30f * scale
    val cloudColor = color.copy(alpha = color.alpha * brightness)
    val shadowColor = Color.Black.copy(alpha = 0.05f * brightness)

    // Base circles
    listOf(
        Offset(-r * 1.2f, r * 0.2f) to 0.8f,
        Offset(-r * 0.6f, -r * 0.4f) to 1.1f,
        Offset(r * 0.2f, -r * 0.6f) to 1.3f,
        Offset(r * 1.0f, -r * 0.2f) to 1.0f,
        Offset(r * 0.4f, r * 0.3f) to 0.9f
    ).forEach { (off, s) ->
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to cloudColor,
                0.8f to cloudColor,
                1.0f to Color.Transparent,
                center = center + off,
                radius = r * s
            ),
            radius = r * s,
            center = center + off
        )
        // Add subtle bottom shadow for volume
        drawCircle(
            color = shadowColor,
            radius = r * s * 0.8f,
            center = center + off + Offset(0f, r * 0.2f)
        )
    }
}

/**
 * Draws the consistent pale stone shelf for the cat.
 * Matches Layer 5 (Ground) in images.
 */
fun DrawScope.drawRestingLedge() {
    val widthFrac = 0.55f
    val yFrac = 0.76f // Adjusted to sit better with cat's 180dp bottom padding
    val w = size.width * widthFrac
    val h = w * 0.32f
    val topLeft = Offset(size.width * (0.5f - widthFrac / 2f), size.height * yFrac)
    
    // Base stone shape
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFE9E4DE), Color(0xFFD8D2C9))
        ),
        topLeft = topLeft,
        size = Size(w, h)
    )
    
    // "Root/Detail" lines from Layer 5
    val path = Path().apply {
        moveTo(topLeft.x + w * 0.1f, topLeft.y + h * 0.5f)
        quadraticTo(topLeft.x + w * 0.3f, topLeft.y + h * 0.8f, topLeft.x + w * 0.2f, topLeft.y + h)
        moveTo(topLeft.x + w * 0.8f, topLeft.y + h * 0.4f)
        quadraticTo(topLeft.x + w * 0.7f, topLeft.y + h * 0.7f, topLeft.x + w * 0.9f, topLeft.y + h)
    }
    drawPath(path, color = Color.Black.copy(alpha = 0.15f), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
}

/**
 * Draws overhanging forest branches for framing (match Melancholic/Nature).
 */
fun DrawScope.drawForestFraming(color: Color, brightness: Float) {
    val framingColor = color.copy(alpha = 0.9f * brightness)
    val path = Path().apply {
        // Top right branch
        moveTo(size.width, 0f)
        lineTo(size.width * 0.7f, 0f)
        cubicTo(size.width * 0.8f, size.height * 0.1f, size.width * 0.9f, size.height * 0.2f, size.width, size.height * 0.15f)
        close()
    }
    drawPath(path, color = framingColor)
    
    // Draw some leaf clusters
    listOf(Offset(size.width * 0.75f, size.height * 0.08f), Offset(size.width * 0.85f, size.height * 0.15f)).forEach { pos ->
        drawOval(color = framingColor, topLeft = pos, size = Size(40.dp.toPx(), 24.dp.toPx()))
    }
}

fun DrawScope.drawTree(base: Offset, scale: Float, color: Color) {
    drawLine(
        color = Color(0xFF423D33), // Darker trunk
        start = base,
        end = Offset(base.x, base.y - 45f * scale),
        strokeWidth = 6f * scale,
        cap = StrokeCap.Round
    )
    // Detailed canopy
    drawDetailedCanopy(Offset(base.x, base.y - 60f * scale), scale, color)
}

private fun DrawScope.drawDetailedCanopy(center: Offset, scale: Float, color: Color) {
    val r = 25f * scale
    listOf(Offset(0f, 0f), Offset(-r*0.6f, r*0.3f), Offset(r*0.6f, r*0.3f), Offset(0f, -r*0.5f)).forEach { off ->
        drawCircle(color = color.copy(alpha = 0.95f), radius = r, center = center + off)
    }
}

fun DrawScope.drawFlower(center: Offset, r: Float, petalColor: Color, coreColor: Color) {
    for (p in 0 until 5) {
        rotate(degrees = 72f * p, pivot = center) {
            drawOval(
                color = petalColor.copy(alpha = 0.85f),
                topLeft = Offset(center.x - r * 0.4f, center.y - r),
                size = Size(r * 0.8f, r * 1.1f)
            )
        }
    }
    drawCircle(color = coreColor, radius = r * 0.35f, center = center)
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
