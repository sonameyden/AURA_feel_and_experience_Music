package com.aura.app.player

import androidx.lifecycle.ViewModel
import com.aura.core.audio.AuraPlayer
import com.aura.core.audio.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin ViewModel exposing AuraPlayer's state to the app-wide mini-player
 * mounted in AuraNavHost. Reads directly from AuraPlayer (the single source
 * of truth for playback) — never keeps a separate local isPlaying/song copy,
 * so it always agrees with Now Playing and every other screen.
 */
@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val player: AuraPlayer
) : ViewModel() {
    val playbackState: StateFlow<PlaybackState> = player.playbackState

    fun onPlayPauseClick() {
        when (player.playbackState.value) {
            is PlaybackState.Playing -> player.pause()
            is PlaybackState.Paused -> player.resume()
            else -> Unit
        }
    }
}
