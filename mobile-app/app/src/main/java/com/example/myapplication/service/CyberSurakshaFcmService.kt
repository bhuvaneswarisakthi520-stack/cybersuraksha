package com.example.myapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for CyberSuraksha emergency alerts.
 *
 * Receives SOS push notifications sent to trusted contacts and displays
 * a high-priority alarm notification that bypasses Do Not Disturb mode
 * and appears over the lock screen.
 */
class CyberSurakshaFcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "CyberSurakshaFCM"
        private const val CHANNEL_ID = "sos_alerts"
        private const val CHANNEL_NAME = "SOS Emergency Alerts"
        private const val NOTIFICATION_ID = 9001
    }

    /**
     * Called when a push message is received while the app is in the foreground,
     * or when a data-only message arrives regardless of app state.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data["type"] == "SOS_ALERT") {
            val senderName = data["senderName"] ?: "Unknown"
            val locationUrl = data["locationUrl"] ?: ""
            val timestamp = data["timestamp"] ?: ""
            showEmergencyNotification(senderName, locationUrl, timestamp)
        }
    }

    /**
     * Called when FCM assigns a new registration token to this device.
     * The token is persisted via [FcmTokenManager] so it can be shared with contacts.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        FcmTokenManager.saveToken(applicationContext, token)
    }

    /**
     * Displays a full-screen, ongoing emergency notification with alarm sound and vibration
     * that cannot be dismissed by the user.
     *
     * @param senderName  Display name of the person in distress.
     * @param locationUrl URL pointing to the sender's live location.
     * @param timestamp   Human-readable time the SOS was triggered.
     */
    private fun showEmergencyNotification(
        senderName: String,
        locationUrl: String,
        timestamp: String,
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // --- Notification Channel (required on Android 8+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "High-priority SOS emergency alerts from trusted contacts"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                setSound(alarmUri, audioAttributes)
                // Allow this channel to bypass Do Not Disturb
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // --- Tap intent: open MainActivity ---
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingTapIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // --- Full-screen intent: shows over lock screen ---
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFullScreenIntent = PendingIntent.getActivity(
            this,
            1,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SOS ALERT — $senderName NEEDS HELP!")
            .setContentText("Tap to see location. Time: $timestamp")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 $senderName needs immediate help!\n\nLocation: $locationUrl\n\nTime: $timestamp"),
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)                           // Cannot be dismissed by the user
            .setAutoCancel(false)
            .setContentIntent(pendingTapIntent)
            .setFullScreenIntent(pendingFullScreenIntent, true) // Pop-up over lock screen
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .setSound(alarmUri)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on secure lock screen
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
