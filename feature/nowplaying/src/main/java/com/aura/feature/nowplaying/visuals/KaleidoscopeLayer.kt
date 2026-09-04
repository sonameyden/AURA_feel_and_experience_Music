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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.aura.core.model.KaleidoscopeStyle
import com.aura.core.model.VisualIntensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap

/**
 * A layered, mirrored, audio-reactive kaleidoscope/mandala — the signature
 * visual element of the Now Playing screen.
 *
 * MORPHING & REACTIVITY:
 * 1. Style Morphing: When the KaleidoscopeStyle changes (song change), we
 *    animate a `transitionProgress` factor and cross-fade between the old
 *    and new motifs/symmetry counts.
 * 2. Color Morphing: Palette colors animate smoothly to their new values.
 * 3. Responsive Energy: We split energy into `baseEnergy` (morphed from
 *    profile) and `liveAmplitude` (raw from AudioAnalyzer). Only the base
 *    morphes; the live part is instant for maximum "feeling" of the music.
 * 4. Beat Bloom: Onsets trigger a bloom that eases out, layered on top of
 *    the breathing animation.
 */
@Composable
fun KaleidoscopeLayer(
    style: KaleidoscopeStyle,
    baseEnergy: Float,          // AtmosphereProfile.energy — morphs on song change
    liveAmplitude: Float,       // AudioAnalyzer.amplitude — real-time, no smoothing
    valence: Float,             // AtmosphereProfile.valence — morphs on song change
    beatPulse: Boolean,
    primaryColorHex: String,
    secondaryColorHexes: List<String>,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    visualIntensity: VisualIntensity = VisualIntensity.Medium
) {
    // Morphing: cross-fade between styles when they change
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

    // 2. Animate morphing properties (AtmosphereProfile values)
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

    // 3. Color Morphing
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

    var timeMs by remember { mutableFloatStateOf(0f) }
    var bloom by remember { mutableFloatStateOf(0f) }
    var bloomTarget by remember { mutableFloatStateOf(0f) }

    val currentBeatPulse by rememberUpdatedState(beatPulse)
    val currentReducedMotion by rememberUpdatedState(reducedMotion)

    LaunchedEffect(Unit) {
        var lastFrame = -1L
        while (isActive) {
            withFrameMillis { frameTimeMs ->
                if (lastFrame >= 0) {
                    timeMs += (frameTimeMs - lastFrame).toFloat()
                }
                lastFrame = frameTimeMs

                if (currentBeatPulse) bloomTarget = 1f
                val rise = 0.5f
                val fall = 0.04f
                bloom += (bloomTarget - bloom) * (if (bloomTarget > bloom) rise else fall)
                bloomTarget *= if (currentReducedMotion) 0.85f else 0.96f
            }
        }
    }

    val bloomEased = easeOutCubic(bloom.coerceIn(0f, 1f))
    val motionMul = if (reducedMotion) 0.12f else 1f
    
    // Final energy is the morphed base + raw live amplitude
    val totalEnergy = (animatedBaseEnergy + liveAmplitude).coerceIn(0f, 1.2f)
    
    val warm = animatedValence > 0.5f
    val warmAmt = abs(animatedValence - 0.5f) * 2f
    val auraColor = if (warm) Color(0xFFF2C48A) else Color(0xFF7A8CD8)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f - 10.dp.toPx()

        // Breath and rotation respond to live energy for "feeling the music"
        val breathe = 1f + sin(timeMs * 0.0011f) * 0.02f + bloomEased * 0.35f + liveAmplitude * 0.25f
        val outerRot = timeMs * (0.00006f + totalEnergy * 0.0008f) * motionMul
        val midRot = -timeMs * (0.0001f + totalEnergy * 0.0012f) * motionMul

        // Layer 1: background aura
        val auraRadiusPx = (110f + totalEnergy * 110f + bloomEased * 100f).dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.14f + warmAmt * 0.08f + bloomEased * 0.3f + liveAmplitude * 0.25f),
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

                // Layer 2: primary organic motifs
                // Morphing: Draw BOTH styles if we're transitioning
                if (morphFactor < 1f) {
                    drawMandala(prevStyle, colors, tier, outerRot, totalEnergy, bloomEased, 1f - morphFactor)
                }
                drawMandala(currentStyle, colors, tier, outerRot, totalEnergy, bloomEased, morphFactor)

                // Layer 3: secondary geometric accents (morphed symmetry)
                rotate(degrees = degrees(midRot), pivot = Offset.Zero) {
                    val geomSymmetry = if (morphFactor < 0.5f) prevStyle.symmetryCount() else currentStyle.symmetryCount()
                    val geomCount = geomSymmetry * (if (tier.rings > 2) 2 else 1)
                    for (s in 0 until geomCount) {
                        rotate(degrees = 360f / geomCount * s, pivot = Offset.Zero) {
                            val color = colors[(s + 1) % colors.size]
                            drawSecondaryGeom(
                                color = color,
                                radiusPx = (52f + totalEnergy * 30f).dp.toPx(),
                                sizePx = (6f + totalEnergy * 4f).dp.toPx(),
                                alpha = (0.28f + tier.glow * 0.12f) * morphFactor
                            )
                        }
                    }
                }

                // Layer 4: Glowing core
                val coreRadiusPx = (15f + totalEnergy * 20f + bloomEased * 25f).dp.toPx()
                val coreColor = colors[0]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = 0.8f + bloomEased * 0.4f + liveAmplitude * 0.3f),
                            colors.getOrElse(1) { coreColor }.copy(alpha = 0.3f),
                            coreColor.copy(alpha = 0f)
                        ),
                        center = Offset.Zero,
                        radius = coreRadiusPx
                    ),
                    radius = coreRadiusPx,
                    center = Offset.Zero
                )

                // Layer 5: particles, reactive to beat and amplitude
                particles.forEachIndexed { i, p ->
                    val outward = bloomEased * 60f + liveAmplitude * 40f
                    val rDp = p.baseRadiusDp + sin(timeMs * 0.01f * p.speed + p.phase) * 10f + outward
                    val rPx = rDp.dp.toPx()
                    val angle = p.angle + timeMs * 0.0008f * p.speed * motionMul
                    val px = cos(angle) * rPx
                    val py = sin(angle) * rPx
                    val col = colors[i % colors.size]
                    val edgeFalloff = (1f - max(0f, (rDp - 200f) / 100f)).coerceIn(0f, 1f)
                    val alpha = max(
                        0f,
                        (0.25f + sin(timeMs * 0.02f + p.phase) * 0.15f + bloomEased * 0.4f + liveAmplitude * 0.3f) * edgeFalloff
                    )
                    drawCircle(
                        color = col.copy(alpha = alpha),
                        radius = p.sizeDp.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }
        
        // Layer 6: Vignette
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                center = Offset(cx, cy),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawMandala(
    style: KaleidoscopeStyle,
    colors: List<Color>,
    tier: TierConfig,
    baseRot: Float,
    energy: Float,
    bloom: Float,
    alpha: Float
) {
    if (alpha <= 0f) return
    val motif = style.toMotifKind()
    val symmetry = style.symmetryCount()

    for (layer in 0 until tier.rings) {
        val layerLenPx = (66f + layer * (40f + energy * 15f) + bloom * (10f + layer * 4f)).dp.toPx()
        val layerRot = baseRot * (1f + layer * 0.15f)
        val edgeFade = if (layer == tier.rings - 1) 0.5f else 1f

        rotate(degrees = degrees(layerRot), pivot = Offset.Zero) {
            for (s in 0 until symmetry) {
                rotate(degrees = 360f / symmetry * s, pivot = Offset.Zero) {
                    val color = colors[layer % colors.size]
                    val highlight = colors[(layer + 1) % colors.size]
                    val widthPx = (14f + energy * 10f - layer * 1.5f).dp.toPx()

                    drawMotif(motif, color, highlight, layerLenPx, widthPx, bloom, edgeFade * alpha)
                    scale(scaleX = -1f, scaleY = 1f, pivot = Offset.Zero) {
                        drawMotif(
                            motif, color, highlight,
                            layerLenPx * 0.9f, widthPx * 0.85f, bloom, edgeFade * alpha
                        )
                    }
                }
            }
        }
    }
}

// ---- Motif shapes & Helpers ----

private enum class MotifKind { Petal, Leaf, Wave, Shard, RibbonLong, Flower }

private fun KaleidoscopeStyle.toMotifKind(): MotifKind = when (this) {
    KaleidoscopeStyle.SoftOrganic -> MotifKind.Petal
    KaleidoscopeStyle.FluidSymmetry -> MotifKind.Petal
    KaleidoscopeStyle.WaveLike -> MotifKind.Wave
    KaleidoscopeStyle.SharpGeometric -> MotifKind.Shard
    KaleidoscopeStyle.DarkReflective -> MotifKind.RibbonLong
    KaleidoscopeStyle.Floral -> MotifKind.Flower
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
                style = Stroke(width = w * 0.3f, cap = StrokeCap.Round),
                alpha = 0.7f * alpha
            )
        }
        MotifKind.RibbonLong -> {
            path.moveTo(0f, 0f)
            path.cubicTo(w * 1.5f, -lengthPx * 0.4f, -w * 1.2f, -lengthPx * 0.7f, 0f, -lengthPx)
            drawPath(
                path,
                brush = bodyBrush,
                style = Stroke(width = w * 0.25f),
                alpha = 0.65f * alpha
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
        color = highlightColor.copy(alpha = (0.3f + bloomEased * 0.4f) * alpha),
        style = Stroke(width = 0.8f.dp.toPx())
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
    drawPath(path, color = color.copy(alpha = alpha), style = Stroke(width = 0.8f.dp.toPx()))
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
