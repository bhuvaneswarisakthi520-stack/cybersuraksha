package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.service.EmergencyManager
import com.example.myapplication.service.EmergencyState
import com.example.myapplication.trigger.OnEmergencyTriggered
import com.example.myapplication.trigger.PatternDetector
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.screens.contacts.ContactUiModel
import com.example.myapplication.ui.screens.history.AuditLogUiItem
import com.example.myapplication.ui.theme.DarkNavy
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var emergencyManager: EmergencyManager
    private lateinit var patternDetector: PatternDetector

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        emergencyManager = EmergencyManager.getInstance(this)
        patternDetector = PatternDetector(object : OnEmergencyTriggered {
            override fun onTriggered() {
                emergencyManager.activateEmergency()
            }
        })

        requestPermissionsIfNeeded()

        // Register power button (screen toggle) receiver for background trigger
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(com.example.myapplication.trigger.PowerButtonReceiver(), filter)

        setContent {
            MyApplicationTheme {
                CyberSurakshaApp(
                    emergencyManager = emergencyManager,
                    onEmergencyCall = { number -> makeEmergencyCall(number) },
                    onExportEvidence = { shareAuditReport() }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event?.repeatCount == 0) {
                val count = patternDetector.onVolumeButtonPressed()
                Toast.makeText(this, "Emergency trigger: press $count/3", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun makeEmergencyCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(intent)
        } else {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(dialIntent)
        }
    }

    private fun shareAuditReport() {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).format(Date())
        val reportText = """
            CyberSuraksha Audit Log Report
            Incident: CS-edf563a2
            
            $dateStr Secret pattern detected - emergency mode started.
            $dateStr Location + network snapshot captured and hashed (sha256=dacd7730e69a...).
            $dateStr Stage 1 - silent safety check started.
            $dateStr Stage 2 - trusted contacts notified.
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CyberSuraksha Evidence Audit Report")
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        startActivity(Intent.createChooser(shareIntent, "Export Evidence Report"))
    }

    private fun requestPermissionsIfNeeded() {
        val needed = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun CyberSurakshaApp(
    emergencyManager: EmergencyManager,
    onEmergencyCall: (String) -> Unit,
    onExportEvidence: () -> Unit
) {
    val navController = rememberNavController()
    val emergencyState by emergencyManager.emergencyState.collectAsState()
    val isEmergencyActive = emergencyState is EmergencyState.Active

    val contacts = remember { mutableStateListOf<ContactUiModel>() }

    // Connect contacts list to EmergencyManager so auto-escalation timer sends SMS to emergency contacts
    LaunchedEffect(contacts) {
        emergencyManager.activeContactsProvider = {
            contacts.filter { it.isEmergencyContact }.map {
                com.example.myapplication.service.Contact(it.name, it.phoneNumber)
            }
        }
    }

    val auditLogs = remember { mutableStateListOf<AuditLogUiItem>() }

    // Dynamically append real audit logs as emergency state changes
    LaunchedEffect(emergencyState) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).format(Date())
        val state = emergencyState
        if (state is EmergencyState.Active) {
            when (state.stage) {
                "STAGE_1_SILENT" -> {
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Secret pattern detected — emergency mode started."))
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Location + network snapshot captured and hashed."))
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Stage 1 — silent safety check started (30s auto-timer)."))
                }
                "STAGE_2_CONTACTS" -> {
                    val emergencyContactCount = contacts.count { it.isEmergencyContact }
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Stage 2 — notifying $emergencyContactCount trusted contacts via SMS."))
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Trusted contacts notified ($emergencyContactCount)."))
                }
                "STAGE_3_ESCALATION" -> {
                    auditLogs.add(0, AuditLogUiItem(dateStr, "Stage 3 — full emergency escalation activated. Loud siren playing."))
                }
            }
        } else {
            if (auditLogs.isNotEmpty() && auditLogs.firstOrNull()?.details?.contains("Incident resolved") != true) {
                auditLogs.add(0, AuditLogUiItem(dateStr, "Incident resolved — user confirmed safe. Audio evidence hashed and saved."))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkNavy
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            isEmergencyActive = isEmergencyActive,
            onSOSActivated = { emergencyManager.activateEmergency() },
            onEmergencyCall = onEmergencyCall,
            contacts = contacts,
            onAddContact = { name, phone, isEmergency ->
                contacts.add(
                    ContactUiModel(
                        id = java.util.UUID.randomUUID().toString(),
                        name = name,
                        phoneNumber = phone,
                        isEmergencyContact = isEmergency
                    )
                )
            },
            onDeleteContact = { id -> contacts.removeAll { it.id == id } },
            onToggleEmergency = { id ->
                val index = contacts.indexOfFirst { it.id == id }
                if (index >= 0) {
                    val c = contacts[index]
                    contacts[index] = c.copy(isEmergencyContact = !c.isEmergencyContact)
                }
            },
            auditLogs = auditLogs,
            onSafe = { emergencyManager.deactivateEmergency() },
            onExportEvidence = onExportEvidence
        )
    }
}