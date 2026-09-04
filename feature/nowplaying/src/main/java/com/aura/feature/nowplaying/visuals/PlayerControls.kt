package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToLong

/**
 * The bottom control cluster — song identity, seek bar, and transport
 * controls (previous / play-pause / next). Styled as translucent "glass"
 * discs over a soft dark scrim so the kaleidoscope stays readable behind it
 * (per the AURA visual-language brief: controls belong to the world, they
 * don't cover it with an opaque card).
 */
@Composable
fun PlayerControls(
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    accentHex: String,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = runCatching { Color(android.graphics.Color.parseColor(accentHex)) }
        .getOrDefault(Color(0xFFA79AC7))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = songTitle,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = artistName,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            var isDragging by remember { mutableStateOf(false) }
            var dragValue by remember { mutableStateOf(0f) }
            val safeDuration = durationMs.coerceAtLeast(1L)
            val sliderValue = if (isDragging) dragValue else (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)

            Slider(
                value = sliderValue,
                onValueChange = {
                    isDragging = true
                    dragValue = it
                },
                onValueChangeFinished = {
                    onSeek((dragValue * safeDuration).roundToLong())
                    isDragging = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = accent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(if (isDragging) (dragValue * safeDuration).roundToLong() else positionMs),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(text = formatMs(durationMs), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    enabled = hasPrevious,
                    size = 52.dp,
                    accent = accent,
                    onClick = onPreviousClick
                )

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GlassIconButton(
                        icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        enabled = true,
                        size = 76.dp,
                        accent = accent,
                        filled = true,
                        onClick = onPlayPauseClick
                    )
                }

                GlassIconButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    enabled = hasNext,
                    size = 52.dp,
                    accent = accent,
                    onClick = onNextClick
                )
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    size: Dp,
    accent: Color,
    onClick: () -> Unit,
    filled: Boolean = false
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (filled) accent.copy(alpha = 0.95f) else Color.White.copy(alpha = if (enabled) 0.16f else 0.08f)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) Color.White else Color.White.copy(alpha = if (enabled) 0.95f else 0.35f),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
