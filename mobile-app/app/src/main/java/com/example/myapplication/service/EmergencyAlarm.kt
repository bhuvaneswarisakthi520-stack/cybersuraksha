package com.example.myapplication.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log

class EmergencyAlarm {

    companion object {
        private const val TAG = "EmergencyAlarm"

        @Volatile
        private var instance: EmergencyAlarm? = null

        fun getInstance(): EmergencyAlarm {
            return instance ?: synchronized(this) {
                instance ?: EmergencyAlarm().also { instance = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var vibratorManager: VibratorManager? = null
    private var audioManager: AudioManager? = null
    private var originalAlarmVolume: Int = -1

    fun startAlarm(context: Context) {
        try {
            audioManager = context.getSystemService(AudioManager::class.java)

            // 1. Save current alarm volume
            originalAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: -1

            // 2. Set STREAM_ALARM to maximum volume
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 0
            audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                maxVolume,
                0
            )

            // 3. Get default alarm ringtone URI
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            // 4. Create and configure MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }

            // 5. Start vibration
            val pattern = longArrayOf(0, 300, 200, 300, 200, 600)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibratorManager = context.getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator = context.getSystemService(Vibrator::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }

            // 6. Log
            Log.i(TAG, "Emergency alarm started")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while starting alarm: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while starting alarm: ${e.message}", e)
        }
    }

    fun stopAlarm() {
        try {
            // 1. Stop and release MediaPlayer
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null

            // 2. Restore original alarm volume
            if (originalAlarmVolume >= 0) {
                audioManager?.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    originalAlarmVolume,
                    0
                )
            }

            // 3. Cancel vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibratorManager?.cancel()
                vibratorManager = null
            } else {
                vibrator?.cancel()
                vibrator = null
            }

            audioManager = null
            originalAlarmVolume = -1

            // 4. Log
            Log.i(TAG, "Emergency alarm stopped")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while stopping alarm: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while stopping alarm: ${e.message}", e)
        }
    }
}
