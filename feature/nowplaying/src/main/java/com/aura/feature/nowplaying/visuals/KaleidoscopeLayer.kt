package com.aura.feature.nowplaying.visuals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.aura.core.model.KaleidoscopeStyle
import com.aura.core.model.VisualIntensity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameMillis

/**
 * A layered, mirrored, audio-reactive kaleidoscope/mandala — the Now Playing
 * screen's signature visual. v3 adds three things on top of the previous
 * (bug-fixed) version, all driven by the interactive preview you approved:
 *
 * 1. PER-RING MOTIF VARIETY — each style now cycles through a short sequence
 *    of motif shapes by ring depth (e.g. Dream: petal -> flower -> shard)
 *    instead of repeating one shape at shrinking sizes. This is what turns
 *    "pretty" into "intricate".
 * 2. SONG-SECTION AWARENESS — `progress` (0f.1f, song position) drives a
 *    complexity curve: sparse on intro/outro, builds through verse and
 *    pre-chorus, peaks at chorus, dips on bridge. Ring count and energy are
 *    both modulated by this curve, on top of (not instead of) live audio.
 * 3. PARTICLE-CORE LIGHT + SPECULAR SPARKLES — particles brighten as they
 *    pass near the glowing core, and rare small cross-shaped highlights spawn
 *    along the outer ring's motif tips and fade out, mimicking light
 *    catching an edge.
 *
 * All fixes from the previous pass are preserved: rotation is accumulated
 * per-frame (not `elapsedTime * speed`), raw amplitude is smoothed locally
 * with fast-attack/slow-release before touching anything visual, breathing
 * is a genuinely subtle few-percent effect, and the vignette extends past
 * the screen diagonal with a low peak alpha so it never reads as a circle.
 */
