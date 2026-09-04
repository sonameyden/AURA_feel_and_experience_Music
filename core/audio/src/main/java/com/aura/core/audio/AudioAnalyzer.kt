package com.aura.core.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max

/**
 * Reads live amplitude/beat data from whatever AuraPlayer is currently playing,
 * using Android's built-in android.media.audiofx.Visualizer, attached to the
 * ExoPlayer's own audio session id (see AuraPlayer.audioSessionId()).
 *
 * This is what ParticleLayer, KaleidoscopeLayer, and the Rive cat/environment
 * state-machine `energy` inputs all read from.
 *
 * No special runtime permission is needed here — Visualizer only requires
 * RECORD_AUDIO when capturing a DIFFERENT app's audio session; capturing your
 * own app's own ExoPlayer session (which is what this does) does not.
 */
@Singleton
class AudioAnalyzer @Inject constructor() {

    private var visualizer: Visualizer? = null

    private val _currentAmplitude = MutableStateFlow(0f) // 0f..1f, smoothed
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _beatPulse = MutableStateFlow(false) // toggles true briefly on a detected onset
    val beatPulse: StateFlow<Boolean> = _beatPulse.asStateFlow()

    // Smoothing state for amplitude (simple exponential moving average).
    private var smoothedAmplitude = 0f
    private val smoothingFactor = 0.45f // More responsive

    private var rollingEnergyAverage = 0f
    private val rollingAverageFactor = 0.8f // Faster adaptation
    private val beatThresholdMultiplier = 1.12f // Even more sensitive

    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0) return // 0 means no session yet (e.g. player not prepared)
        detach() // clean up any previous instance first

        runCatching {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // max capture size for smoother readings
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform ?: return
                            processWaveform(waveform)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Not used in this version — waveform amplitude is enough
                            // for particle/kaleidoscope/cat energy. Add FFT-band
                            // analysis here later if you want frequency-specific
                            // reactivity (e.g. bass-only pulses).
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2, // sample at half max rate — plenty smooth, less CPU
                    /* waveform = */ true,
                    /* fft = */ false
                )
                enabled = true
            }
        }.onFailure {
            // Visualizer can fail to attach in some emulator configurations or if
            // the session id is momentarily invalid right after a song change —
            // fail silently and just keep emitting the last known amplitude
            // rather than crashing the Now Playing screen over a visual extra.
            visualizer = null
        }
    }

    private fun processWaveform(waveform: ByteArray) {
        // PCM 8-bit unsigned waveform data, centered at 128. Compute average
        // absolute deviation from center as a simple loudness proxy.
        var sum = 0
        for (byte in waveform) {
            // Correctly handle signed-to-unsigned conversion for PCM 8-bit data
            sum += abs((byte.toInt() and 0xFF) - 128)
        }
        val rawAmplitude = (sum.toFloat() / waveform.size) / 128f // normalize to 0..1
        val clamped = rawAmplitude.coerceIn(0f, 1f)

        smoothedAmplitude += (clamped - smoothedAmplitude) * smoothingFactor
        _currentAmplitude.value = smoothedAmplitude

        // Beat detection: spike relative to the rolling average = a pulse.
        rollingEnergyAverage = max(
            0.01f,
            rollingEnergyAverage * rollingAverageFactor + clamped * (1 - rollingAverageFactor)
        )
        val isBeat = clamped > rollingEnergyAverage * beatThresholdMultiplier
        
        if (isBeat && !_beatPulse.value) {
            Log.d("AURA_RES", "BEAT PULSE! Amp: $clamped")
            _beatPulse.value = true
        } else if (!isBeat) {
            _beatPulse.value = false
        }
    }

    fun detach() {
        runCatching {
            visualizer?.enabled = false
            visualizer?.release()
        }
        visualizer = null
        smoothedAmplitude = 0f
        rollingEnergyAverage = 0f
        _currentAmplitude.value = 0f
        _beatPulse.value = false
    }
}
