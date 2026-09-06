package com.aura.feature.nowplaying.visuals

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.aura.core.model.CatBehaviorState
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The high-fidelity persistent cat companion — entirely hand-drawn via procedural math.
 * 
 * Replaces the sprite-sheet attempt to ensure NO external image dependencies, while 
 * capturing the exact AURA cat aesthetics (fur gradients, organic paths, and 
 * independent 4-leg movement) purely through code.
 */
@Composable
fun CatCompanion(
    behaviorState: CatBehaviorState,
    liveAudioEnergy: Float,
    modifier: Modifier = Modifier,
    furColorHex: String = "#B4AEB9",
    bellyColorHex: String = "#EFE9E4"
) {
    val furColor = remember(furColorHex) { parseColorOr(furColorHex, Color(0xFFB4AEB9)) }
    val bellyColor = remember(bellyColorHex) { parseColorOr(bellyColorHex, Color(0xFFEFE9E4)) }

    var timeMs by remember { mutableFloatStateOf(0f) }
    var nextBlinkAt by remember { mutableFloatStateOf(1500f) }
    var blinking by remember { mutableStateOf(false) }

    val currentEnergy by rememberUpdatedState(liveAudioEnergy)
    val currentState by rememberUpdatedState(behaviorState)

    LaunchedEffect(Unit) {
        var last = -1L
        while (isActive) {
            withFrameMillis { frameTime ->
                val dt = if (last >= 0) (frameTime - last).toFloat() else 0f
                last = frameTime
                timeMs += dt

                if (currentState != CatBehaviorState.Sleeping && !blinking && timeMs > nextBlinkAt) {
                    blinking = true
                    nextBlinkAt = timeMs + 120f
                } else if (blinking && timeMs > nextBlinkAt) {
                    blinking = false
                    nextBlinkAt = timeMs + 2200f + Random.nextFloat() * 2600f
                }
            }
        }
    }

    val pose = remember(behaviorState) { CatPose.forState(behaviorState) }

    Canvas(modifier = modifier.size(120.dp)) {
        drawHighFidelityCat(
            pose = pose,
            energy = (0.15f + currentEnergy * 0.85f).coerceIn(0f, 1f),
            timeMs = timeMs,
            eyesClosed = blinking || pose.eyesClosed,
            furColor = furColor,
            bellyColor = bellyColor
        )
    }
}

private fun parseColorOr(hex: String, fallback: Color): Color =
    runCatching { Color(hex.toColorInt()) }.getOrDefault(fallback)

private data class CatPose(
    val bodySquash: Float,
    val bodyStretch: Float,
    val earAngleDeg: Float,
    val tailBaseAngleDeg: Float,
    val tailWagSpeed: Float,
    val tailWagAmplitudeDeg: Float,
    val bounceAmplitudeDp: Float,
    val bounceSpeed: Float,
    val eyesClosed: Boolean,
    val legsVisible: Boolean,
    val legCycleSpeed: Float,
    val showZzz: Boolean,
    val pawRaised: Boolean,
    val bodyAngle: Float = 0f
) {
    companion object {
        fun forState(state: CatBehaviorState): CatPose = when (state) {
            CatBehaviorState.Idle -> CatPose(
                bodySquash = 1f, bodyStretch = 1f, earAngleDeg = 15f,
                tailBaseAngleDeg = 25f, tailWagSpeed = 0.8f, tailWagAmplitudeDeg = 12f,
                bounceAmplitudeDp = 1.2f, bounceSpeed = 1f,
                eyesClosed = false, legsVisible = false, legCycleSpeed = 0f,
                showZzz = false, pawRaised = false
            )
            CatBehaviorState.Sleeping -> CatPose(
                bodySquash = 0.55f, bodyStretch = 1.3f, earAngleDeg = 45f,
                tailBaseAngleDeg = 80f, tailWagSpeed = 0.2f, tailWagAmplitudeDeg = 5f,
                bounceAmplitudeDp = 0.8f, bounceSpeed = 0.4f,
                eyesClosed = true, legsVisible = false, legCycleSpeed = 0f,
                showZzz = true, pawRaised = false
            )
            CatBehaviorState.Walking -> CatPose(
                bodySquash = 0.92f, bodyStretch = 1.1f, earAngleDeg = 18f,
                tailBaseAngleDeg = 10f, tailWagSpeed = 1.8f, tailWagAmplitudeDeg = 15f,
                bounceAmplitudeDp = 4f, bounceSpeed = 3.5f,
                eyesClosed = false, legsVisible = true, legCycleSpeed = 3.5f,
                showZzz = false, pawRaised = false, bodyAngle = -5f
            )
            CatBehaviorState.Running -> CatPose(
                bodySquash = 0.8f, bodyStretch = 1.4f, earAngleDeg = -10f,
                tailBaseAngleDeg = -5f, tailWagSpeed = 4f, tailWagAmplitudeDeg = 20f,
                bounceAmplitudeDp = 8f, bounceSpeed = 7f,
                eyesClosed = false, legsVisible = true, legCycleSpeed = 7f,
                showZzz = false, pawRaised = false, bodyAngle = -12f
            )
            CatBehaviorState.Watching -> CatPose(
                bodySquash = 1.1f, bodyStretch = 0.9f, earAngleDeg = 35f,
                tailBaseAngleDeg = 40f, tailWagSpeed = 0.6f, tailWagAmplitudeDeg = 8f,
                bounceAmplitudeDp = 0.5f, bounceSpeed = 0.6f,
                eyesClosed = false, legsVisible = false, legCycleSpeed = 0f,
                showZzz = false, pawRaised = false
            )
            CatBehaviorState.Playing -> CatPose(
                bodySquash = 1f, bodyStretch = 1f, earAngleDeg = 25f,
                tailBaseAngleDeg = 15f, tailWagSpeed = 3f, tailWagAmplitudeDeg = 25f,
                bounceAmplitudeDp = 6f, bounceSpeed = 5f,
                eyesClosed = false, legsVisible = false, legCycleSpeed = 0f,
                showZzz = false, pawRaised = true
            )
            CatBehaviorState.Stretching -> CatPose(
                bodySquash = 0.75f, bodyStretch = 1.5f, earAngleDeg = 12f,
                tailBaseAngleDeg = 60f, tailWagSpeed = 0.4f, tailWagAmplitudeDeg = 6f,
                bounceAmplitudeDp = 0.2f, bounceSpeed = 0.5f,
                eyesClosed = false, legsVisible = true, legCycleSpeed = 0f,
                showZzz = false, pawRaised = false
            )
        }
    }
}

