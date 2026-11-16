package com.example.myapplication.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * On-Device Acoustic AI Listener.
 * Analyzes ambient microphone audio stream for acoustic scream spikes & distress signals.
 * Triggers SOS callback when high-decibel acoustic energy anomaly is detected.
 */
class AcousticAiDetector(
    private val context: Context,
    private val onAcousticTriggered: () -> Unit
) {

    private var isListening = false
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val AMPLITUDE_THRESHOLD = 24000 // High decibel scream threshold
        private const val SUSTAINED_SPIKES_REQUIRED = 3
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (isListening) return
        isListening = true

        job = scope.launch {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            if (minBufferSize <= 0) return@launch

            val audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize
                )
            } catch (e: Exception) {
                Log.e("AcousticAiDetector", "Failed to initialize AudioRecord", e)
                null
            } ?: return@launch

            try {
                audioRecord.startRecording()
                val buffer = ShortArray(minBufferSize)
                var spikeCount = 0

                while (isListening && isActive) {
                    val readSize = audioRecord.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        var maxAmplitude = 0
                        for (i in 0 until readSize) {
                            val sample = abs(buffer[i].toInt())
                            if (sample > maxAmplitude) maxAmplitude = sample
                        }

                        if (maxAmplitude > AMPLITUDE_THRESHOLD) {
                            spikeCount++
                            Log.w("AcousticAiDetector", "Acoustic AI scream spike detected! Amplitude: $maxAmplitude (Count: $spikeCount)")
                            if (spikeCount >= SUSTAINED_SPIKES_REQUIRED) {
                                withContext(Dispatchers.Main) {
                                    onAcousticTriggered()
                                }
                                spikeCount = 0
                                delay(5000) // Cooldown after trigger
                            }
                        } else {
                            if (spikeCount > 0) spikeCount--
                        }
                    }
                    delay(100)
                }
            } catch (e: Exception) {
                Log.e("AcousticAiDetector", "Error during acoustic recording loop", e)
            } finally {
                try {
                    audioRecord.stop()
                    audioRecord.release()
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        job?.cancel()
    }
}
