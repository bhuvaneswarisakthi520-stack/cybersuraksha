package com.example.myapplication.service

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Utility object for managing the device's Firebase Cloud Messaging (FCM) token.
 *
 * The token is stored in [SharedPreferences] under [PREFS_NAME] so that it is
 * available synchronously without requiring a network call on every app launch.
 * Trusted contacts can exchange tokens out-of-band (e.g. via QR code or SMS)
 * so that the app can target specific devices when sending SOS alerts.
 */
object FcmTokenManager {

    private const val TAG = "FcmTokenManager"
    private const val PREFS_NAME = "cybersuraksha_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    /**
     * Persists [token] to [SharedPreferences].
     *
     * Called automatically by [CyberSurakshaFcmService.onNewToken] whenever
     * FCM rotates the registration token.
     */
    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
        Log.d(TAG, "FCM token saved.")
    }

    /**
     * Returns the most-recently saved FCM token, or `null` if none has been
     * stored yet (e.g. on first launch before FCM delivers a token).
     */
    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Fetches the current FCM token from Firebase and caches it locally.
     *
     * Use this to proactively refresh the token at startup rather than
     * waiting for [CyberSurakshaFcmService.onNewToken] to fire.
     *
     * @param context    Application or activity context.
     * @param onComplete Callback invoked on the calling thread with the token
     *                   string on success, or `null` on failure.
     */
    fun fetchAndSaveToken(context: Context, onComplete: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveToken(context, token)
                Log.d(TAG, "FCM token fetched and saved: $token")
                onComplete(token)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch FCM token", exception)
                onComplete(null)
            }
    }
}
