package com.aura.core.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.hypot

/**
 * Reads live amplitude/beat data from whatever AuraPlayer is currently playing,
 * using Android's android.media.audiofx.Visualizer API attached to the
 * ExoPlayer's audio session (see AuraPlayer.audioSessionId()).
 *
 * This is what ParticleLayer, KaleidoscopeLayer, and the Rive cat/environment
 * state-machine `energy` inputs all read from — it is the single source of
 * "how energetic does this moment sound right now."
 */
@Singleton
class AudioAnalyzer @Inject constructor() {

    private val _currentAmplitude = MutableStateFlow(0f) // 0f..1f, smoothed
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _beatPulse = MutableStateFlow(false) // toggles true briefly on detected beat/onset
    val beatPulse: StateFlow<Boolean> = _beatPulse.asStateFlow()

    private var visualizer: Visualizer? = null

    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0) return
        
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        if (waveform == null) return
                        
                        // Simple amplitude calculation: RMS
                        var sum = 0f
                        for (i in waveform.indices) {
                            val sample = (waveform[i].toInt() and 0xFF) - 128
                            sum += sample * sample
                        }
                        val rms = Math.sqrt((sum / waveform.size).toDouble()).toFloat()
                        // Normalize: 0..1 (RMS usually peaks around 64-80 for loud music)
                        val normalized = (rms / 64f).coerceIn(0f, 1f)
                        _currentAmplitude.value = normalized
                    }

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        if (fft == null) return
                        
                        // Detect "beat" based on low-frequency energy (bins 2-10)
                        var lowFreqEnergy = 0f
                        for (i in 2..10 step 2) {
                            val real = fft[i].toFloat()
                            val imag = fft[i + 1].toFloat()
                            lowFreqEnergy += hypot(real, imag)
                        }
                        
                        // Simple threshold-based pulse
                        _beatPulse.value = lowFreqEnergy > 150f
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true)
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioAnalyzer", "Failed to initialize Visualizer", e)
        }
    }

    fun detach() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }
}
