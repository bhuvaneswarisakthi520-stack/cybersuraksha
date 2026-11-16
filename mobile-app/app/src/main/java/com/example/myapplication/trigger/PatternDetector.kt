package com.example.myapplication.trigger

import android.view.KeyEvent

interface OnEmergencyTriggered {
    fun onTriggered()
}

class PatternDetector(private val callback: OnEmergencyTriggered) {

    private val volumePressTimestamps = mutableListOf<Long>()
    private val powerPressTimestamps = mutableListOf<Long>()

    /**
     * Call when any volume button (Up or Down) is pressed.
     * Triggers emergency if 3 presses occur within 4 seconds.
     */
    fun onVolumeButtonPressed(): Int {
        val currentTime = System.currentTimeMillis()
        volumePressTimestamps.add(currentTime)

        // Remove timestamps older than 4 seconds
        volumePressTimestamps.removeAll { currentTime - it > 4000 }

        val count = volumePressTimestamps.size
        if (count >= 3) {
            callback.onTriggered()
            volumePressTimestamps.clear()
        }
        return count
    }

    fun onScreenToggled() {
        val currentTime = System.currentTimeMillis()
        powerPressTimestamps.add(currentTime)

        // Remove timestamps older than 4 seconds
        powerPressTimestamps.removeAll { currentTime - it > 4000 }

        if (powerPressTimestamps.size >= 3) {
            callback.onTriggered()
            powerPressTimestamps.clear()
        }
    }

    fun handleKeyEvent(keyCode: Int, action: Int): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) && action == KeyEvent.ACTION_DOWN) {
            onVolumeButtonPressed()
            return true
        }
        return false
    }
}

