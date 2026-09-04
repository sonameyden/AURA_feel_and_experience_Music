package com.aura.feature.nowplaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AudioAnalyzer
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import com.aura.core.common.util.AppResult
import com.aura.core.data.remote.LyricsResponse
import com.aura.core.data.repository.AtmosphereRepository
import com.aura.core.data.repository.LyricsRepository
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.AtmosphereProfile
import com.aura.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Now Playing screen. AuraPlayer is the single source of truth for
 * playback — this ViewModel never keeps its own "isPlaying"/position copy,
 * it only reads player.playbackState / player.currentPositionMs / player.hasNext
 * / player.hasPrevious and forwards user intents (play/pause/seek/skip) to it.
 *
 * If the caller (Home/Search) already started this song via
 * AuraPlayer.playQueue(...) before navigating here, we don't call play()
 * again — restarting an already-buffering song is what used to make the
 * screen feel like it needed a second tap. We only fall back to play(song)
 * when arriving here without an active matching song (e.g. a mood-input
 * deep link straight into Now Playing).
 *
 * Atmosphere and lyrics are also no longer awaited before the screen can
 * show anything — they load in the background and update in place, so
 * playback + controls appear immediately instead of sitting on a spinner
 * for a couple of network round-trips.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val songRepository: SongRepository,
    private val atmosphereRepository: AtmosphereRepository,
    private val lyricsRepository: LyricsRepository,
    private val player: AuraPlayer,
    private val audioAnalyzer: AudioAnalyzer
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"])

    private val _uiState = MutableStateFlow<NowPlayingUiState>(NowPlayingUiState.Loading)
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    init {
        loadAndPlay()
    }

    private fun loadAndPlay() {
        viewModelScope.launch {
            // Observe the player's playback state to detect when the song changes
            // (e.g., via Next/Previous) and refresh atmosphere/lyrics accordingly.
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

        // If we arrived here and nothing is playing or it's the wrong song,
        // force-start the requested songId. If it is already the current song
        // but paused, resume it.
        viewModelScope.launch {
            if (!player.isCurrentSong(songId)) {
                val songResult = songRepository.getSong(songId)
                val song = (songResult as? AppResult.Success)?.data
                if (song != null) {
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

        activeSongJob?.cancel()
        activeSongJob = viewModelScope.launch {
            // Listen for the audio session ID to be allocated before attaching analyzer.
            launch {
                player.audioSessionIdFlow.collect { sessionId ->
                    if (sessionId != 0) {
                        audioAnalyzer.attach(sessionId)
                    }
                }
            }

            val atmosphereFlow = MutableStateFlow(AtmosphereProfile.loadingPlaceholder(song.id))
            val lyricsFlow = MutableStateFlow<LyricsResponse?>(null)

            launch {
                val result = atmosphereRepository.getAtmosphereForSong(song.id)
                (result as? AppResult.Success)?.data?.let { atmosphereFlow.value = it }
            }
            launch {
                // Safeguard: Do not fetch or display lyrics if the artist is "Unknown Artist"
                // (Matches Section 26-28 of the spec: only verified/matched lyrics should render).
                if (song.artistName != "Unknown Artist") {
                    val result = lyricsRepository.getLyrics(song.id)
                    (result as? AppResult.Success)?.data?.let { lyricsFlow.value = it }
                } else {
                    lyricsFlow.value = null
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
                player.hasNext,
                player.hasPrevious
            ) { snapshot, atmosphere, lyrics, hasNext, hasPrevious ->
                NowPlayingUiState.Ready(
                    playbackState = snapshot.playback,
                    currentPositionMs = snapshot.position,
                    atmosphereProfile = atmosphere,
                    lyrics = lyrics,
                    liveAudioEnergy = snapshot.energy,
                    beatPulse = snapshot.beat,
                    hasNext = hasNext,
                    hasPrevious = hasPrevious
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
