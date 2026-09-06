package com.aura.feature.nowplaying

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AudioAnalyzer
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import com.aura.core.common.util.AppResult
import com.aura.core.data.remote.LyricsResponse
import com.aura.core.data.repository.*
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val songRepository: SongRepository,
    private val atmosphereRepository: AtmosphereRepository,
    private val lyricsRepository: LyricsRepository,
    private val libraryRepository: LibraryRepository,
    private val player: AuraPlayer,
    private val audioAnalyzer: AudioAnalyzer
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow<NowPlayingUiState>(NowPlayingUiState.Loading)
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val isLikedFlow = MutableStateFlow(false)

    // FIX: carries the last successfully-loaded REAL profile across song
    // changes. Without this, every skip reset atmosphereFlow to a generic
    // neutral placeholder (Nature/Aura-Violet/Idle) for the entire network
    // round-trip, then snapped again once the real profile arrived — two
    // hard visual cuts sandwiching a loading gap, which is what read as both
    // "abrupt" and "slow". By keeping the PREVIOUS real profile flowing
    // instead, KaleidoscopeLayer's existing animateFloatAsState/
    // animateColorAsState/style-morph logic (already built for this) smoothly
    // carries the visuals from the old song's real profile straight to the
    // new one, with no placeholder detour in between.
    private var carriedAtmosphereProfile: AtmosphereProfile? = null

    init {
        loadAndPlay()
    }

    private fun loadAndPlay() {
        viewModelScope.launch {
            player.playbackState.collect { playback ->
                val currentSong = when (playback) {
                    is PlaybackState.Playing -> playback.song
                    is PlaybackState.Paused -> playback.song
                    is PlaybackState.Buffering -> playback.song
                    else -> null
                }

                if (currentSong != null) {
                    refreshForSong(currentSong)
                }
            }
        }

        viewModelScope.launch {
            if (!player.isCurrentSong(songId)) {
                val songResult = songRepository.getSong(songId)
                val song = (songResult as? AppResult.Success)?.data
                if (song != null) {
                    if (song.streamUrl.isBlank()) {
                        Log.e("NowPlayingViewModel", "Song ${song.id} has empty streamUrl!")
                    }
                    player.play(song)
                } else {
                    _uiState.value = NowPlayingUiState.Error("Couldn't load this song.")
                }
            } else if (player.playbackState.value is PlaybackState.Paused) {
                player.resume()
            }
        }
    }

    private var activeSongJob: Job? = null
    private var lastLoadedSongId: String? = null

    private fun refreshForSong(song: Song) {
        if (song.id == lastLoadedSongId) return
        lastLoadedSongId = song.id

        isLikedFlow.value = false

        activeSongJob?.cancel()
        activeSongJob = viewModelScope.launch {
            launch {
                player.audioSessionIdFlow.collect { sessionId ->
                    if (sessionId != 0) {
                        audioAnalyzer.attach(sessionId)
                    }
                }
            }

            launch {
                libraryRepository.addToHistory(song.id)
            }

            // FIX: seed with the carried-over previous real profile instead of
            // AtmosphereProfile.loadingPlaceholder(song.id). Only genuinely
            // falls back to the neutral placeholder on the very first song of
            // the session, when there's nothing real to carry forward yet.
            val atmosphereFlow = MutableStateFlow(
                carriedAtmosphereProfile ?: AtmosphereProfile.loadingPlaceholder(song.id)
            )
            val lyricsFlow = MutableStateFlow<LyricsResponse?>(null)

            launch {
                val result = libraryRepository.getLikedSongs()
                if (result is AppResult.Success) {
                    isLikedFlow.value = result.data.any { it.id == song.id }
                }
            }

            launch {
                val result = atmosphereRepository.getAtmosphereForSong(song.id)
                if (result is AppResult.Success) {
                    atmosphereFlow.value = result.data
                    // FIX: remember this as the new "last real profile" so the
                    // *next* song change carries forward from here, not from
                    // whatever song started the app session.
                    carriedAtmosphereProfile = result.data
                }
            }

            launch {
                val result = lyricsRepository.getLyrics(song.id)
                if (result is AppResult.Success) {
                    lyricsFlow.value = result.data
                }
            }

            val playbackSnapshot = combine(
                player.playbackState,
                player.currentPositionMs,
                audioAnalyzer.currentAmplitude,
                audioAnalyzer.beatPulse
            ) { playback, position, energy, beat ->
                PlaybackSnapshot(playback, position, energy, beat)
            }

            combine(
                playbackSnapshot,
                atmosphereFlow,
                lyricsFlow,
                isLikedFlow,
                player.hasNext,
                player.hasPrevious
            ) { args ->
                val snapshot = args[0] as PlaybackSnapshot
                val atmosphere = args[1] as AtmosphereProfile
                val lyrics = args[2] as? LyricsResponse
                val isLiked = args[3] as Boolean
                val hasNext = args[4] as Boolean
                val hasPrevious = args[5] as Boolean

                NowPlayingUiState.Ready(
                    playbackState = snapshot.playback,
                    currentPositionMs = snapshot.position,
                    atmosphereProfile = atmosphere,
                    lyrics = lyrics,
                    liveAudioEnergy = snapshot.energy,
                    beatPulse = snapshot.beat,
                    hasNext = hasNext,
                    hasPrevious = hasPrevious,
                    isLiked = isLiked
                )
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    fun onPlayPauseClick() {
        when (player.playbackState.value) {
            is PlaybackState.Playing -> player.pause()
            is PlaybackState.Paused -> player.resume()
            else -> Unit
        }
    }

    fun onNextClick() = player.skipToNext()

    fun onPreviousClick() = player.skipToPrevious()

    fun onSeek(positionMs: Long) = player.seekTo(positionMs)

    fun onLikeClick() {
        val songId = lastLoadedSongId ?: return
        val currentLiked = isLikedFlow.value

        viewModelScope.launch {
            isLikedFlow.value = !currentLiked
            val result = if (currentLiked) {
                libraryRepository.unlikeSong(songId)
            } else {
                libraryRepository.likeSong(songId)
            }
            if (result is AppResult.Error) {
                isLikedFlow.value = currentLiked
            }
        }
    }

    override fun onCleared() {
        audioAnalyzer.detach()
        super.onCleared()
    }

    private data class PlaybackSnapshot(
        val playback: PlaybackState,
        val position: Long,
        val energy: Float,
        val beat: Boolean
    )
}