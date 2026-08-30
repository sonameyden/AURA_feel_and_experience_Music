package com.aura.core.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads live amplitude/beat data from whatever AuraPlayer is currently playing,
 * using Android's android.media.audiofx.Visualizer API attached to the
 * ExoPlayer's audio session (see AuraPlayer.audioSessionId()).
 *
 * This is what ParticleLayer, KaleidoscopeLayer, and the Rive cat/environment
 * state-machine `energy` inputs all read from — it is the single source of
 * "how energetic does this moment sound right now."
 *
 * NOTE: This is a Phase 3 implementation stub. Wire the actual
 * android.media.audiofx.Visualizer capture + FFT/amplitude processing here
 * once AuraPlayer is fully working (per Section 12, Phase 3 of the spec).
 */
@Singleton
class AudioAnalyzer @Inject constructor() {

    private val _currentAmplitude = MutableStateFlow(0f) // 0f..1f, smoothed
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _beatPulse = MutableStateFlow(false) // toggles true briefly on detected beat/onset
    val beatPulse: StateFlow<Boolean> = _beatPulse.asStateFlow()

    fun attach(audioSessionId: Int) {
        // TODO Phase 3: android.media.audiofx.Visualizer(audioSessionId).apply { ... }
    }

    fun detach() {
        // TODO Phase 3: release the Visualizer instance
    }
}
