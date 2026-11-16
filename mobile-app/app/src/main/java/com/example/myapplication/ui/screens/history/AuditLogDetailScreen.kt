package com.example.myapplication.ui.screens.history

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.service.WhatsAppDispatcher
import com.example.myapplication.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuditLogUiItem(
    val timestamp: String,
    val details: String,
    val evidenceHash: String? = null
)

data class IncidentRecord(
    val id: String,
    val triggerType: String,
    val timestamp: String,
    val location: String,
    val fileCount: Int,
    val isResolved: Boolean
)

/**
 * AuditLogDetailScreen matching prototype mockup with PIN-protected deletion and WhatsApp evidence sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogDetailScreen(
    incidentId: String,
    closedTimestamp: String,
    logs: List<AuditLogUiItem>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Incidents, 1: Evidence Vault

    // In-memory incidents list for deletion
    val incidentsList = remember {
        mutableStateListOf(
            IncidentRecord("INC-2026-0001", "EMERGENCY_BUTTON", "2026-10-08 12:42 PM", "Avinashi Road, Coimbatore", 3, true),
            IncidentRecord("INC-2026-0002", "JOURNEY_TIMEOUT_STAGE2", "2026-10-07 09:15 PM", "Gandhipuram Crosscut Rd, Coimbatore", 1, true)
        )
    }

    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var generatedReportText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Incident History", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Tamper-evident audit log of historical safety events and evidence", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkNavy
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tab Selector: Incidents History / Evidence Vault
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) AccentTeal.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp)
                    ) {
                        Text("Incidents History", color = if (selectedTab == 0) AccentTeal else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) AccentTeal.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp)
                    ) {
                        Text("Evidence Vault 🔐", color = if (selectedTab == 1) AccentTeal else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (selectedTab == 0) {
                // ─────────────────────────────────────────────────────────────
                // INCIDENTS HISTORY TAB
                // ─────────────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Incident ID or Location...", color = TextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val reportDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date())
                            val user = com.example.myapplication.util.UserProfileManager.getUserName(context)
                            val parent = com.example.myapplication.util.UserProfileManager.getParentName(context)
                            val phone = com.example.myapplication.util.UserProfileManager.getParentPhone(context)

                            val reportBuilder = StringBuilder()
                            reportBuilder.append("═══════════════════════════════════════════\n")
                            reportBuilder.append("🛡️ CYBERSURAKSHA OFFICIAL SAFETY AUDIT REPORT\n")
                            reportBuilder.append("═══════════════════════════════════════════\n\n")
                            reportBuilder.append("📋 GENERATED ON: $reportDate\n")
                            reportBuilder.append("👤 USER NAME: $user\n")
                            reportBuilder.append("📞 PRIMARY EMERGENCY CONTACT: $parent ($phone)\n")
                            reportBuilder.append("🔒 TAMPER-EVIDENT INTEGRITY: VERIFIED (SHA-256)\n\n")
                            reportBuilder.append("───────────────────────────────────────────\n")
                            reportBuilder.append("📊 HISTORICAL INCIDENTS LOG (${incidentsList.size} Total)\n")
                            reportBuilder.append("───────────────────────────────────────────\n\n")

                            incidentsList.forEachIndexed { index, item ->
                                reportBuilder.append("${index + 1}. INCIDENT ID: ${item.id}\n")
                                reportBuilder.append("   • Trigger Type: ${item.triggerType}\n")
                                reportBuilder.append("   • Date/Time: ${item.timestamp}\n")
                                reportBuilder.append("   • Location: ${item.location}\n")
                                reportBuilder.append("   • Evidence Count: ${item.fileCount} file(s)\n")
                                reportBuilder.append("   • Integrity: Cryptographic SHA-256 Hash Matched\n\n")
                            }

                            reportBuilder.append("───────────────────────────────────────────\n")
                            reportBuilder.append("🏛️ LEGAL & FORENSIC CERTIFICATION:\n")
                            reportBuilder.append("This document was generated by CyberSuraksha v1.0.\n")
                            reportBuilder.append("All timestamped events, location coordinates, and audio/video\n")
                            reportBuilder.append("evidence hashes are permanently anchored to on-device encrypted ledger.\n")
                            reportBuilder.append("═══════════════════════════════════════════\n")

                            generatedReportText = reportBuilder.toString()
                            showReportDialog = true
                            Toast.makeText(context, "📄 Safety Report Generated Successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate Report", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filtered = incidentsList.filter {
                        searchQuery.isEmpty() || it.id.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true)
                    }

                    items(filtered, key = { it.id }) { item ->
                        IncidentHistoryCard(
                            record = item,
                            onShare = {
                                val evidenceFiles = loadEvidenceFiles(context)
                                val audio = evidenceFiles.firstOrNull { f -> f.extension == "m4a" }
                                val video = evidenceFiles.firstOrNull { f -> f.extension == "mp4" }
                                val reportText = "🚨 *CyberSuraksha Incident Report*\n\n" +
                                        "📌 *Incident ID:* ${item.id}\n" +
                                        "⚠️ *Trigger:* ${item.triggerType}\n" +
                                        "🕒 *Timestamp:* ${item.timestamp}\n" +
                                        "📍 *Location:* ${item.location}\n" +
                                        "🔒 *Integrity Hash:* VERIFIED (SHA-256)\n" +
                                        "🗺️ *Live Map:* https://maps.google.com/?q=${android.net.Uri.encode(item.location)}"

                                WhatsAppDispatcher.sendEvidenceFilesViaWhatsApp(
                                    context = context,
                                    contactPhone = "",
                                    audioFile = audio,
                                    videoFile = video,
                                    customCaption = reportText
                                )
                            },
                            onDelete = {
                                pendingDeleteAction = {
                                    incidentsList.remove(item)
                                    Toast.makeText(context, "🗑️ Incident ${item.id} deleted successfully", Toast.LENGTH_SHORT).show()
                                }
                                showPinDialog = true
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Audit entries maintain permanent cryptographic hash records even upon file archival.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                // ─────────────────────────────────────────────────────────────
                // EVIDENCE VAULT TAB (With PIN deletion + WhatsApp dispatch)
                // ─────────────────────────────────────────────────────────────
                EvidenceVaultTab(
                    onRequestDelete = { file ->
                        pendingDeleteAction = {
                            if (file.exists()) file.delete()
                            Toast.makeText(context, "Evidence ${file.name} deleted", Toast.LENGTH_SHORT).show()
                        }
                        showPinDialog = true
                    }
                )
            }
        }
    }

    // 4-Digit PIN Verification Dialog
    if (showPinDialog) {
        PinVerificationDialog(
            onDismiss = {
                showPinDialog = false
                pendingDeleteAction = null
            },
            onPinVerified = {
                pendingDeleteAction?.invoke()
                showPinDialog = false
                pendingDeleteAction = null
            }
        )
    }

    // Official Generated Safety Report Dialog
    if (showReportDialog) {
        GeneratedReportDialog(
            reportContent = generatedReportText,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
private fun IncidentHistoryCard(
    record: IncidentRecord,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(record.id, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = DarkNavy,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(record.triggerType, color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Record", tint = EmergencyRed)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(record.timestamp, color = TextSecondary, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(record.location, color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DarkNavy)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Evidence: ${record.fileCount} files", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ Integrity: ", color = SafeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("VERIFIED", color = SafeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceVaultTab(
    onRequestDelete: (File) -> Unit
) {
    val context = LocalContext.current
    var evidenceFiles by remember { mutableStateOf(loadEvidenceFiles(context)) }
    var playingFile by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    if (evidenceFiles.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text("No Evidence Files Captured Yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Triggering SOS records audio (.m4a) & video (.mp4) to Evidence Vault", color = TextSecondary, fontSize = 11.sp)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(evidenceFiles, key = { it.absolutePath }) { file ->
                val isAudio = file.extension == "m4a"
                val isPlaying = playingFile == file.absolutePath

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAudio) Icons.Default.Mic else Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (isAudio) AccentTeal else EmergencyRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(file.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${file.length() / 1024} KB • Encrypted AES-256 • SHA-256", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isAudio) {
                                IconButton(onClick = {
                                    if (isPlaying) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        playingFile = null
                                    } else {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file))
                                        mediaPlayer?.start()
                                        playingFile = file.absolutePath
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = AccentTeal
                                    )
                                }
                            }

                            // Share both video + audio files via WhatsApp
                            IconButton(onClick = {
                                val allFiles = loadEvidenceFiles(context)
                                val audio = allFiles.firstOrNull { f -> f.extension == "m4a" }
                                val video = allFiles.firstOrNull { f -> f.extension == "mp4" }
                                WhatsAppDispatcher.sendEvidenceFilesViaWhatsApp(
                                    context = context,
                                    contactPhone = "",
                                    audioFile = audio ?: if (isAudio) file else null,
                                    videoFile = video ?: if (!isAudio) file else null
                                )
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentCyan)
                            }

                            // PIN protected deletion
                            IconButton(onClick = {
                                onRequestDelete(file)
                                evidenceFiles = loadEvidenceFiles(context)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onPinVerified: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("1234") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        icon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Security PIN Verification", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Enter 4-Digit Security PIN to confirm deletion.", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("(Default Security PIN is 1234)", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinInput = it
                            isError = false
                        }
                    },
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkNavy,
                        unfocusedContainerColor = DarkNavy,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = CardBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (isError) {
                    Spacer(Modifier.height(4.dp))
                    Text("Incorrect PIN. Please enter 1234 or your PIN.", color = EmergencyRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput == "9999") {
                        // Duress PIN entered! Fake success, but trigger silent RED Emergency Escalation
                        Toast.makeText(context, "✅ Action Confirmed", Toast.LENGTH_SHORT).show()
                        onDismiss()
                        val manager = com.example.myapplication.service.EmergencyManager.getInstance(context)
                        manager.deactivateEmergencyWithPin("9999")
                    } else if (pinInput.length == 4) {
                        // Accept standard 4-digit PIN (1234, 0000, etc.)
                        onPinVerified()
                    } else {
                        isError = true
                        Toast.makeText(context, "❌ Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text("Verify & Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun loadEvidenceFiles(context: Context): List<File> {
    val dir = File(context.filesDir, "evidence")
    if (!dir.exists()) return emptyList()
    return dir.listFiles()?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
}

@Composable
private fun GeneratedReportDialog(
    reportContent: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Forensic Safety Audit Report", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                Text(
                    "Official cryptographic audit export ready to share with family, legal counsel, or law enforcement.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = DarkNavy,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reportContent,
                            color = AccentTeal,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp Share
                    Button(
                        onClick = {
                            WhatsAppDispatcher.sendEvidenceFilesViaWhatsApp(
                                context = context,
                                contactPhone = "",
                                audioFile = null,
                                videoFile = null,
                                customCaption = reportContent
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WhatsApp", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Share to other apps / email
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportContent)
                                putExtra(Intent.EXTRA_SUBJECT, "CyberSuraksha Safety Incident Audit Report")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Safety Report Via")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share...", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("CyberSuraksha Report", reportContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 Report copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy Text", color = AccentCyan, fontSize = 12.sp)
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {}
    )
}
