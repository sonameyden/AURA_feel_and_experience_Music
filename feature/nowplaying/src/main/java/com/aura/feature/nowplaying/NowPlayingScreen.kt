package com.aura.feature.nowplaying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.audio.PlaybackState
import com.aura.feature.nowplaying.visuals.CatCompanion
import com.aura.feature.nowplaying.visuals.EnvironmentBackground
import com.aura.feature.nowplaying.visuals.KaleidoscopeLayer
import com.aura.feature.nowplaying.visuals.LyricsOverlay
import com.aura.feature.nowplaying.visuals.ParticleLayer
import com.aura.feature.nowplaying.visuals.PlayerControls
import com.aura.feature.nowplaying.visuals.ReactiveGradientLayer

/**
 * THE core immersive screen — the only place the full Immersive Experience
 * Engine renders (per Section 2 / 5 of the project spec). Every other screen
 * in the app stays on the neutral Aura theme.
 *
 * Layer order (back to front), matching the project spec's Section 4 stack:
 *   1. EnvironmentBackground (Rive scenery, placeholder-first)
 *   2. ReactiveGradientLayer (Compose Canvas)
 *   3. ParticleLayer (Compose Canvas)
 *   4. KaleidoscopeLayer (Compose Canvas)
 *   5. CatCompanion (Rive, placeholder-first)
 *   6. LyricsOverlay (Compose Text)
 */
@Composable
fun NowPlayingScreen(
    songId: String,
    onBackClick: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is NowPlayingUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is NowPlayingUiState.Error -> {
                Text(text = state.message, modifier = Modifier.align(Alignment.Center))
            }

            is NowPlayingUiState.Ready -> {
                val profile = state.atmosphereProfile

                EnvironmentBackground(
                    profile = profile,
                    liveAudioEnergy = state.liveAudioEnergy,
                    modifier = Modifier.fillMaxSize()
                )

                ReactiveGradientLayer(
                    profile = profile,
                    modifier = Modifier.fillMaxSize()
                )

                ParticleLayer(
                    style = profile.particleStyle,
                    energy = profile.energy,
                    tintHex = profile.primaryColorHex,
                    modifier = Modifier.fillMaxSize()
                )

                KaleidoscopeLayer(
                    style = profile.kaleidoscopeStyle,
                    baseEnergy = profile.energy,
                    liveAmplitude = state.liveAudioEnergy,
                    valence = profile.valence,
                    beatPulse = state.beatPulse,
                    primaryColorHex = profile.primaryColorHex,
                    secondaryColorHexes = profile.secondaryColorHexes,
                    modifier = Modifier.fillMaxSize()
                )

                CatCompanion(
                    behaviorState = profile.catBehavior,
                    liveAudioEnergy = state.liveAudioEnergy,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                val currentLine = state.lyrics?.lines?.lastOrNull { line ->
                    // Find the latest line that has already started
                    state.currentPositionMs >= line.startTimeMs && 
                    // But ensure we don't show it forever if the next line is far away 
                    // (max 2s buffer after its stated end time)
                    state.currentPositionMs <= line.endTimeMs + 2000L
                }
                val song = (state.playbackState as? PlaybackState.Playing)?.song
                    ?: (state.playbackState as? PlaybackState.Paused)?.song

                if (song != null) {
                    val durationMs = (state.playbackState as? PlaybackState.Playing)?.durationMs
                        ?: (state.playbackState as? PlaybackState.Paused)?.durationMs
                        ?: 0L

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LyricsOverlay(
                            currentLine = currentLine,
                            isResonant = currentLine?.id in profile.resonantLyricLineIds,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        PlayerControls(
                            songTitle = song.title,
                            artistName = song.artistName,
                            isPlaying = state.playbackState is PlaybackState.Playing,
                            positionMs = state.currentPositionMs,
                            durationMs = durationMs,
                            hasNext = state.hasNext,
                            hasPrevious = state.hasPrevious,
                            accentHex = profile.primaryColorHex,
                            onPlayPauseClick = viewModel::onPlayPauseClick,
                            onNextClick = viewModel::onNextClick,
                            onPreviousClick = viewModel::onPreviousClick,
                            onSeek = viewModel::onSeek
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
