package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun PlayerControls(
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    isLiked: Boolean,
    accentHex: String,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onLikeClick: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = runCatching { Color(accentHex.toColorInt()) }
        .getOrDefault(Color(0xFFA79AC7))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Song Info (Centered) ---
        Text(
            text = songTitle,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = artistName,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = onArtistClick)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // --- Progress Bar (White track/thumb) ---
        var isDragging by remember { mutableStateOf(false) }
        var dragValue by remember { mutableFloatStateOf(0f) }
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
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMs(if (isDragging) (dragValue * safeDuration).roundToLong() else positionMs),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)
            Text(
                text = "-${formatMs(remainingMs)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Playback Controls Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heart Button (Left-aligned)
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) accent else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Centered Playback Controls (Previous / Play-Pause / Next)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousClick,
                    enabled = hasPrevious
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Main Play/Pause (Circular Accent)
                Surface(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPlayPauseClick),
                    color = accent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = onNextClick,
                    enabled = hasNext
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Right side spacer to keep playback controls centered (since Heart is on the left)
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
