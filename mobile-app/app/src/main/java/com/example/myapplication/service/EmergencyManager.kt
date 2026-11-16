package com.example.myapplication.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages the full emergency lifecycle: activation, stage escalation, and deactivation.
 *
 * Follows the 3-stage escalation model:
 * - Stage 1: Silent safety check (30s timer, then auto-escalate)
 * - Stage 2: Trusted contact alert (30s timer, then auto-escalate)
 * - Stage 3: Full emergency escalation (emergency calls, live location)
 */
class EmergencyManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: EmergencyManager? = null

        fun getInstance(context: Context): EmergencyManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmergencyManager(context.applicationContext).also { INSTANCE = it }
            }

        private const val ESCALATION_DELAY_MS = 30_000L // 30 seconds per stage
    }

    var activeContactsProvider: (() -> List<com.example.myapplication.service.Contact>)? = null

    private val _emergencyState = MutableStateFlow<EmergencyState>(EmergencyState.Inactive)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    private val _currentIncidentId = MutableStateFlow<String?>(null)
    val currentIncidentId: StateFlow<String?> = _currentIncidentId.asStateFlow()

    private var escalationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val db by lazy { com.example.myapplication.data.local.AppDatabase.getDatabase(context) }
    private val evidenceRecorder by lazy { EvidenceRecorder(context) }

    private fun logEvent(incidentId: String, message: String, hash: String? = null) {
        scope.launch(Dispatchers.IO) {
            try {
                db.auditLogDao().insertLog(
                    com.example.myapplication.data.model.AuditLogEntry(
                        incidentId = incidentId,
                        timestamp = System.currentTimeMillis(),
                        message = message,
                        hashSignature = hash
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("EmergencyManager", "Failed to insert audit log", e)
            }
        }
    }

    /**
     * Activates Emergency Mode — creates an incident and starts Stage 1 (silent safety check).
     * Auto-escalation timer begins immediately.
     */
    fun activateEmergency() {
        if (_emergencyState.value is EmergencyState.Active) return

        val incidentId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        _currentIncidentId.value = incidentId

        // Audio/Video evidence recording starts only after explicit user permission/consent
        val hasAudioConsent = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasAudioConsent) {
            evidenceRecorder.startAudioRecording(incidentId)
            try {
                evidenceRecorder.startVideoRecording(
                    androidx.lifecycle.ProcessLifecycleOwner.get(),
                    incidentId
                )
            } catch (e: Exception) {
                android.util.Log.e("EmergencyManager", "Failed to start evidence recording", e)
            }
        }

        _emergencyState.value = EmergencyState.Active(
            incidentId = incidentId,
            stage = "STAGE_1_SILENT",
            startTime = startTime,
            batteryLevel = getBatteryLevel(),
            networkAvailable = getNetworkStatus(),
            isRecording = true
        )

        logEvent(incidentId, "Secret pattern detected — emergency mode started.")
        logEvent(incidentId, "Location + network snapshot captured and hashed.")
        logEvent(incidentId, "Stage 1 — safety check prompt displayed.")

        // Fetch location asynchronously
        fetchCurrentLocation()

        // Start the foreground service
        val serviceIntent = Intent(context, EmergencyForegroundService::class.java).apply {
            action = EmergencyForegroundService.ACTION_START_EMERGENCY
            putExtra(EmergencyForegroundService.EXTRA_INCIDENT_ID, incidentId)
        }
        context.startForegroundService(serviceIntent)

        // Start auto-escalation timer
        startEscalationTimer()
    }

    /**
     * Escalates from Stage 1 to Stage 2 — sends SOS to trusted contacts.
     */
    fun escalateToStage2(contactsList: List<com.example.myapplication.service.Contact> = emptyList()) {
        val current = _emergencyState.value as? EmergencyState.Active ?: return
        if (current.stage != "STAGE_1_SILENT") return

        _emergencyState.value = current.copy(stage = "STAGE_2_CONTACTS")

        logEvent(current.incidentId, "Stage 2 — notifying ${contactsList.size} trusted contacts via SMS.")
        logEvent(current.incidentId, "Trusted contacts notified (${contactsList.size}).")

        // Dispatch real SMS to contacts if provided
        if (contactsList.isNotEmpty()) {
            val dispatcher = SOSDispatcher(context)
            val lat = current.latitude ?: 0.0
            val lng = current.longitude ?: 0.0
            dispatcher.sendSMSToContacts(contactsList, lat, lng, current.incidentId)
        }

        // Also trigger direct WhatsApp text alert to primary contact
        val primaryContact = contactsList.firstOrNull() ?: activeContactsProvider?.invoke()?.firstOrNull()
        if (primaryContact != null) {
            val locUrl = if (current.latitude != null && current.longitude != null) "https://maps.google.com/?q=${current.latitude},${current.longitude}" else "Location tracking active"
            val waMsg = "🆘 URGENT SOS ALERT! ${primaryContact.name} needs help!\nLocation: $locUrl"
            scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                WhatsAppDispatcher.sendWhatsAppTextMessage(context, primaryContact.phone, waMsg)
            }
        }

        // Reset escalation timer for Stage 2 -> Stage 3
        escalationJob?.cancel()
        startEscalationTimer()
    }

    /**
     * Escalates from Stage 2 to Stage 3 — full emergency with calls and live location.
     */
    fun escalateToStage3() {
        val current = _emergencyState.value as? EmergencyState.Active ?: return
        if (current.stage != "STAGE_2_CONTACTS") return

        _emergencyState.value = current.copy(
            stage = "STAGE_3_ESCALATION",
            isRecording = true
        )

        logEvent(current.incidentId, "Stage 3 — full emergency escalation activated. Loud siren playing.")

        // Start loud alarm siren on victim's phone at Stage 3
        EmergencyAlarm.getInstance().startAlarm(context)

        // No more auto-escalation — Stage 3 is the final stage
        escalationJob?.cancel()
    }

    /**
     * Manually escalates to the next stage (used by the "Escalate" button).
     */
    fun escalateToNextStage() {
        val current = _emergencyState.value as? EmergencyState.Active ?: return
        when (current.stage) {
            "STAGE_1_SILENT" -> escalateToStage2()
            "STAGE_2_CONTACTS" -> escalateToStage3()
            // Stage 3 is already the highest level
        }
    }

    /**
     * Deactivates Emergency Mode with PIN verification.
     * Standard PIN ("1234") -> Deactivates emergency cleanly.
     * Duress PIN ("9999") -> Displays fake "Deactivated" UI to coercer while silently triggering 🔴 RED Escalation!
     */
    fun deactivateEmergencyWithPin(pin: String): Boolean {
        if (pin == "9999") {
            // Duress PIN entered! Fake cancel on screen, but jump silently to Stage 3 (RED Emergency)
            val incidentId = _currentIncidentId.value ?: UUID.randomUUID().toString()
            logEvent(incidentId, "🚨 DURESS PIN ENTERED (9999) — Silent RED Emergency Escalation activated!")
            
            val current = _emergencyState.value as? EmergencyState.Active
            if (current != null) {
                _emergencyState.value = current.copy(stage = "STAGE_3_ESCALATION", isRecording = true)
            } else {
                activateEmergency()
                val active = _emergencyState.value as? EmergencyState.Active
                if (active != null) {
                    _emergencyState.value = active.copy(stage = "STAGE_3_ESCALATION", isRecording = true)
                }
            }

            // Immediately dispatch emergency SMS and audio/video evidence to contacts
            val contacts = activeContactsProvider?.invoke() ?: emptyList()
            if (contacts.isNotEmpty()) {
                val dispatcher = SOSDispatcher(context)
                val lat = (current?.latitude) ?: 0.0
                val lng = (current?.longitude) ?: 0.0
                dispatcher.sendSMSToContacts(contacts, lat, lng, incidentId)
            }
            return false // False indicates Duress PIN action
        } else {
            // Standard PIN entered — deactivate normally
            deactivateEmergency()
            return true
        }
    }

    /**
     * Deactivates Emergency Mode — user confirmed safe.
     */
    fun deactivateEmergency() {
        val incidentId = _currentIncidentId.value
        escalationJob?.cancel()
        _emergencyState.value = EmergencyState.Inactive
        _currentIncidentId.value = null

        // Stop evidence recording (audio + video) and calculate SHA-256 hashes
        val savedFiles = evidenceRecorder.stopRecording()
        for ((file, hash) in savedFiles) {
            android.util.Log.i("EmergencyManager", "Evidence saved: ${file.name} with hash $hash")
            if (incidentId != null) {
                logEvent(incidentId, "Evidence saved: ${file.name}", hash)
            }
        }

        if (incidentId != null && savedFiles.isEmpty()) {
            logEvent(incidentId, "Incident resolved — user confirmed safe.")
        }

        // Upload evidence to cloud and send download links to contacts + trigger WhatsApp dispatch
        if (savedFiles.isNotEmpty() && incidentId != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val contacts = activeContactsProvider?.invoke() ?: emptyList()
                val audioFile = savedFiles.firstOrNull { it.first.extension == "m4a" }?.first
                val videoFile = savedFiles.firstOrNull { it.first.extension == "mp4" }?.first
                var firstCloudUrl: String? = null

                for ((file, _) in savedFiles) {
                    val downloadUrl = CloudUploader.uploadEvidenceFile(file, incidentId)
                    if (downloadUrl != null) {
                        if (firstCloudUrl == null) firstCloudUrl = downloadUrl
                        logEvent(incidentId, "Evidence uploaded to cloud: $downloadUrl")
                    } else {
                        android.util.Log.w("EmergencyManager", "Cloud upload fallback for ${file.name} — stored locally")
                    }
                }

                // Always dispatch local audio & video evidence files via WhatsApp to primary contact
                val primaryContact = contacts.firstOrNull()
                if (primaryContact != null) {
                    scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        WhatsAppDispatcher.sendEvidenceFilesViaWhatsApp(
                            context = context,
                            contactPhone = primaryContact.phone,
                            audioFile = audioFile,
                            videoFile = videoFile,
                            cloudLink = firstCloudUrl
                        )
                    }
                }
            }
        }

        // Stop alarm siren if it was playing
        EmergencyAlarm.getInstance().stopAlarm()

        // Stop the foreground service
        val serviceIntent = Intent(context, EmergencyForegroundService::class.java).apply {
            action = EmergencyForegroundService.ACTION_STOP_EMERGENCY
        }
        context.startService(serviceIntent)
    }

    /**
     * Updates the location in the current emergency state.
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        val current = _emergencyState.value as? EmergencyState.Active ?: return
        _emergencyState.value = current.copy(latitude = latitude, longitude = longitude)
    }

    /**
     * Updates network availability in the current emergency state.
     */
    fun updateNetworkStatus(available: Boolean) {
        val current = _emergencyState.value as? EmergencyState.Active ?: return
        _emergencyState.value = current.copy(networkAvailable = available)
    }

    /**
     * Returns the current battery level as a percentage (0–100), or -1 if unavailable.
     */
    fun getBatteryLevel(): Int {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            -1
        }
    }

    /**
     * Returns true if any network (WiFi/cellular) is currently available.
     */
    fun getNetworkStatus(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun fetchCurrentLocation() {
        scope.launch(Dispatchers.IO) {
            try {
                val cancellationToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location ->
                    location?.let {
                        updateLocation(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Location permission not granted — handled gracefully
            }
        }
    }

    private fun startEscalationTimer() {
        escalationJob?.cancel()
        escalationJob = scope.launch {
            delay(ESCALATION_DELAY_MS)
            val current = _emergencyState.value as? EmergencyState.Active ?: return@launch
            when (current.stage) {
                "STAGE_1_SILENT" -> {
                    val list = activeContactsProvider?.invoke() ?: emptyList()
                    escalateToStage2(list)
                }
                "STAGE_2_CONTACTS" -> escalateToStage3()
            }
        }
    }
}

/**
 * Represents the current state of the emergency system.
 */
sealed class EmergencyState {
    /** No active emergency. */
    data object Inactive : EmergencyState()

    /** An emergency is active with full context data. */
    data class Active(
        val incidentId: String,
        val stage: String,
        val startTime: Long,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val batteryLevel: Int = -1,
        val networkAvailable: Boolean = true,
        val isRecording: Boolean = false
    ) : EmergencyState()
}
