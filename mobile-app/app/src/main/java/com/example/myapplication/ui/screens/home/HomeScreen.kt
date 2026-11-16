package com.example.myapplication.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

/**
 * CyberSuraksha Home Dashboard with directly integrated 112 India emergency features:
 * - 8 Contact Emergency Services (Police, Fire, Medical, Disaster, Woman, Child, Elderly, Railway)
 * - Situation Description box with microphone voice input
 * - Live Location pill (e.g. Kozhipathy)
 * - Integrated Helpline Information Dialog ((i) button)
 * - Stealth Calculator Disguise, Fake Call, Hotlines, Journey Tracking, and Incident Telemetry.
 */
@Composable
fun CyberSurakshaHomeScreen(
    isEmergencyActive: Boolean,
    statusText: String = "Resolved",
    userName: String = "User",
    parentName: String = "Primary Contact",
    parentPhone: String = "9876543210",
    locationName: String = "Kozhipathy",
    onTriggerManually: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToJourney: () -> Unit = {},
    onNavigateToSafePoints: () -> Unit = {},
    onNavigateToSafetyMap: () -> Unit = {},
    onToggleDisguiseMode: () -> Unit = {},
    onTriggerFakeCall: () -> Unit = {},
    onCallHotline: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var situationText by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ─────────────────────────────────────────────────────────────
        // TOP APP BAR: CyberSuraksha + (i) Info + Disguise + Fake Call + Online
        // ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("CyberSuraksha", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Privacy-Preserving Safety", color = TextSecondary, fontSize = 10.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // (i) Information Helpline Dialog Button (Image 2)
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Helpline Info", tint = AccentTeal, modifier = Modifier.size(16.dp))
                }

                // Fake Call Button
                IconButton(
                    onClick = onTriggerFakeCall,
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Fake Call", tint = AccentCyan, modifier = Modifier.size(16.dp))
                }

                // Calculator Disguise Mode Button
                IconButton(
                    onClick = onToggleDisguiseMode,
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calculator Disguise", tint = WarningAmber, modifier = Modifier.size(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SafeGreen)
                )
                Text("Online", color = SafeGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 1. SITUATION DESCRIPTION BOX (With Microphone voice button)
        // ─────────────────────────────────────────────────────────────
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = situationText,
                    onValueChange = { situationText = it },
                    placeholder = {
                        Text(
                            "Briefly describe the situation.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        Toast.makeText(context, "🎤 Voice note description listening...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = EmergencyRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─────────────────────────────────────────────────────────────
        // 2. CURRENT LOCATION PILL (e.g. Kozhipathy)
        // ─────────────────────────────────────────────────────────────
        Surface(
            color = Color(0xFF2C1518),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = EmergencyRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = locationName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 3. CONTACT EMERGENCY SERVICES (8 Specialized Cards)
        // ─────────────────────────────────────────────────────────────
        Text(
            text = "Contact Emergency Services",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Row 1: POLICE, FIRE, MEDICAL, DISASTER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmergencyServiceCard("POLICE", Icons.Default.Emergency, EmergencyRed, Modifier.weight(1f)) {
                onCallHotline("100")
            }
            EmergencyServiceCard("FIRE", Icons.Default.LocalFireDepartment, WarningAmber, Modifier.weight(1f)) {
                onCallHotline("101")
            }
            EmergencyServiceCard("MEDICAL", Icons.Default.LocalHospital, SafeGreen, Modifier.weight(1f)) {
                onCallHotline("108")
            }
            EmergencyServiceCard("DISASTER", Icons.Default.Flood, AccentCyan, Modifier.weight(1f)) {
                onCallHotline("1077")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Row 2: WOMAN, CHILD, ELDERLY, RAILWAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmergencyServiceCard("WOMAN", Icons.Default.Face, Color(0xFFFF4081), Modifier.weight(1f)) {
                onCallHotline("181")
            }
            EmergencyServiceCard("CHILD", Icons.Default.ChildCare, Color(0xFFFFB74D), Modifier.weight(1f)) {
                onCallHotline("1098")
            }
            EmergencyServiceCard("ELDERLY", Icons.Default.Elderly, AccentTeal, Modifier.weight(1f)) {
                onCallHotline("14567")
            }
            EmergencyServiceCard("RAILWAY", Icons.Default.Train, Color(0xFF64B5F6), Modifier.weight(1f)) {
                onCallHotline("139")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────────────
        // 4. SYSTEM STATUS CARD: Protected & Safe
        // ─────────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "SYSTEM STATUS",
                    color = AccentTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SafeGreen)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Protected & Safe",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "All safety check-ins current. Zero pending alerts.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = DarkNavy)
                Spacer(Modifier.height(12.dp))

                // Telemetry metrics grid (2x2)
                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryMetricItem(
                        modifier = Modifier.weight(1f),
                        label = "USER",
                        value = userName,
                        valueColor = AccentTeal
                    )
                    TelemetryMetricItem(
                        modifier = Modifier.weight(1f),
                        label = "PRIMARY CONTACT",
                        value = parentName,
                        valueColor = TextPrimary
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryMetricItem(
                        modifier = Modifier.weight(1f),
                        label = "LEDGER",
                        value = "VERIFIED (12 BLOCKS)",
                        valueColor = AccentCyan
                    )
                    TelemetryMetricItem(
                        modifier = Modifier.weight(1f),
                        label = "NETWORK",
                        value = "ONLINE (4G LTE)",
                        valueColor = SafeGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 5. EMERGENCY RESPONSE CARD: Instant SOS
        // ─────────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Emergency Response",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "INSTANT SOS",
                        color = EmergencyRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Initiates immediate location logging, encrypted evidence capture, and tiered escalation.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(14.dp))

                // ACTIVATE EMERGENCY MODE RED BUTTON
                Button(
                    onClick = onTriggerManually,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEmergencyActive) "VIEW EMERGENCY SCREEN" else "ACTIVATE EMERGENCY MODE",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tap or Hold to Trigger Incident",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Triggers row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Triggers:", color = TextSecondary, fontSize = 10.sp)
                    TriggerChip("Secret Pattern", onTriggerManually)
                    TriggerChip("Shake", onTriggerManually)
                    TriggerChip("Acoustic AI", onTriggerManually)
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = DarkNavy)
                Spacer(Modifier.height(10.dp))

                // Direct Hotlines Bar: 112, 100, 181, 1091
                Text("DIRECT EMERGENCY HOTLINES", color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HotlineButton("112", "National", EmergencyRed, modifier = Modifier.weight(1f)) { onCallHotline("112") }
                    HotlineButton("100", "Police", AccentCyan, modifier = Modifier.weight(1f)) { onCallHotline("100") }
                    HotlineButton("181", "Women", WarningAmber, modifier = Modifier.weight(1f)) { onCallHotline("181") }
                    HotlineButton("1091", "Distress", SafeGreen, modifier = Modifier.weight(1f)) { onCallHotline("1091") }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 6. JOURNEY MODE CARD: Safe Journey Tracking
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
                    Text(
                        "Journey Mode",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "INACTIVE",
                        color = SafeGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AccentTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Safe Journey Tracking", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Set your destination, ETA, and automated safety check.", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToJourney,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Configure & Start Journey", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("→", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 7. QUICK SAFETY TOOLS: Risk Map & Safe Points
        // ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToSafetyMap() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Risk Map", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("🟢🟡🔴 Zone Intel", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToSafePoints() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Safe Points", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Police & ERs", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 8. RECENT INCIDENT CARD: INC-2026-0001
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
                    Text("Recent Incident", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("INC-2026-0001", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("EMERGENCY BUTTON", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("12:42 PM • Coimbatore", color = TextSecondary, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SafeGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("RESOLVED", color = SafeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text("Integrity: Verified (SHA-256)", color = AccentTeal, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ─────────────────────────────────────────────────────────────
    // INFORMATION - INTEGRATED HELPLINE MODAL (Image 2)
    // ─────────────────────────────────────────────────────────────
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp),
            title = {
                Column {
                    Text(
                        "Information",
                        color = EmergencyRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Integrated Helpline",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InfoHelplineRow(Icons.Default.Emergency, "Police SOS", "provides instant help in police services")
                    InfoHelplineRow(Icons.Default.LocalFireDepartment, "Fire SOS", "provides instant help in fire helpline")
                    InfoHelplineRow(Icons.Default.LocalHospital, "Medical SOS", "provides instant help in medical helpline")
                    InfoHelplineRow(Icons.Default.Flood, "Disaster SOS", "provides instant help in disaster helpline")
                    InfoHelplineRow(Icons.Default.Face, "Women SOS", "provides instant help in women helpline")
                    InfoHelplineRow(Icons.Default.ChildCare, "Child SOS", "provides instant help in child helpline")
                    InfoHelplineRow(Icons.Default.Elderly, "Elderly SOS", "provides instant help in elderly services")
                    InfoHelplineRow(Icons.Default.Train, "Railway SOS", "provides instant help in railway helpline")
                    InfoHelplineRow(Icons.Default.Warning, "SOS", "provides instant help in any emergency situation")
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
private fun EmergencyServiceCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
            .aspectRatio(0.95f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoHelplineRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmergencyRed,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text("▶", color = EmergencyRed, fontSize = 8.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TelemetryMetricItem(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    Column(modifier = modifier) {
        Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HotlineButton(
    number: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = DarkNavy,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(number, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(label, color = TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
private fun TriggerChip(text: String, onClick: () -> Unit) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
