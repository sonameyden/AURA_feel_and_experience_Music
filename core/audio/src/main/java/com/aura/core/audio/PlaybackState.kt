package com.aura.core.audio

import com.aura.core.model.Song

/**
 * UI-facing playback state, exposed as StateFlow<PlaybackState> from AuraPlayer.
 * Modeled explicitly (sealed) rather than several independent booleans,
 * per the Kotlin best-practices doc.
 */
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Buffering(val song: Song) : PlaybackState
    data class Playing(val song: Song, val positionMs: Long, val durationMs: Long = 0L) : PlaybackState
    data class Paused(val song: Song, val positionMs: Long, val durationMs: Long = 0L) : PlaybackState
    data class Error(val message: String) : PlaybackState
}
