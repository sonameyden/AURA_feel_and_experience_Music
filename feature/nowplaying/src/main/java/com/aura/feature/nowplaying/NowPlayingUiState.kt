package com.aura.feature.nowplaying

import com.aura.core.audio.PlaybackState
import com.aura.core.data.remote.LyricsResponse
import com.aura.core.model.AtmosphereProfile

/**
 * Explicit sealed UI state — modeled per the best-practices doc, no independent
 * booleans. Combines playback + atmosphere + lyrics into one thing the screen renders.
 */
sealed interface NowPlayingUiState {
    data object Loading : NowPlayingUiState

    data class Ready(
        val playbackState: PlaybackState,
        val atmosphereProfile: AtmosphereProfile,
        val lyrics: LyricsResponse?,
        val liveAudioEnergy: Float // smoothed 0f..1f value from AudioAnalyzer, updated continuously
    ) : NowPlayingUiState

    data class Error(val message: String) : NowPlayingUiState
}
