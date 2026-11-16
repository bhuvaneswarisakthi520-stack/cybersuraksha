package com.example.myapplication.util

import android.content.Context

/**
 * Manages the Evidence Vault PIN stored in SharedPreferences.
 * The PIN protects deletion of audio/video evidence files.
 */
object EvidencePinManager {

    private const val PREFS_NAME = "cybersuraksha_prefs"
    private const val KEY_EVIDENCE_PIN = "evidence_vault_pin"

    /** Save a 4-digit PIN set by the user */
    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_EVIDENCE_PIN, pin).apply()
    }

    /** Get the saved PIN, or null if not set */
    fun getPin(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EVIDENCE_PIN, null)
    }

    /** Returns true if a PIN has been configured */
    fun isPinSet(context: Context): Boolean = getPin(context) != null

    /** Checks if the entered PIN matches the saved PIN */
    fun verifyPin(context: Context, enteredPin: String): Boolean {
        val saved = getPin(context) ?: return false
        return saved == enteredPin
    }
}
