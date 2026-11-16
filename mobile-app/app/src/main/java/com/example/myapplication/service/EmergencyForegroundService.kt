package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Foreground service that keeps running during an active emergency.
 *
 * Responsibilities:
 * - Shows a persistent notification with emergency status
 * - Continuously tracks GPS location (every 5 seconds)
 * - Keeps the app alive even when in the background
 * - Forwards location updates to [EmergencyManager]
 */
class EmergencyForegroundService : Service() {

    companion object {
        const val ACTION_START_EMERGENCY = "com.example.myapplication.START_EMERGENCY"
        const val ACTION_STOP_EMERGENCY = "com.example.myapplication.STOP_EMERGENCY"
        const val EXTRA_INCIDENT_ID = "incident_id"

        private const val CHANNEL_ID = "emergency_channel"
        private const val NOTIFICATION_ID = 1001
        private const val LOCATION_INTERVAL_MS = 5000L
        private const val LOCATION_FASTEST_INTERVAL_MS = 3000L
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var emergencyManager: EmergencyManager

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        emergencyManager = EmergencyManager.getInstance(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    emergencyManager.updateLocation(location.latitude, location.longitude)
                }
            }
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_EMERGENCY -> {
                val incidentId = intent.getStringExtra(EXTRA_INCIDENT_ID) ?: "unknown"
                startForeground(NOTIFICATION_ID, buildNotification(incidentId))
                startLocationUpdates()
            }
            ACTION_STOP_EMERGENCY -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.emergency_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.emergency_channel_desc)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(incidentId: String): Notification {
        // Tapping the notification opens the app
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(this, EmergencyForegroundService::class.java).apply {
            action = ACTION_STOP_EMERGENCY
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🆘 Emergency Active")
            .setContentText("GuardianSOS is tracking your safety. Incident: ${incidentId.take(8)}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(pendingOpenApp)
            .addAction(
                R.drawable.ic_home,
                getString(R.string.confirm_safe),
                pendingStop
            )
            .build()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Location permission not granted — handled gracefully
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
