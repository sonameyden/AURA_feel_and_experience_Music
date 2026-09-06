package com.aura.feature.nowplaying.visuals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.data.remote.LyricLine

/**
 * Synced lyrics with "Liquid" transitions. Properties morph smoothly when 
 * resonance (AI emotional highlighting) is detected.
 */
@Composable
fun LyricsOverlay(
    currentLine: LyricLine?,
    isResonant: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = currentLine,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 3 }
                )).togetherWith(
                    fadeOut(animationSpec = tween(400)) + slideOutVertically(
                        animationSpec = tween(400),
                        targetOffsetY = { -it / 3 }
                    )
                )
            },
            label = "lyric_line_swap"
        ) { line ->
            if (line != null) {
                // Morphing Properties: Animate these so they transition smoothly 
                // even if resonance flips while the line is already on screen.
                val animatedFontSize by animateFloatAsState(
                    targetValue = if (isResonant) 26f else 22f, // Slightly larger base sizes
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "fontSize"
                )
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (isResonant) 1f else 0.75f, // Increased from 0.5f for better legibility
                    animationSpec = tween(400),
                    label = "alpha"
                )
                val animatedLineHeight by animateFloatAsState(
                    targetValue = if (isResonant) 34f else 30f,
                    animationSpec = tween(400),
                    label = "lineHeight"
                )

                // Breathing Resonance Effect
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Text(
                        text = line.text,
                        color = Color.White.copy(alpha = animatedAlpha),
                        fontSize = animatedFontSize.sp,
                        fontWeight = if (isResonant) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = animatedLineHeight.sp,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            shadow = if (isResonant) Shadow(
                                color = Color.White.copy(alpha = glowAlpha),
                                blurRadius = 12.dp.value
                            ) else null
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                // Subtlest possible scale-breath for resonance
                                if (isResonant) {
                                    val s = 1f + (glowAlpha * 0.02f)
                                    scaleX = s
                                    scaleY = s
                                }
                            }
                    )
                }
            }
        }
    }
}
