package com.example.myapplication.ui.screens.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.screens.history.AuditLogUiItem
import com.example.myapplication.ui.theme.*

/**
 * Pixel-perfect implementation of CyberSuraksha Emergency Active Screen matching prototype mockup (media_1791555221450_331194a9.jpg).
 */
@Composable
fun CyberSurakshaEmergencyScreen(
    auditLogs: List<AuditLogUiItem> = emptyList(),
    trustedContactCount: Int = 1,
    onCallNumber: (String) -> Unit = {},
    onExportEvidence: () -> Unit = {},
    onResolveIncident: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var isSafeClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Active Banner: EMERGENCY ACTIVE + INC-2026-0001
        Surface(
            color = EmergencyRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmergencyRed)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "EMERGENCY ACTIVE",
                        color = EmergencyRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "INC-2026-0001",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 1. INCIDENT TELEMETRY & DIAGNOSTICS CARD
        // ─────────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Incident Telemetry & Diagnostics", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("12:42:15 PM", color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DarkNavy)
                Spacer(Modifier.height(10.dp))

                DiagnosticRow(label = "Location:", value = "Avinashi Road, Coimbatore (11.0168° N, 76.9558° E)", valueColor = TextPrimary)
                DiagnosticRow(label = "Network Status:", value = "ONLINE (4G LTE)", valueColor = TextPrimary)
                DiagnosticRow(label = "Battery Level:", value = "82%", valueColor = TextPrimary)
                DiagnosticRow(label = "Device Status:", value = "ARMED • SECURE BOOT", valueColor = TextPrimary)
                DiagnosticRow(label = "Evidence Capture:", value = "ACTIVE (AUDIO + TELEMETRY)", valueColor = SafeGreen)
                DiagnosticRow(label = "Cloud Sync:", value = "SYNCED (AES-GCM-256)", valueColor = AccentCyan)
                DiagnosticRow(label = "Safety Check:", value = "STAGE 2 (CONTACT ALERTED)", valueColor = WarningAmber)
                DiagnosticRow(label = "Trusted Contact:", value = "NOTIFIED (Primary Contact)", valueColor = TextPrimary)
                DiagnosticRow(label = "Emergency Escalation:", value = "PENDING CONTACT RESOLUTION", valueColor = TextSecondary)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 2. PRIVACY-PRESERVING EVIDENCE PIPELINE CARD
        // ─────────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Privacy-Preserving Evidence Pipeline", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("AES-GCM-256 + SHA-256", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(14.dp))

                // Pipeline Step 1
                PipelineStepItem(
                    stepNumber = "1",
                    title = "Capture Initiated",
                    desc = "Microphone + Location snapshot captured",
                    isComplete = true
                )

                Spacer(Modifier.height(8.dp))

                // Pipeline Step 2
                PipelineStepItem(
                    stepNumber = "2",
                    title = "Client-Side Encrypted",
                    desc = "Encrypted via AES-GCM-256 before disk/network",
                    isComplete = true
                )

                Spacer(Modifier.height(8.dp))

                // Pipeline Step 3
                PipelineStepItem(
                    stepNumber = "3",
                    title = "Integrity Hash Registered",
                    desc = "SHA-256: 9e107d9d...4a2b",
                    isComplete = true
                )

                Spacer(Modifier.height(8.dp))

                // Pipeline Step 4
                PipelineStepItem(
                    stepNumber = "4",
                    title = "Audit Ledger Anchored",
                    desc = "Block #13 chained with previous hash",
                    isComplete = true
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Simulated Buffer Recording indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "SIMULATED BUFFER RECORDING",
                color = EmergencyRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // Action Buttons Row: I'm Safe / Escalate / Call 112
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onResolveIncident() },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
            ) {
                Text("I'm Safe", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { onExportEvidence() },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Escalate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { onCallNumber("112") },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Call 112", color = TextPrimary, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun PipelineStepItem(
    stepNumber: String,
    title: String,
    desc: String,
    isComplete: Boolean
) {
    Surface(
        color = DarkNavy,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SafeGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(desc, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}
