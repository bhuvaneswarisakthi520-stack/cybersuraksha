package com.example.myapplication.data.model

/**
 * Represents the 4 risk states of the CyberSuraksha Safety Risk Engine:
 *
 * - [GREEN_SAFE]: 🟢 Green State — Normal monitoring (low GPS frequency, zero trail persisted).
 * - [YELLOW_CHECK]: 🟡 Yellow State — Pre-Alert & Silent Safety Check (30s timer, haptic feedback, PIN prompt).
 * - [ORANGE_HIGH_THREAT]: 🟠 Orange State — High Threat Active (Max precision GPS, log trail, record audio/video evidence, SHA-256 hashes).
 * - [RED_EMERGENCY]: 🔴 Red State — Full Emergency Escalation (SMS/WhatsApp/FCM alert, loud alarm siren, emergency calls 112/100/181/1091).
 */
enum class EmergencyStage {
    GREEN_SAFE,
    YELLOW_CHECK,
    ORANGE_HIGH_THREAT,
    RED_EMERGENCY
}
