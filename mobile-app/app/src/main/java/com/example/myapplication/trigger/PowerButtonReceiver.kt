package com.example.myapplication.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.service.EmergencyManager

/**
 * Listens for screen ON/OFF toggle events even when CyberSuraksha is in background/closed.
 * 3 rapid power button presses (screen toggles) within 4 seconds trigger emergency mode.
 */
class PowerButtonReceiver : BroadcastReceiver() {

    companion object {
        private val powerPressTimestamps = mutableListOf<Long>()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF || intent.action == Intent.ACTION_SCREEN_ON) {
            val currentTime = System.currentTimeMillis()
            powerPressTimestamps.add(currentTime)

            // Remove timestamps older than 4 seconds
            powerPressTimestamps.removeAll { currentTime - it > 4000 }

            if (powerPressTimestamps.size >= 3) {
                powerPressTimestamps.clear()
                val emergencyManager = EmergencyManager.getInstance(context)
                emergencyManager.activateEmergency()
            }
        }
    }
}