@Composable
fun KaleidoscopeLayer(
    style: KaleidoscopeStyle,
    baseEnergy: Float,          // AtmosphereProfile.energy — morphs on song change
    liveAmplitude: Float,       // AudioAnalyzer.amplitude — raw at the source, smoothed here
    valence: Float,             // AtmosphereProfile.valence — morphs on song change
    beatPulse: Boolean,
    primaryColorHex: String,
    secondaryColorHexes: List<String>,
    modifier: Modifier = Modifier,
    progress: Float = 0f,       // 0f..1f — song position, drives section-aware complexity
    reducedMotion: Boolean = false,
    visualIntensity: VisualIntensity = VisualIntensity.Medium
) {
    var prevStyle by remember { mutableStateOf(style) }
    var currentStyle by remember { mutableStateOf(style) }
    var trigger by remember { mutableStateOf(0) }

    LaunchedEffect(style) {
        if (style != currentStyle) {
            prevStyle = currentStyle
            currentStyle = style
            trigger++
        }
    }

    val transitionProgress by animateFloatAsState(
        targetValue = trigger.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "styleMorph"
    )
    val morphFactor = (transitionProgress - trigger + 1).coerceIn(0f, 1f)

    val animatedBaseEnergy by animateFloatAsState(
        targetValue = baseEnergy,
        animationSpec = tween(durationMillis = 1500),
        label = "baseEnergy"
    )
    val animatedValence by animateFloatAsState(
        targetValue = valence,
        animationSpec = tween(durationMillis = 1500),
        label = "valence"
    )

    val baseColors = remember(primaryColorHex, secondaryColorHexes) {
        buildColorPalette(primaryColorHex, secondaryColorHexes)
    }
    val colors = baseColors.mapIndexed { index, color ->
        animateColorAsState(
            targetValue = color,
            animationSpec = tween(durationMillis = 1500),
            label = "color_$index"
        ).value
    }

    val tier = visualIntensity.toTierConfig()
    val particles = remember(visualIntensity) {
        List(tier.particleCount) {
            KParticle(
                angle = Random.nextFloat() * (2 * PI.toFloat()),
                baseRadiusDp = 50f + Random.nextFloat() * 210f,
                speed = 0.1f + Random.nextFloat() * 0.3f,
                sizeDp = 0.8f + Random.nextFloat() * 1.4f,
                phase = Random.nextFloat() * (2 * PI.toFloat())
            )
        }
    }

    val sparkles = remember { mutableStateListOf<Sparkle>() }

    var timeMs by remember { mutableFloatStateOf(0f) }
    var bloom by remember { mutableFloatStateOf(0f) }
    var bloomTarget by remember { mutableFloatStateOf(0f) }
    var outerAngle by remember { mutableFloatStateOf(0f) }
    var midAngle by remember { mutableFloatStateOf(0f) }
    var smoothedAmplitude by remember { mutableFloatStateOf(0f) }

    val currentBeatPulse by rememberUpdatedState(beatPulse)
    val currentReducedMotion by rememberUpdatedState(reducedMotion)
    val currentLiveAmplitude by rememberUpdatedState(liveAmplitude)
    val currentBaseEnergy by rememberUpdatedState(animatedBaseEnergy)
    val currentProgress by rememberUpdatedState(progress)

    LaunchedEffect(Unit) {
        var lastFrame = -1L
        while (isActive) {
            withFrameMillis { frameTimeMs ->
                val dtMs = if (lastFrame >= 0) (frameTimeMs - lastFrame).toFloat() else 0f
                lastFrame = frameTimeMs
                timeMs += dtMs

                val attackRate = 0.5f
                val releaseRate = 0.08f
                val rate = if (currentLiveAmplitude > smoothedAmplitude) attackRate else releaseRate
                smoothedAmplitude += (currentLiveAmplitude - smoothedAmplitude) * rate

                if (currentBeatPulse) bloomTarget = 1f
                bloom += (bloomTarget - bloom) * (if (bloomTarget > bloom) 0.3f else 0.05f)
                bloomTarget *= if (currentReducedMotion) 0.85f else 0.96f

                val motionMul = if (currentReducedMotion) 0.12f else 1f
                val sectionMul = sectionComplexity(currentProgress)
                val totalEnergyForMotion = (currentBaseEnergy + smoothedAmplitude).coerceIn(0f, 1.2f)
                val effectiveEnergyForMotion = (totalEnergyForMotion * (0.5f + sectionMul * 0.6f)).coerceIn(0f, 1.2f)
                val outerSpeed = (0.00006f + effectiveEnergyForMotion * 0.0012f) * motionMul
                val midSpeed = (0.0001f + effectiveEnergyForMotion * 0.0018f) * motionMul
                outerAngle += dtMs * outerSpeed
                midAngle -= dtMs * midSpeed

                for (i in sparkles.indices.reversed()) {
                    sparkles[i].life -= 0.03f
                    if (sparkles[i].life <= 0f) sparkles.removeAt(i)
                }
            }
        }
    }

    val bloomEased = easeOutCubic(bloom.coerceIn(0f, 1f))
    val motionMul = if (reducedMotion) 0.12f else 1f
    val sectionMul = sectionComplexity(progress)
    val totalEnergy = (animatedBaseEnergy + smoothedAmplitude).coerceIn(0f, 1.2f)
    val effectiveEnergy = (totalEnergy * (0.5f + sectionMul * 0.6f)).coerceIn(0f, 1.2f)

    val activeRings = (1 + sectionMul * (tier.rings - 1)).roundToInt().coerceIn(1, tier.rings)

    val warm = animatedValence > 0.5f
    val warmAmt = abs(animatedValence - 0.5f) * 2f
    val auraColor = if (warm) Color(0xFFF2C48A) else Color(0xFF7A8CD8)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f - 10.dp.toPx()

        val breathe = 1f + sin(timeMs * 0.0011f) * 0.02f + bloomEased * 0.06f + smoothedAmplitude * 0.05f
        val outerRot = outerAngle
        val midRot = midAngle

        val auraRadiusPx = (100f + effectiveEnergy * 70f + bloomEased * 30f).dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.14f + warmAmt * 0.08f + bloomEased * 0.12f),
                    auraColor.copy(alpha = 0f)
                ),
                center = Offset(cx, cy),
                radius = auraRadiusPx
            ),
            radius = auraRadiusPx,
            center = Offset(cx, cy)
        )

        translate(left = cx, top = cy) {
            scale(scale = breathe, pivot = Offset.Zero) {

                if (morphFactor < 1f) {
                    drawMandala(
                        prevStyle, colors, activeRings, outerRot, effectiveEnergy,
                        bloomEased, 1f - morphFactor, sparkles
                    )
                }
                drawMandala(
                    currentStyle, colors, activeRings, outerRot, effectiveEnergy,
                    bloomEased, morphFactor, sparkles
                )

                rotate(degrees = degrees(midRot), pivot = Offset.Zero) {
                    val geomSymmetry = if (morphFactor < 0.5f) prevStyle.symmetryCount() else currentStyle.symmetryCount()
                    val geomCount = geomSymmetry * (if (activeRings > 2) 2 else 1)
                    for (s in 0 until geomCount) {
                        rotate(degrees = 360f / geomCount * s, pivot = Offset.Zero) {
                            val color = colors[(s + 1) % colors.size]
                            drawSecondaryGeom(
                                color = color,
                                radiusPx = (48f + effectiveEnergy * 26f).dp.toPx(),
                                sizePx = (6f + effectiveEnergy * 3f).dp.toPx(),
                                alpha = (0.26f + sectionMul * 0.14f) * morphFactor
                            )
                        }
                    }
                }

                val coreRadiusPx = (14f + effectiveEnergy * 10f + bloomEased * 10f).dp.toPx()
                val coreColor = colors[0]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = 0.85f + bloomEased * 0.15f),
                            colors.getOrElse(1) { coreColor }.copy(alpha = 0.35f),
                            coreColor.copy(alpha = 0f)
                        ),
                        center = Offset.Zero,
                        radius = coreRadiusPx
                    ),
                    radius = coreRadiusPx,
                    center = Offset.Zero
                )

                particles.forEachIndexed { i, p ->
                    val outward = bloomEased * 14f
                    val rDp = p.baseRadiusDp + sin(timeMs * 0.01f * p.speed + p.phase) * 7f + outward
                    val rPx = rDp.dp.toPx()
                    val angle = p.angle + timeMs * 0.0005f * p.speed * motionMul
                    val px = cos(angle) * rPx
                    val py = sin(angle) * rPx
                    val col = colors[i % colors.size]
                    val edgeFalloff = (1f - max(0f, (rDp - 200f) / 100f)).coerceIn(0f, 1f)
                    val coreLight = max(0f, 1f - rDp / 90f) * 0.4f
                    val alpha = max(
                        0f,
                        (0.2f + sin(timeMs * 0.02f + p.phase) * 0.12f + bloomEased * 0.2f + coreLight) * edgeFalloff
                    )
                    drawCircle(
                        color = col.copy(alpha = alpha),
                        radius = (p.sizeDp + coreLight * 1.2f).dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                sparkles.forEach { sp ->
                    drawSpecular(sp.x, sp.y, 4f.dp.toPx(), sp.life * 0.8f)
                }
            }
        }

        val vignetteRadius = kotlin.math.hypot(size.width, size.height) * 0.75f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    0.8f to Color.Black.copy(alpha = 0.08f),
                    1f to Color.Black.copy(alpha = 0.16f)
                ),
                center = Offset(cx, cy),
                radius = vignetteRadius
            ),
            radius = vignetteRadius,
            center = Offset(cx, cy)
        )
    }
}

