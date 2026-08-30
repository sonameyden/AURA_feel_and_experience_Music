package com.aura.core.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.aura.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Media3 ExoPlayer. Every song streams from its R2 URL
 * (Song.streamUrl) — there is no local-file / MediaStore playback path.
 *
 * NowPlayingViewModel observes `playbackState` and `currentSong` to drive
 * both the standard player UI and (indirectly, via AudioAnalyzer) the
 * reactive visual layers.
 */
@Singleton
class AuraPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    // TODO: map ExoPlayer's Player.STATE_* into this class's PlaybackState
                }
            })
        }
    }

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(song: Song) {
        val mediaItem = MediaItem.fromUri(song.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _playbackState.value = PlaybackState.Playing(song, positionMs = 0L)
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun release() {
        exoPlayer.release()
    }

    /** Exposes the raw ExoPlayer's audio session id — needed by AudioAnalyzer's Visualizer. */
    fun audioSessionId(): Int = exoPlayer.audioSessionId
}
