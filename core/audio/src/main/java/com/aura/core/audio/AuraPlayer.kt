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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Which song id is currently loaded, and whether it's actually playing (vs paused). */
data class PlayingIndicator(val songId: String, val isPlaying: Boolean)

/**
 * Thin wrapper around Media3 ExoPlayer with simple queue support (Next/
 * Previous). Every song streams from its R2 URL (Song.streamUrl) — there is
 * no local-file / MediaStore playback path.
 *
 * This is the SINGLE SOURCE OF TRUTH for playback across the whole app —
 * Home, Search, the mini-player, and Now Playing all read `playbackState`
 * (and `currentPositionMs`, `hasNext`, `hasPrevious`, `currentPlayingIndicator`)
 * from here rather than keeping their own local "isPlaying" booleans. This is
 * what fixes the "song needs two taps to play" symptom: there used to be no
 * shared queue, so screens had nothing consistent to check before deciding
 * whether to (re)start playback.
 */
@Singleton
class AuraPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var queue: List<Song> = emptyList()
    private var currentIndex: Int = -1
    private var positionTickerJob: Job? = null

    private val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

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
                volume = 1f
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val song = currentSongOrNull() ?: return
                        when (playbackState) {
                            Player.STATE_BUFFERING -> _playbackState.value = PlaybackState.Buffering(song)
                            Player.STATE_READY -> {
                                emitPlayingOrPaused(song)
                                startPositionTicker()
                            }
                            Player.STATE_ENDED -> {
                                if (hasNext.value) skipToNext() else _playbackState.value = PlaybackState.Idle
                            }
                            Player.STATE_IDLE -> Unit
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("AuraPlayer", "Player Error: ${error.message}", error)
                        currentSongOrNull()?.let {
                            Log.e("AuraPlayer", "Failed playing URL: ${it.streamUrl}")
                        }
                        _playbackState.value = PlaybackState.Error(error.message ?: "Playback error")
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        val song = currentSongOrNull() ?: return
                        when {
                            this@apply.playbackState == Player.STATE_READY -> emitPlayingOrPaused(song)
                            this@apply.playbackState == Player.STATE_BUFFERING && playWhenReady ->
                                _playbackState.value = PlaybackState.Buffering(song)
                        }
                    }
                })
                _audioSessionId.value = audioSessionId
            }
    }

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionIdFlow: StateFlow<Int> = _audioSessionId.asStateFlow()

    /** Smooth, continuously-updated playback position — drives the Now Playing seek bar. */
    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()

    private val _hasPrevious = MutableStateFlow(false)
    val hasPrevious: StateFlow<Boolean> = _hasPrevious.asStateFlow()

    /**
     * Derived once, here, so every screen that wants to highlight "the song
     * that's currently playing" (Home's trending row, Search results, ...)
     * reads the same answer instead of re-deriving it from playbackState
     * with slightly different logic in each ViewModel.
     */
    val currentPlayingIndicator: StateFlow<PlayingIndicator?> = playbackState
        .map { state ->
            when (state) {
                is PlaybackState.Playing -> PlayingIndicator(state.song.id, isPlaying = true)
                is PlaybackState.Paused -> PlayingIndicator(state.song.id, isPlaying = false)
                is PlaybackState.Buffering -> PlayingIndicator(state.song.id, isPlaying = true)
                else -> null
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private fun emitPlayingOrPaused(song: Song) {
        val durationMs = exoPlayer.duration.let { if (it == C.TIME_UNSET) 0L else it }
        _playbackState.value = if (exoPlayer.playWhenReady) {
            PlaybackState.Playing(song, exoPlayer.currentPosition, durationMs)
        } else {
            PlaybackState.Paused(song, exoPlayer.currentPosition, durationMs)
        }
    }

    /** Play a single song with no queue context (e.g. a mood-input deep link). */
    fun play(song: Song) = playQueue(listOf(song), 0)

    /** Play `songs` starting at `startIndex` — this list becomes the Next/Previous queue. */
    fun playQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        
        // If we are already playing this exact song from what looks like the same 
        // source/queue context, don't restart it.
        val requestedSong = songs.getOrNull(startIndex)
        if (requestedSong?.id == currentSongOrNull()?.id && queue.size == songs.size) {
            // Check if the IDs match in order (simple heuristic for "same queue")
            val isSameQueue = queue.zip(songs).all { it.first.id == it.second.id }
            if (isSameQueue) {
                Log.d("AuraPlayer", "Already playing requested song in same queue, skipping reset.")
                return
            }
        }

        Log.d("AuraPlayer", "Setting new queue of size ${songs.size}, starting at $startIndex")
        queue = songs
        currentIndex = startIndex.coerceIn(0, songs.lastIndex)
        updateNextPreviousFlags()
        playCurrentQueueItem()
    }

    /** True if `songId` is the song currently loaded (playing, paused, or buffering). */
    fun isCurrentSong(songId: String): Boolean = currentSongOrNull()?.id == songId

    private fun playCurrentQueueItem() {
        val song = currentSongOrNull() ?: return
        val mediaItem = MediaItem.fromUri(song.streamUrl)
        _playbackState.value = PlaybackState.Buffering(song)
        _currentPositionMs.value = 0L
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun skipToNext() {
        if (currentIndex < queue.lastIndex) {
            currentIndex++
            updateNextPreviousFlags()
            playCurrentQueueItem()
        }
    }

    fun skipToPrevious() {
        when {
            exoPlayer.currentPosition > 3_000L || currentIndex == 0 -> exoPlayer.seekTo(0)
            currentIndex > 0 -> {
                currentIndex--
                updateNextPreviousFlags()
                playCurrentQueueItem()
            }
        }
    }

    private fun updateNextPreviousFlags() {
        _hasNext.value = currentIndex in 0 until queue.lastIndex
        _hasPrevious.value = currentIndex > 0
    }

    fun pause() = exoPlayer.pause()

    fun resume() = exoPlayer.play()

    fun stop() {
        exoPlayer.stop()
        positionTickerJob?.cancel()
        _playbackState.value = PlaybackState.Idle
        _currentPositionMs.value = 0L
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun release() {
        positionTickerJob?.cancel()
        exoPlayer.release()
    }

    /** Exposes the raw ExoPlayer's audio session id — needed by AudioAnalyzer's Visualizer. */
    fun audioSessionId(): Int = exoPlayer.audioSessionId

    private fun currentSongOrNull(): Song? = queue.getOrNull(currentIndex)

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                delay(100) // Faster update for better lyric sync
            }
        }
    }
}
