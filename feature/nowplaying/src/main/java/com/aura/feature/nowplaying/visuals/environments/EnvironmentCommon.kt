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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.core.graphics.toColorInt
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.LightingStyle
import kotlinx.coroutines.isActive
import kotlin.math.sin

/**
 * Every environment file below is a stack of painted Canvas layers matching
 * the moodboard reference structure (sky -> distant silhouette -> midground
 * -> foreground detail -> resting ledge / optional particles). This wrapper
 * owns the one-time boilerplate every environment needs:
 *  - a frame ticker (timeMs) for all the drifting/swaying/rotating motion
 *  - profile.energy animated smoothly across song changes
 *  - blending that with the live AudioAnalyzer amplitude
 *  - a short "bloom" bump that brightens the whole scene right after a beat,
 *    so the background itself breathes with the music, not just the
 *    kaleidoscope layer on top of it
 *  - the LightingStyle -> brightness mapping every environment already used
 *
 * Each environment file only supplies its own `content` lambda: the actual
 * layer painting, using (colors, energy, brightness, timeMs).
 */
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

    // Smoothly morphs when the song (and therefore energy) changes, instead
    // of hard-cutting -- mirrors ReactiveGradientLayer's cross-fade.
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

                // Fast attack on a detected beat, slow release after --
                // gives the background a brief, visible "breath" per hit
                // rather than a flat brightness.
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
    val brightness = baseBrightness * (1f + bloom * 0.12f)

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

fun DrawScope.drawTree(base: Offset, scale: Float, color: Color) {
    drawLine(
        color = Color(0xFF6B4A34),
        start = base,
        end = Offset(base.x, base.y - 30f * scale),
        strokeWidth = 5f * scale,
        cap = StrokeCap.Round
    )
    drawCircle(color = color.copy(alpha = 0.9f), radius = 20f * scale, center = Offset(base.x, base.y - 42f * scale))
}

fun DrawScope.drawFlower(center: Offset, r: Float, petalColor: Color, coreColor: Color) {
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

fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    val r = 22f * scale
    listOf(-1.4f, -0.6f, 0.3f, 1.1f).forEachIndexed { i, dx ->
        drawCircle(color = color, radius = r * (0.7f + (i % 2) * 0.3f), center = Offset(center.x + dx * r, center.y))
    }
}

/**
 * The shared soft pale-stone "resting ledge" foreground platform that shows
 * up in every moodboard (the cat's resting spot). Drawn last, on top of each
 * environment's own foreground layer, so CatCompanion always has somewhere
 * to visually sit regardless of which environment is active.
 */
fun DrawScope.drawRestingLedge(widthFrac: Float = 0.42f, yFrac: Float = 0.86f) {
    val w = size.width * widthFrac
    val h = w * 0.22f
    val topLeft = Offset(size.width * (0.5f - widthFrac / 2f), size.height * yFrac)
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFFF3F0EC).copy(alpha = 0.9f), Color(0xFFD8D2C9).copy(alpha = 0.9f))),
        topLeft = topLeft,
        size = Size(w, h)
    )
}
