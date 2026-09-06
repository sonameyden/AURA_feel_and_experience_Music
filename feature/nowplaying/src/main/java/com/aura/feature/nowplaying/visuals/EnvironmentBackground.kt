package com.aura.feature.nowplaying.visuals

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
import androidx.core.graphics.toColorInt
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.EnvironmentType
import com.aura.core.model.LightingStyle
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The illustrated "scenery" layer — entirely hand-drawn on a Compose Canvas.
 *
 * Replaces the earlier Rive-based implementation: all 8 environments from
 * Section 8 of the project spec (Heaven, Nature, Ocean, Dream, Romantic,
 * Melancholic, Hopeful, Energetic) are generated procedurally from
 * AtmosphereProfile's colors + energy, so there's no `.riv` asset pipeline,
 * no `rive-android` dependency, and no state-machine input names to keep in
 * sync with an external design tool. `energy`/`intensity` drive the visuals
 * exactly the way they drove the old Rive state-machine inputs — just as
 * plain function parameters now.
 */
@Composable
fun EnvironmentBackground(
    profile: AtmosphereProfile,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier
) {
    val colors = remember(profile.primaryColorHex, profile.secondaryColorHexes) {
        parseEnvColors(profile.primaryColorHex, profile.secondaryColorHexes)
    }

    // Smoothly morphs when the song (and therefore the environment/energy)
    // changes, instead of hard-cutting — mirrors ReactiveGradientLayer's
    // cross-fade behavior for the scenery layer itself.
    val animatedIntensity by animateFloatAsState(
        targetValue = profile.energy,
        animationSpec = tween(durationMillis = 2000),
        label = "env_intensity"
    )

    var timeMs by remember { mutableFloatStateOf(0f) }
    val currentEnergy by rememberUpdatedState(liveAudioEnergy)

    LaunchedEffect(Unit) {
        var last = -1L
        while (isActive) {
            withFrameMillis { frameTime ->
                val dt = if (last >= 0) (frameTime - last).toFloat() else 0f
                last = frameTime
                timeMs += dt
            }
        }
    }

    val brightnessMultiplier = when (profile.lightingStyle) {
        LightingStyle.Soft -> 0.85f
        LightingStyle.Bright -> 1.15f
        LightingStyle.Dramatic -> 1f
        LightingStyle.Fading -> 0.7f + 0.3f * ((sin(timeMs / 1400f) + 1f) / 2f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val effectiveEnergy = (animatedIntensity * 0.6f + currentEnergy * 0.4f).coerceIn(0f, 1f)
        drawEnvironment(
            environment = profile.environment,
            colors = colors,
            energy = effectiveEnergy,
            brightness = brightnessMultiplier,
            timeMs = timeMs
        )
    }
}

private fun parseEnvColors(primaryHex: String, secondaryHexes: List<String>): List<Color> {
    val parsed = (listOf(primaryHex) + secondaryHexes).mapNotNull {
        runCatching { Color(it.toColorInt()) }.getOrNull()
    }
    return when {
        parsed.isEmpty() -> listOf(Color(0xFFA79AC7), Color(0xFFE9E4DE), Color(0xFF211E1B))
        parsed.size < 3 -> parsed + List(3 - parsed.size) { parsed.last() }
        else -> parsed
    }
}

private fun DrawScope.drawEnvironment(
    environment: EnvironmentType,
    colors: List<Color>,
    energy: Float,
    brightness: Float,
    timeMs: Float
) {
    val primary = colors[0]
    val secondary = colors.getOrElse(1) { primary }
    val tertiary = colors.getOrElse(2) { secondary }

    // Shared base wash under every environment — this is what the old
    // Rive fallback flat-color box becomes: a base layer instead of a
    // last-resort replacement, so there's never a blank frame.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                primary.copy(alpha = 0.55f * brightness),
                tertiary.copy(alpha = 0.85f * brightness)
            )
        )
    )

    when (environment) {
        EnvironmentType.Heaven -> drawHeaven(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Nature -> drawNature(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Ocean -> drawOcean(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Dream -> drawDream(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Romantic -> drawRomantic(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Melancholic -> drawMelancholic(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Hopeful -> drawHopeful(primary, secondary, tertiary, energy, brightness, timeMs)
        EnvironmentType.Energetic -> drawEnergetic(primary, secondary, tertiary, energy, brightness, timeMs)
    }
}

// ---------- Heaven: drifting clouds + soft radiating light rays ----------
private fun DrawScope.drawHeaven(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    val rayCount = 6
    rotate(degrees = (timeMs / 800f) % 360f) {
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
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    val r = 22f * scale
    listOf(-1.4f, -0.6f, 0.3f, 1.1f).forEachIndexed { i, dx ->
        drawCircle(color = color, radius = r * (0.7f + (i % 2) * 0.3f), center = Offset(center.x + dx * r, center.y))
    }
}

// ---------- Nature: rolling hills + swaying trees ----------
private fun DrawScope.drawNature(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFFE9B0).copy(alpha = 0.5f * brightness), Color.Transparent)),
        radius = size.minDimension * 0.3f,
        center = Offset(size.width * 0.8f, size.height * 0.2f)
    )
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
    val sway = sin(timeMs / 900f) * (2f + energy * 6f)
    listOf(0.2f, 0.35f, 0.82f).forEachIndexed { i, xFrac ->
        rotate(degrees = sway * (if (i % 2 == 0) 1f else -1f), pivot = Offset(size.width * xFrac, size.height * 0.78f)) {
            drawTree(Offset(size.width * xFrac, size.height * 0.78f), 0.7f + i * 0.15f, secondary)
        }
    }
}

private fun DrawScope.drawTree(base: Offset, scale: Float, color: Color) {
    drawLine(
        color = Color(0xFF6B4A34),
        start = base,
        end = Offset(base.x, base.y - 30f * scale),
        strokeWidth = 5f * scale,
        cap = StrokeCap.Round
    )
    drawCircle(color = color.copy(alpha = 0.9f), radius = 20f * scale, center = Offset(base.x, base.y - 42f * scale))
}

// ---------- Ocean: layered animated waves ----------
private fun DrawScope.drawOcean(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    drawRect(color = tertiary.copy(alpha = 0.4f * brightness), topLeft = Offset.Zero, size = Size(size.width, size.height * 0.5f))
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
    for (i in 0 until 10) {
        val sx = Random(i).nextFloat()
        val sparkleY = size.height * (0.35f + 0.1f * (i % 3))
        val alpha = ((sin(timeMs / 500f + i) + 1f) / 2f) * 0.5f
        drawCircle(color = Color.White.copy(alpha = alpha), radius = 1.5f, center = Offset(size.width * sx, sparkleY))
    }
}

// ---------- Dream: floating islands + twinkling star field ----------
private fun DrawScope.drawDream(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
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
    listOf(0.25f to 0.6f, 0.65f to 0.75f, 0.85f to 0.5f).forEachIndexed { i, (xFrac, yFrac) ->
        val bob = sin(timeMs / 1600f + i * 2f) * 8f
        val center = Offset(size.width * xFrac, size.height * yFrac + bob)
        val islandWidth = size.width * (0.16f + i * 0.03f)
        drawOval(
            color = secondary.copy(alpha = 0.85f * brightness),
            topLeft = Offset(center.x - islandWidth / 2f, center.y),
            size = Size(islandWidth, islandWidth * 0.35f)
        )
        drawTree(Offset(center.x, center.y), 0.5f, tertiary)
    }
}

// ---------- Romantic: sunset glow + swaying flowers + drifting petals ----------
private fun DrawScope.drawRomantic(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    drawCircle(
        brush = Brush.radialGradient(listOf(secondary.copy(alpha = 0.6f * brightness), Color.Transparent)),
        radius = size.minDimension * 0.45f,
        center = Offset(size.width * 0.5f, size.height * 0.4f)
    )
    val sway = sin(timeMs / 1000f) * (3f + energy * 5f)
    for (i in 0 until 6) {
        val xFrac = 0.1f + i * 0.16f
        rotate(degrees = sway * (if (i % 2 == 0) 1f else -1f), pivot = Offset(size.width * xFrac, size.height * 0.92f)) {
            drawFlower(Offset(size.width * xFrac, size.height * 0.9f), 10f, primary, tertiary)
        }
    }
    for (i in 0 until 8) {
        val speed = 5000f + i * 400f
        val yFrac = ((timeMs / speed) + i * 0.2f) % 1f
        val xFrac = 0.1f + ((i * 53) % 80) / 100f + sin(timeMs / 900f + i) * 0.03f
        drawCircle(
            color = tertiary.copy(alpha = 0.5f * (1f - yFrac) * brightness),
            radius = 3f,
            center = Offset(size.width * xFrac, size.height * (1f - yFrac))
        )
    }
}

private fun DrawScope.drawFlower(center: Offset, r: Float, petalColor: Color, coreColor: Color) {
    for (p in 0 until 5) {
        rotate(degrees = 72f * p, pivot = center) {
            drawOval(
                color = petalColor.copy(alpha = 0.85f),
                topLeft = Offset(center.x - r * 0.35f, center.y - r),
                size = Size(r * 0.7f, r)
            )
        }
    }
    drawCircle(color = coreColor, radius = r * 0.3f, center = center)
}

// ---------- Melancholic: rain streaks behind a window frame ----------
private fun DrawScope.drawMelancholic(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    for (i in 0 until 26) {
        val seed = i * 71
        val xFrac = ((seed * 31) % 100) / 100f
        val speed = 700f - energy * 300f
        val fall = ((timeMs / speed) + (seed % 50) / 50f) % 1.2f
        val yStart = size.height * (fall - 0.2f)
        drawLine(
            color = secondary.copy(alpha = 0.35f * brightness),
            start = Offset(size.width * xFrac, yStart),
            end = Offset(size.width * xFrac - 6f, yStart + 26f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
    val frameColor = Color(0xFF1C1A22).copy(alpha = 0.4f * brightness)
    drawLine(frameColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = 4f)
    drawLine(frameColor, Offset(0f, size.height * 0.4f), Offset(size.width, size.height * 0.4f), strokeWidth = 4f)
    drawRect(color = frameColor, topLeft = Offset.Zero, size = size, style = Stroke(width = 6f))
}

// ---------- Hopeful: horizon that gradually brightens ----------
private fun DrawScope.drawHopeful(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    // Self-contained brightening loop. If you wire real playback progress
    // in later, replace `cycle` with the song's 0f..1f position — the rest
    // of this function already expects a 0f..1f value.
    val cycle = (timeMs / 12000f) % 1f
    val horizonY = size.height * 0.62f
    val glowColors = listOf(Color(0xFF9AC1E0), Color(0xFFF3C9A6), Color(0xFFE8B84B))
    val scaled = cycle * (glowColors.size - 1)
    val lowIndex = scaled.toInt().coerceIn(0, glowColors.size - 2)
    val blended = lerpColor(glowColors[lowIndex], glowColors[lowIndex + 1], scaled - lowIndex)

    drawCircle(
        brush = Brush.radialGradient(listOf(blended.copy(alpha = (0.4f + cycle * 0.4f) * brightness), Color.Transparent)),
        radius = size.minDimension * (0.35f + cycle * 0.25f),
        center = Offset(size.width * 0.5f, horizonY)
    )
    drawLine(
        color = blended.copy(alpha = 0.6f * brightness),
        start = Offset(0f, horizonY),
        end = Offset(size.width, horizonY),
        strokeWidth = 2f
    )
    for (i in 0 until 5) {
        val rayAlpha = (0.08f + cycle * 0.12f) * brightness
        drawLine(
            color = Color.White.copy(alpha = rayAlpha),
            start = Offset(size.width * 0.5f, horizonY),
            end = Offset(size.width * (0.2f + i * 0.15f), 0f),
            strokeWidth = 3f
        )
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t
)

// ---------- Energetic: rotating geometric light tunnel ----------
private fun DrawScope.drawEnergetic(
    primary: Color, secondary: Color, tertiary: Color, energy: Float, brightness: Float, timeMs: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val ringCount = 6
    val rotationSpeed = 4000f - energy * 2800f
    for (ring in 0 until ringCount) {
        val progress = ((timeMs / rotationSpeed) + ring.toFloat() / ringCount) % 1f
        val radius = progress * size.minDimension * 0.65f
        val alpha = ((1f - progress) * (0.5f + energy * 0.4f) * brightness).coerceIn(0f, 1f)
        val color = if (ring % 2 == 0) primary else secondary
        val sides = 6
        val path = Path()
        for (s in 0..sides) {
            val angle = (2 * PI.toFloat() / sides) * s + timeMs / 3000f
            val x = cx + cos(angle) * radius
            val y = cy + sin(angle) * radius
            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color.copy(alpha = alpha), style = Stroke(width = 3f))
    }
    drawCircle(
        brush = Brush.radialGradient(listOf(tertiary.copy(alpha = 0.7f * brightness), Color.Transparent)),
        radius = size.minDimension * (0.12f + energy * 0.08f),
        center = Offset(cx, cy)
    )
}