/**
 * Song-position -> complexity curve. Sparse on intro/outro, builds through
 * verse/pre-chorus, peaks at chorus, dips on bridge. Approximated from
 * `progress` alone since real per-song section timestamps
 * (AtmosphereProfile.sectionProfiles) aren't populated by the backend yet —
 * swap this for real section lookups once that's wired up.
 */
private fun sectionComplexity(progress: Float): Float {
    val pos = progress.coerceIn(0f, 1f) * 100f
    return when {
        pos < 12f -> 0.35f + (pos / 12f) * 0.15f
        pos < 35f -> 0.5f + ((pos - 12f) / 23f) * 0.15f
        pos < 55f -> 0.65f + ((pos - 35f) / 20f) * 0.2f
        pos < 80f -> 1.0f
        pos < 92f -> 0.55f
        else -> (0.4f - ((pos - 92f) / 8f) * 0.15f).coerceAtLeast(0.2f)
    }
}

private fun DrawScope.drawMandala(
    style: KaleidoscopeStyle,
    colors: List<Color>,
    rings: Int,
    baseRot: Float,
    energy: Float,
    bloom: Float,
    alpha: Float,
    sparkles: androidx.compose.runtime.snapshots.SnapshotStateList<Sparkle>
) {
    if (alpha <= 0f) return
    val motifSequence = style.motifSequence()
    val symmetry = style.symmetryCount()

    for (layer in 0 until rings) {
        val motif = motifSequence[layer % motifSequence.size]
        val layerLenPx = (60f + layer * (38f + energy * 12f) + bloom * (8f + layer * 3f)).dp.toPx()
        val layerRot = baseRot * (1f + layer * 0.16f)
        val edgeFade = if (layer == rings - 1) 0.55f else 1f

        rotate(degrees = degrees(layerRot), pivot = Offset.Zero) {
            for (s in 0 until symmetry) {
                rotate(degrees = 360f / symmetry * s, pivot = Offset.Zero) {
                    val color = colors[layer % colors.size]
                    val highlight = colors[(layer + 1) % colors.size]
                    val widthPx = (14f + energy * 9f - layer * 1.2f).dp.toPx()

                    drawMotif(motif, color, highlight, layerLenPx, widthPx, bloom, edgeFade * alpha)
                    scale(scaleX = -1f, scaleY = 1f, pivot = Offset.Zero) {
                        drawMotif(motif, color, highlight, layerLenPx * 0.9f, widthPx * 0.85f, bloom, edgeFade * alpha)
                    }

                    if (layer == rings - 1 && sparkles.size < 14 && Random.nextFloat() < 0.0025f) {
                        val worldAngle = layerRot + (2 * PI.toFloat() / symmetry) * s
                        sparkles.add(
                            Sparkle(
                                x = sin(-worldAngle) * layerLenPx,
                                y = -cos(worldAngle) * layerLenPx,
                                life = 1f
                            )
                        )
                    }
                }
            }
        }
    }
}

