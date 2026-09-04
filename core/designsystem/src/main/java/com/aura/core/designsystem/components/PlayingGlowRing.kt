package com.aura.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The one "living" element allowed on the otherwise-calm Home/Search
 * screens (per the brief: Home stays calmer than Now Playing, which owns
 * the full immersive engine). A soft breathing badge drawn on whichever
 * card is the currently active song. Everything else on these screens is
 * static — this is the only place restraint gets spent.
 */
@Composable
fun PlayingGlowRing(
    isPlaying: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playing_glow")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val alpha = if (isPlaying) breathe else 0.9f

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.9f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Playing" else "Paused",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}
