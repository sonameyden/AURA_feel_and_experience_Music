package com.aura.feature.nowplaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.audio.AudioAnalyzer
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import com.aura.core.common.util.AppResult
import com.aura.core.data.repository.AtmosphereRepository
import com.aura.core.data.repository.LyricsRepository
import com.aura.core.data.repository.SongRepository
import com.aura.core.model.AtmosphereProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Now Playing screen — the ONLY screen where the full Immersive
 * Experience Engine renders. Combines PlaybackState + AtmosphereProfile +
 * live AudioAnalyzer energy into one NowPlayingUiState.Ready every visual
 * layer (Rive environment, Rive cat, particles, kaleidoscope, lyrics) reads from.
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
            val songResult = songRepository.getSong(songId)
            val song = (songResult as? AppResult.Success)?.data
            if (song == null) {
                _uiState.value = NowPlayingUiState.Error("Couldn't load this song.")
                return@launch
            }

            player.play(song)
            audioAnalyzer.attach(player.audioSessionId())

            // Atmosphere and lyrics are fetched in parallel — both feed the same Ready state.
            val atmosphereResult = atmosphereRepository.getAtmosphereForSong(songId)
            val atmosphere = (atmosphereResult as? AppResult.Success)?.data
                ?: AtmosphereProfile.loadingPlaceholder(songId)

            val lyricsResult = lyricsRepository.getLyrics(songId)
            val lyrics = (lyricsResult as? AppResult.Success)?.data

            combine(
                player.playbackState,
                audioAnalyzer.currentAmplitude,
                audioAnalyzer.beatPulse
            ) { playback: PlaybackState, energy: Float, beat: Boolean ->
                NowPlayingUiState.Ready(
                    playbackState = playback,
                    atmosphereProfile = atmosphere,
                    lyrics = lyrics,
                    liveAudioEnergy = energy,
                    beatPulse = beat
                )
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    fun onPlayPauseClick() {
        val current = _uiState.value
        if (current is NowPlayingUiState.Ready) {
            when (current.playbackState) {
                is PlaybackState.Playing -> player.pause()
                is PlaybackState.Paused -> player.resume()
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        audioAnalyzer.detach()
        super.onCleared()
    }
}