// ---- Motif shapes & helpers ----

private enum class MotifKind { Petal, Leaf, Wave, Shard, RibbonLong, Flower }

private class Sparkle(val x: Float, val y: Float, var life: Float)

/**
 * Each style now cycles through a short SEQUENCE of shapes by ring depth
 * instead of repeating one shape — the "per-ring motif variety" upgrade.
 * Outer rings get the style's primary/largest-feeling shape, inner rings
 * pick up finer detail shapes.
 */
private fun KaleidoscopeStyle.motifSequence(): List<MotifKind> = when (this) {
    KaleidoscopeStyle.SoftOrganic -> listOf(MotifKind.Petal, MotifKind.Leaf, MotifKind.Shard)
    KaleidoscopeStyle.FluidSymmetry -> listOf(MotifKind.Petal, MotifKind.Flower, MotifKind.Shard)
    KaleidoscopeStyle.WaveLike -> listOf(MotifKind.Wave, MotifKind.Leaf, MotifKind.Shard)
    KaleidoscopeStyle.SharpGeometric -> listOf(MotifKind.Shard, MotifKind.Flower, MotifKind.Leaf)
    KaleidoscopeStyle.DarkReflective -> listOf(MotifKind.RibbonLong, MotifKind.Leaf, MotifKind.Shard)
    KaleidoscopeStyle.Floral -> listOf(MotifKind.Flower, MotifKind.Petal, MotifKind.Shard)
}

private fun KaleidoscopeStyle.symmetryCount(): Int = when (this) {
    KaleidoscopeStyle.SoftOrganic -> 8
    KaleidoscopeStyle.FluidSymmetry -> 11
    KaleidoscopeStyle.WaveLike -> 9
    KaleidoscopeStyle.SharpGeometric -> 14
    KaleidoscopeStyle.DarkReflective -> 7
    KaleidoscopeStyle.Floral -> 11
}

private data class TierConfig(val rings: Int, val particleCount: Int, val glow: Float)

private fun VisualIntensity.toTierConfig(): TierConfig = when (this) {
    VisualIntensity.Low -> TierConfig(rings = 2, particleCount = 16, glow = 0.45f)
    VisualIntensity.Medium -> TierConfig(rings = 3, particleCount = 28, glow = 0.7f)
    VisualIntensity.High -> TierConfig(rings = 4, particleCount = 44, glow = 1.0f)
}

private data class KParticle(
    val angle: Float,
    val baseRadiusDp: Float,
    val speed: Float,
    val sizeDp: Float,
    val phase: Float
)

private fun DrawScope.drawSpecular(x: Float, y: Float, size: Float, alpha: Float) {
    if (alpha <= 0f) return
    drawLine(
        color = Color.White.copy(alpha = alpha * 0.9f),
        start = Offset(x - size, y),
        end = Offset(x + size, y),
        strokeWidth = 0.8f.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = alpha * 0.9f),
        start = Offset(x, y - size),
        end = Offset(x, y + size),
        strokeWidth = 0.8f.dp.toPx()
    )
}