private fun DrawScope.drawHighFidelityCat(
    pose: CatPose,
    energy: Float,
    timeMs: Float,
    eyesClosed: Boolean,
    furColor: Color,
    bellyColor: Color
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val tSec = timeMs / 1000f

    val bounce = sin(tSec * pose.bounceSpeed * (0.6f + energy)) * pose.bounceAmplitudeDp.dp.toPx()
    val breathe = 1f + sin(tSec * (1.2f + energy)) * 0.02f
    val tailWag = sin(tSec * pose.tailWagSpeed * (0.7f + energy) * (2f * PI.toFloat())) * pose.tailWagAmplitudeDeg

    translate(left = 0f, top = bounce + h * 0.05f) {
        translate(left = cx, top = cy) {
            rotate(degrees = pose.bodyAngle) {
                scale(scaleX = pose.bodyStretch, scaleY = pose.bodySquash * breathe, pivot = Offset.Zero) {
                    
                    // --- TAIL ---
                    rotate(degrees = pose.tailBaseAngleDeg + tailWag, pivot = Offset(w * 0.25f, h * 0.05f)) {
                        val tailPath = Path().apply {
                            moveTo(w * 0.25f, h * 0.05f)
                            cubicTo(w * 0.55f, -h * 0.1f, w * 0.55f, -h * 0.35f, w * 0.35f, -h * 0.45f)
                        }
                        drawPath(
                            path = tailPath,
                            brush = Brush.linearGradient(listOf(furColor, furColor.copy(alpha = 0.8f))),
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // --- LEGS (4 independent legs for walk/run) ---
                    if (pose.legsVisible) {
                        val cycle = tSec * pose.legCycleSpeed * (0.8f + energy) * (2f * PI.toFloat())
                        val legIndices = listOf(0, 1, 2, 3) // front-left, back-left, front-right, back-right
                        val legOffsets = listOf(-0.25f, 0.15f, -0.15f, 0.25f)
                        
                        legIndices.forEach { i ->
                            val phase = i * (PI.toFloat() / 2f)
                            val legX = w * legOffsets[i]
                            val legY = h * 0.12f
                            val lift = abs(sin(cycle + phase)) * 8.dp.toPx()
                            val forward = cos(cycle + phase) * 12.dp.toPx()
                            
                            drawLine(
                                color = furColor,
                                start = Offset(legX, legY),
                                end = Offset(legX + forward, legY + 12.dp.toPx() - lift),
                                strokeWidth = 7.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // --- BODY ---
                    val bodyPath = Path().apply {
                        moveTo(-w * 0.35f, -h * 0.1f)
                        cubicTo(-w * 0.45f, h * 0.1f, w * 0.1f, h * 0.25f, w * 0.35f, h * 0.05f)
                        cubicTo(w * 0.45f, -h * 0.15f, w * 0.15f, -h * 0.25f, -w * 0.15f, -h * 0.22f)
                        close()
                    }
                    drawPath(
                        path = bodyPath,
                        brush = Brush.radialGradient(
                            listOf(furColor, furColor.copy(alpha = 0.9f)),
                            center = Offset.Zero,
                            radius = w * 0.5f
                        )
                    )
                    
                    // Belly patch
                    drawOval(
                        color = bellyColor.copy(alpha = 0.4f),
                        topLeft = Offset(-w * 0.22f, h * 0.02f),
                        size = Size(w * 0.38f, h * 0.18f)
                    )

                    // --- HEAD ---
                    val headPos = Offset(-w * 0.22f, -h * 0.25f)
                    val headR = w * 0.22f
                    
                    // Ears
                    listOf(-1f, 1f).forEach { side ->
                        rotate(degrees = side * pose.earAngleDeg, pivot = headPos) {
                            val earPath = Path().apply {
                                moveTo(headPos.x + side * headR * 0.4f, headPos.y - headR * 0.6f)
                                lineTo(headPos.x + side * headR * 0.9f, headPos.y - headR * 1.6f)
                                lineTo(headPos.x + side * headR * 1.2f, headPos.y - headR * 0.3f)
                                close()
                            }
                            drawPath(earPath, color = furColor)
                            // Inner ear
                            val innerEar = Path().apply {
                                moveTo(headPos.x + side * headR * 0.55f, headPos.y - headR * 0.7f)
                                lineTo(headPos.x + side * headR * 0.85f, headPos.y - headR * 1.35f)
                                lineTo(headPos.x + side * headR * 1.05f, headPos.y - headR * 0.45f)
                                close()
                            }
                            drawPath(innerEar, color = Color(0xFFFFB6C1).copy(alpha = 0.5f))
                        }
                    }

                    // Main head shape
                    val headShape = Path().apply {
                        addOval(Rect(headPos, headR))
                    }
                    drawPath(headShape, color = furColor)

                    // Eyes
                    val eyeY = headPos.y + headR * 0.1f
                    val eyeDx = headR * 0.45f
                    val eyeR = headR * 0.18f
                    listOf(-1f, 1f).forEach { side ->
                        val eyeCenter = Offset(headPos.x + side * eyeDx, eyeY)
                        if (eyesClosed) {
                            drawLine(
                                color = Color(0xFF2D2A26),
                                start = Offset(eyeCenter.x - eyeR, eyeCenter.y),
                                end = Offset(eyeCenter.x + eyeR, eyeCenter.y),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        } else {
                            drawCircle(color = Color.White, radius = eyeR, center = eyeCenter)
                            drawCircle(color = Color(0xFF2D2A26), radius = eyeR * 0.7f, center = eyeCenter)
                            // Reflection
                            drawCircle(color = Color.White, radius = eyeR * 0.2f, center = Offset(eyeCenter.x + eyeR*0.3f, eyeCenter.y - eyeR*0.3f))
                        }
                    }

                    // Nose
                    drawCircle(color = Color(0xFFE5A9A9), radius = 3.dp.toPx(), center = Offset(headPos.x, headPos.y + headR * 0.45f))
                    
                    // Paw Raised (Playing)
                    if (pose.pawRaised) {
                        val pawTime = tSec * 8f
                        val pX = headPos.x + headR * 1.1f + sin(pawTime) * 10.dp.toPx()
                        val pY = headPos.y + headR * 0.6f + cos(pawTime) * 5.dp.toPx()
                        drawCircle(color = furColor, radius = 10.dp.toPx(), center = Offset(pX, pY))
                    }
                }
            }
        }
    }

    // Zzz
    if (pose.showZzz) {
        val zAlpha = 0.3f + 0.6f * ((sin(tSec * 1.5f) + 1f) / 2f)
        val zOff = (tSec * 15f) % 30f
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = furColor.copy(alpha = zAlpha).toArgb()
                textSize = 14.dp.toPx()
                isAntiAlias = true
            }
            drawText("Z", cx + w*0.25f, cy - h*0.4f - zOff, paint)
            paint.textSize = 10.dp.toPx()
            drawText("z", cx + w*0.35f, cy - h*0.5f - zOff*1.3f, paint)
        }
    }
}
