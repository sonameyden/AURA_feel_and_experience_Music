package com.aura.feature.nowplaying.visuals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.aura.core.designsystem.components.GlassCard
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp) // Wider card (less padding on sides)
            .padding(bottom = 12.dp)    // Brought down closer to the bottom
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 32.dp 
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 14.dp, horizontal = 20.dp) // Shorter height
                    .fillMaxWidth()
            ) {
                // --- Top: Info + Like ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, // Center vertically to save space
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = songTitle,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = artistName,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable(onClick = onArtistClick)
                        )
                    }

                    // Heart button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(onClick = onLikeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) accent else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Much shorter spacer

                // --- Middle: Progress Bar ---
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
                        thumbColor = Color.Transparent, 
                        activeTrackColor = accent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp) // Even shorter slider height
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(if (isDragging) (dragValue * safeDuration).roundToLong() else positionMs),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatMs(durationMs),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // Much shorter spacer

                // --- Bottom: Playback Controls ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    OutlinedIconButton(
                        onClick = onPreviousClick,
                        enabled = hasPrevious,
                        modifier = Modifier.size(44.dp), // Slightly smaller to keep height short
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious, 
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Play/Pause
                    Surface(
                        modifier = Modifier
                            .size(56.dp) // Slightly smaller
                            .clip(CircleShape)
                            .clickable(onClick = onPlayPauseClick)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = accent),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Next
                    OutlinedIconButton(
                        onClick = onNextClick,
                        enabled = hasNext,
                        modifier = Modifier.size(44.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext, 
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
