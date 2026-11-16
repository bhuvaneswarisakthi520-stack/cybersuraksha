package com.example.myapplication.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.util.EncryptionHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles silent background audio and video recording during emergency events.
 * Encrypts and computes SHA-256 evidence hashes for security audit log.
 */
class EvidenceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var videoRecording: Recording? = null

    private var currentAudioFile: File? = null
    private var currentVideoFile: File? = null

    /**
     * Starts background audio recording (M4A / AAC).
     */
    fun startAudioRecording(incidentId: String): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = File(context.filesDir, "evidence").apply { if (!exists()) mkdirs() }
            val outputFile = File(storageDir, "AUD_${incidentId}_${timeStamp}.m4a")

            @Suppress("DEPRECATION")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            currentAudioFile = outputFile
            Log.i("EvidenceRecorder", "Audio evidence recording started: ${outputFile.name}")
            return outputFile
        } catch (e: Exception) {
            Log.e("EvidenceRecorder", "Failed to start audio recording", e)
            mediaRecorder = null
            currentAudioFile = null
            return null
        }
    }

    /**
     * Starts silent video recording using CameraX (MP4).
     * Checks CAMERA permission before binding to lifecycle.
     */
    fun startVideoRecording(lifecycleOwner: LifecycleOwner, incidentId: String) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            Log.w("EvidenceRecorder", "Cannot record video: CAMERA permission not granted!")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.SD))
                    .build()
                val videoCapture = VideoCapture.Builder(recorder).build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, videoCapture)

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val storageDir = File(context.filesDir, "evidence").apply { if (!exists()) mkdirs() }
                val videoFile = File(storageDir, "VID_${incidentId}_${timeStamp}.mp4")
                currentVideoFile = videoFile

                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                videoRecording = videoCapture.output
                    .prepareRecording(context, outputOptions)
                    .start(ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> Log.i("EvidenceRecorder", "Video recording started: ${videoFile.name}")
                            is VideoRecordEvent.Finalize -> {
                                if (!event.hasError()) {
                                    val hash = EncryptionHelper.hashEvidence(videoFile.absolutePath)
                                    Log.i("EvidenceRecorder", "Video evidence saved: ${videoFile.name}, SHA256: $hash")
                                } else {
                                    Log.e("EvidenceRecorder", "Video recording error event: ${event.error}")
                                }
                            }
                        }
                    }
                Log.i("EvidenceRecorder", "CameraX video recording initialized for ${videoFile.name}")
            } catch (e: Exception) {
                Log.e("EvidenceRecorder", "Failed to start video recording via CameraX", e)
                currentVideoFile = null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Stops both audio and video recordings.
     * Returns a list of (File, SHA256 Hash) for all successfully recorded evidence files.
     */
    fun stopRecording(): List<Pair<File, String>> {
        val results = mutableListOf<Pair<File, String>>()

        // Stop Audio
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("EvidenceRecorder", "Error stopping mediaRecorder audio", e)
        } finally {
            mediaRecorder = null
            currentAudioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val hash = EncryptionHelper.hashEvidence(file.absolutePath)
                    results.add(Pair(file, hash))
                    Log.i("EvidenceRecorder", "Audio saved: ${file.name} (${file.length() / 1024} KB)")
                }
            }
            currentAudioFile = null
        }

        // Stop Video
        try {
            videoRecording?.stop()
        } catch (e: Exception) {
            Log.e("EvidenceRecorder", "Error stopping videoRecording", e)
        } finally {
            videoRecording = null
            currentVideoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val hash = EncryptionHelper.hashEvidence(file.absolutePath)
                    results.add(Pair(file, hash))
                    Log.i("EvidenceRecorder", "Video saved: ${file.name} (${file.length() / 1024} KB)")
                }
            }
            currentVideoFile = null
        }

        return results
    }

    fun isRecording(): Boolean = mediaRecorder != null || videoRecording != null
}
