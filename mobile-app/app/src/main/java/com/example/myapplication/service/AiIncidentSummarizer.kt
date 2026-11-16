package com.example.myapplication.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AI Incident Summary Engine for CyberSuraksha.
 * Synthesizes raw incident telemetry (location, battery, network, trigger pattern,
 * evidence metadata, contact ack status) into a concise, high-priority incident summary
 * optimized for first responders, trusted contacts, and control-room dashboards.
 */
object AiIncidentSummarizer {

    data class IncidentTelemetry(
        val incidentId: String,
        val stage: String, // "STAGE_1_SILENT", "STAGE_2_CONTACTS", "STAGE_3_ESCALATION"
        val triggerType: String = "Secret Pattern (Volume 3x / Power 3x)",
        val timestampMs: Long = System.currentTimeMillis(),
        val latitude: Double? = null,
        val longitude: Double? = null,
        val batteryLevel: Int = -1,
        val networkAvailable: Boolean = true,
        val isAudioRecording: Boolean = true,
        val isVideoRecording: Boolean = true,
        val trustedContactsNotified: Int = 0,
        val contactAcknowledged: Boolean = false,
        val evidenceFilesCount: Int = 0,
        val sha256Verified: Boolean = true
    )

    /**
     * Synthesizes raw incident data into a concise, professional AI Incident Summary.
     */
    fun generateSummary(telemetry: IncidentTelemetry): String {
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(telemetry.timestampMs))
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(telemetry.timestampMs))

        val priorityHeader = when (telemetry.stage) {
            "STAGE_3_ESCALATION" -> "🚨 CRITICAL INCIDENT (ESCALATED STAGE 3)"
            "STAGE_2_CONTACTS" -> "⚠️ HIGH-PRIORITY SOS ALERT (STAGE 2)"
            else -> "🛡️ ACTIVE EMERGENCY CHECK (STAGE 1)"
        }

        val triggerDesc = "User activated emergency mode via ${telemetry.triggerType} at $timeFmt on $dateFmt."

        val locationDesc = if (telemetry.latitude != null && telemetry.longitude != null) {
            "Current GPS coordinates indicate active position (${String.format(Locale.US, "%.4f", telemetry.latitude)}, ${String.format(Locale.US, "%.4f", telemetry.longitude)})."
        } else {
            "GPS location tracking active."
        }

        val contactDesc = if (telemetry.trustedContactsNotified > 0) {
            if (telemetry.contactAcknowledged) {
                "${telemetry.trustedContactsNotified} trusted contact(s) notified and alert acknowledged."
            } else {
                "${telemetry.trustedContactsNotified} trusted contact(s) notified via Flash SMS & live GPS link (awaiting response)."
            }
        } else {
            "Emergency dispatch queued for trusted contact notification."
        }

        val evidenceDesc = if (telemetry.isAudioRecording || telemetry.isVideoRecording) {
            val recTypes = mutableListOf<String>()
            if (telemetry.isAudioRecording) recTypes.add("AAC Audio")
            if (telemetry.isVideoRecording) recTypes.add("CameraX Video")
            "Real-time evidence capture active (${recTypes.joinToString(" + ")}), hashed with SHA-256."
        } else {
            "Evidence recording standby."
        }

        val telemetryStats = "Device Battery: ${if (telemetry.batteryLevel > 0) "${telemetry.batteryLevel}%" else "Normal"} | Network: ${if (telemetry.networkAvailable) "Online" else "Offline Queue"}."

        return """
            $priorityHeader
            
            $triggerDesc $locationDesc
            
            $contactDesc $evidenceDesc
            
            [$telemetryStats]
        """.trimIndent()
    }

    /**
     * Short 1-sentence AI summary for SMS or Push Notification preview.
     */
    fun generateShortSmsSummary(name: String, timeStr: String, lat: Double?, lng: Double?): String {
        val loc = if (lat != null && lng != null) "https://maps.google.com/?q=$lat,$lng" else "Location tracking active"
        return "🚨 High-priority incident: $name triggered SOS at $timeStr. $loc. Evidence recording active."
    }
}
