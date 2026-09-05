package com.aura.feature.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aura.core.audio.PlaybackState
import com.aura.feature.nowplaying.visuals.EnvironmentBackground
import com.aura.feature.nowplaying.visuals.KaleidoscopeLayer
import com.aura.feature.nowplaying.visuals.LyricsOverlay
import com.aura.feature.nowplaying.visuals.ParticleLayer
import com.aura.feature.nowplaying.visuals.PlayerControls
import com.aura.feature.nowplaying.visuals.ReactiveGradientLayer
import com.aura.feature.library.AddToPlaylistDialog
import com.aura.feature.library.LibraryUiState
import com.aura.feature.library.LibraryViewModel

@Composable
fun NowPlayingScreen(
    songId: String,
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val libState by libraryViewModel.uiState.collectAsState()
    
    var showPlaylistDialog by remember { mutableStateOf(false) }

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

                val currentLine = state.lyrics?.lines?.lastOrNull { line ->
                    state.currentPositionMs >= line.startTimeMs && 
                    state.currentPositionMs <= line.endTimeMs + 2000L
                }
                val song = (state.playbackState as? PlaybackState.Playing)?.song
                    ?: (state.playbackState as? PlaybackState.Paused)?.song

                if (song != null) {
                    val durationMs = (state.playbackState as? PlaybackState.Playing)?.durationMs
                        ?: (state.playbackState as? PlaybackState.Paused)?.durationMs
                        ?: 0L

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars),
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
                            isLiked = state.isLiked,
                            accentHex = profile.primaryColorHex,
                            onPlayPauseClick = viewModel::onPlayPauseClick,
                            onNextClick = viewModel::onNextClick,
                            onPreviousClick = viewModel::onPreviousClick,
                            onSeek = viewModel::onSeek,
                            onLikeClick = viewModel::onLikeClick,
                            onArtistClick = { onArtistClick(song.artistId) }
                        )
                    }
                }
                
                // Add to Playlist Button (Top Right)
                IconButton(
                    onClick = { showPlaylistDialog = true },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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

    if (showPlaylistDialog) {
        val playlists = (libState as? LibraryUiState.Success)?.playlists ?: emptyList()
        val currentSongId = (uiState as? NowPlayingUiState.Ready)?.let { state ->
            (state.playbackState as? PlaybackState.Playing)?.song?.id 
                ?: (state.playbackState as? PlaybackState.Paused)?.song?.id
        }

        AddToPlaylistDialog(
            playlists = playlists,
            onPlaylistSelected = { playlistId ->
                currentSongId?.let { songId ->
                    libraryViewModel.addSongToPlaylist(playlistId, songId)
                }
            },
            onDismiss = { showPlaylistDialog = false }
        )
    }
}
