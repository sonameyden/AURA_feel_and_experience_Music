package com.aura.core.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
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
    private var currentSong: Song? = null

    private val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // More aggressive LoadControl to reduce "is so late at playing"
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                5_000, // minBufferMs
                15_000, // maxBufferMs
                1_000, // bufferForPlaybackMs
                2_000 // bufferForPlaybackAfterRebufferMs
            )
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setLoadControl(loadControl)
            .build().apply {
            volume = 1f // Explicitly set volume
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateStr = when (playbackState) {
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        Player.STATE_IDLE -> "IDLE"
                        else -> "UNKNOWN"
                    }
                    Log.d("AuraPlayer", "ExoPlayer State Changed: $stateStr (playWhenReady=${this@apply.playWhenReady})")

                    val song = currentSong ?: return
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d("AuraPlayer", "Emitting PlaybackState.Buffering")
                            _playbackState.value = PlaybackState.Buffering
                        }
                        Player.STATE_READY -> {
                            if (this@apply.playWhenReady) {
                                Log.d("AuraPlayer", "Emitting PlaybackState.Playing")
                                _playbackState.value = PlaybackState.Playing(song, this@apply.currentPosition)
                            } else {
                                Log.d("AuraPlayer", "Emitting PlaybackState.Paused")
                                _playbackState.value = PlaybackState.Paused(song, this@apply.currentPosition)
                            }
                        }
                        Player.STATE_ENDED -> {
                            Log.d("AuraPlayer", "Emitting PlaybackState.Idle (Ended)")
                            _playbackState.value = PlaybackState.Idle
                        }
                        Player.STATE_IDLE -> {
                            Log.d("AuraPlayer", "ExoPlayer is IDLE")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("AuraPlayer", "Player Error: ${error.message}", error)
                    Log.e("AuraPlayer", "Error Code: ${error.errorCodeName}")
                    // Diagnostic logging for stream URL issues
                    currentSong?.let {
                        Log.e("AuraPlayer", "Failed playing URL: ${it.streamUrl}")
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    val song = currentSong ?: return
                    val exoState = this@apply.playbackState
                    Log.d("AuraPlayer", "onPlayWhenReadyChanged: playWhenReady=$playWhenReady, reason=$reason, exoState=$exoState, volume=${this@apply.volume}")

                    if (exoState == Player.STATE_READY) {
                        if (playWhenReady) {
                            Log.d("AuraPlayer", "Emitting PlaybackState.Playing (from onPlayWhenReadyChanged)")
                            _playbackState.value = PlaybackState.Playing(song, this@apply.currentPosition)
                        } else {
                            Log.d("AuraPlayer", "Emitting PlaybackState.Paused (from onPlayWhenReadyChanged)")
                            _playbackState.value = PlaybackState.Paused(song, this@apply.currentPosition)
                        }
                    } else if (exoState == Player.STATE_BUFFERING && playWhenReady) {
                        Log.d("AuraPlayer", "onPlayWhenReadyChanged: Still buffering, ensuring Buffering state")
                        _playbackState.value = PlaybackState.Buffering
                    }
                }
            })
        }
    }

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(song: Song) {
        currentSong = song
        val mediaItem = MediaItem.fromUri(song.streamUrl)
        Log.d("AuraPlayer", "play(song): Setting state to Buffering and preparing ExoPlayer")
        _playbackState.value = PlaybackState.Buffering
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun stop() {
        Log.d("AuraPlayer", "stop(): Stopping ExoPlayer")
        exoPlayer.stop()
        _playbackState.value = PlaybackState.Idle
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