private fun DrawScope.drawMotif(
    kind: MotifKind,
    color: Color,
    highlightColor: Color,
    lengthPx: Float,
    widthPx: Float,
    bloomEased: Float,
    alpha: Float
) {
    if (alpha <= 0f) return
    val w = widthPx * (1f + bloomEased * 0.08f)
    val tip = Offset(0f, -lengthPx)
    val bodyBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0f to color.copy(alpha = 0f),
            0.5f to color.copy(alpha = 0.5f * alpha),
            1f to color.copy(alpha = 0.85f * alpha)
        ),
        start = Offset.Zero,
        end = tip
    )

    val path = Path()
    when (kind) {
        MotifKind.Petal, MotifKind.Flower -> {
            path.moveTo(0f, 0f)
            path.cubicTo(w, -lengthPx * 0.35f, w * 0.7f, -lengthPx * 0.85f, 0f, -lengthPx)
            path.cubicTo(-w * 0.7f, -lengthPx * 0.85f, -w, -lengthPx * 0.35f, 0f, 0f)
            path.close()
            drawPath(path, brush = bodyBrush)

            if (kind == MotifKind.Flower) {
                drawOval(
                    color = highlightColor.copy(alpha = 0.35f * alpha),
                    topLeft = Offset(-w * 0.3f, -lengthPx * 0.7f),
                    size = Size(w * 0.6f, lengthPx * 0.4f)
                )
            }
        }
        MotifKind.Leaf -> {
            path.moveTo(0f, 0f)
            path.quadraticTo(w, -lengthPx * 0.5f, 0f, -lengthPx)
            path.quadraticTo(-w, -lengthPx * 0.5f, 0f, 0f)
            path.close()
            drawPath(path, brush = bodyBrush)
        }
        MotifKind.Shard -> {
            path.moveTo(0f, 0f)
            path.lineTo(w * 0.5f, -lengthPx * 0.6f)
            path.lineTo(0f, -lengthPx)
            path.lineTo(-w * 0.5f, -lengthPx * 0.6f)
            path.close()
            drawPath(path, brush = bodyBrush)
        }
        MotifKind.Wave -> {
            path.moveTo(-w, 0f)
            path.quadraticTo(0f, -lengthPx * (1f + bloomEased * 0.1f), w, 0f)
            drawPath(
                path,
                brush = bodyBrush,
                style = Stroke(width = w * 0.28f, cap = StrokeCap.Round),
                alpha = 0.7f * alpha
            )
        }
        MotifKind.RibbonLong -> {
            path.moveTo(0f, 0f)
            path.cubicTo(w * 1.4f, -lengthPx * 0.4f, -w * 1.2f, -lengthPx * 0.7f, 0f, -lengthPx)
            drawPath(
                path,
                brush = bodyBrush,
                style = Stroke(width = w * 0.22f),
                alpha = 0.62f * alpha
            )
        }
    }

    val highlightPath = Path()
    when (kind) {
        MotifKind.Petal, MotifKind.Flower -> {
            highlightPath.moveTo(0f, 0f)
            highlightPath.cubicTo(w, -lengthPx * 0.35f, w * 0.7f, -lengthPx * 0.85f, 0f, -lengthPx)
        }
        MotifKind.Shard -> {
            highlightPath.moveTo(0f, 0f)
            highlightPath.lineTo(w * 0.5f, -lengthPx * 0.6f)
            highlightPath.lineTo(0f, -lengthPx)
        }
        MotifKind.Leaf -> {
            highlightPath.moveTo(0f, 0f)
            highlightPath.quadraticTo(w, -lengthPx * 0.5f, 0f, -lengthPx)
        }
        else -> return
    }
    drawPath(
        highlightPath,
        color = highlightColor.copy(alpha = (0.28f + bloomEased * 0.3f) * alpha),
        style = Stroke(width = 0.7f.dp.toPx())
    )
}

private fun DrawScope.drawSecondaryGeom(color: Color, radiusPx: Float, sizePx: Float, alpha: Float) {
    if (alpha <= 0f) return
    val path = Path().apply {
        moveTo(0f, -radiusPx - sizePx)
        lineTo(sizePx * 0.7f, -radiusPx)
        lineTo(0f, -radiusPx + sizePx)
        lineTo(-sizePx * 0.7f, -radiusPx)
        close()
    }
    drawPath(path, color = color.copy(alpha = alpha), style = Stroke(width = 0.7f.dp.toPx()))
}

private fun buildColorPalette(primaryHex: String, secondaryHexes: List<String>): List<Color> {
    val parsed = (listOf(primaryHex) + secondaryHexes).mapNotNull {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    }
    return when {
        parsed.isEmpty() -> listOf(Color(0xFFA79AC7), Color(0xFFE9E4DE), Color(0xFFA79AC7))
        parsed.size < 3 -> parsed + List(3 - parsed.size) { parsed.first() }
        else -> parsed
    }
}

private fun degrees(radians: Float): Float = radians * (180f / PI.toFloat())

private fun easeOutCubic(x: Float): Float {
    val inv = 1f - x
    return 1f - inv * inv * inv
}