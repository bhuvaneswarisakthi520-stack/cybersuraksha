package com.example.myapplication.ui.screens.erss112

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 112 India Official Emergency Dashboard Screen matching prototype mockup (Image 1 & Image 2).
 *
 * Implements:
 * 1. 112 Hero Service card with tricolor badge
 * 2. 8 Action buttons: Profile, SOS History, Notification, About Us, Dial 112, Chat Us, TrackMe, Emergency
 * 3. 8 Specialized Emergency Services: POLICE, FIRE, MEDICAL, DISASTER, WOMAN, CHILD, ELDERLY, RAILWAY
 * 4. Situation Description voice/text input box
 * 5. Dynamic GPS Location pill (e.g. Kozhipathy)
 * 6. "Information - Integrated Helpline" popup dialog matching Image 2!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Erss112Screen(
    locationName: String = "Kozhipathy",
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTrackMe: () -> Unit = {},
    onTriggerEmergencySos: () -> Unit = {},
    onCallNumber: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var situationText by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val maroonRed = Color(0xFFB71C1C)
    val cardBg = Color.White
    val bgScreen = Color(0xFFFBF1F1)
    val textDark = Color(0xFF212121)
    val textGrey = Color(0xFF616161)
    val locationPillBg = Color(0xFFF5D6D6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "112 India",
                            color = maroonRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { Toast.makeText(context, "112 India ERSS Menu", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = maroonRed
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = maroonRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = bgScreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ─────────────────────────────────────────────────────────────
            // 1. 112 HERO SERVICE CARD (Red Gradient with Tricolor 112)
            // ─────────────────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B1212), Color(0xFF5A0A0A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "112 Service",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        ServiceBulletItem("Check Your Request History Anytime")
                        ServiceBulletItem("Rate the Service You Received")
                        ServiceBulletItem("112 Services – Always Available, Anywhere")
                        ServiceBulletItem("Choose to Volunteer and Help Others")
                    }

                    // 112 Indian Tricolor Badge
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "112",
                                    color = Color(0xFFFF9933), // Saffron
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    modifier = Modifier.width(48.dp).height(3.dp).background(Color.White)
                                ) {}
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.width(48.dp).height(3.dp).background(Color(0xFF138808)) // Green
                                ) {}
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 2. 8 CIRCULAR ACTION BUTTONS (Profile, History, TrackMe, etc.)
            // ─────────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionRoundButton(
                        label = "Profile",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToProfile
                    )
                    ActionRoundButton(
                        label = "SOS History",
                        icon = Icons.Default.Groups,
                        onClick = onNavigateToHistory
                    )
                    ActionRoundButton(
                        label = "Notification",
                        icon = Icons.Default.Notifications,
                        onClick = { Toast.makeText(context, "No unread emergency broadcast alerts", Toast.LENGTH_SHORT).show() }
                    )
                    ActionRoundButton(
                        label = "About Us",
                        icon = Icons.Default.ChatBubble,
                        onClick = { showAboutDialog = true }
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionRoundButton(
                        label = "Dial 112",
                        icon = Icons.Default.Call,
                        onClick = { onCallNumber("112") }
                    )
                    ActionRoundButton(
                        label = "Chat Us",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?text=Emergency%20SOS%20Alert!%20Need%20Immediate%20Assistance.")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                    ActionRoundButton(
                        label = "TrackMe",
                        icon = Icons.Default.PersonPinCircle,
                        onClick = onNavigateToTrackMe
                    )
                    ActionRoundButton(
                        label = "Emergency",
                        icon = Icons.Default.Warning,
                        isRedSos = true,
                        onClick = onTriggerEmergencySos
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 3. CONTACT EMERGENCY SERVICES SECTION (8 Specialized Cards)
            // ─────────────────────────────────────────────────────────────
            Text(
                text = "Contact Emergency Services",
                color = textDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Grid Row 1: POLICE, FIRE, MEDICAL, DISASTER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyServiceCard(
                    title = "POLICE",
                    icon = Icons.Default.Emergency,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("100")
                }
                EmergencyServiceCard(
                    title = "FIRE",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("101")
                }
                EmergencyServiceCard(
                    title = "MEDICAL",
                    icon = Icons.Default.LocalHospital,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("108")
                }
                EmergencyServiceCard(
                    title = "DISASTER",
                    icon = Icons.Default.Flood,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("1077")
                }
            }

            // Grid Row 2: WOMAN, CHILD, ELDERLY, RAILWAY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyServiceCard(
                    title = "WOMAN",
                    icon = Icons.Default.Face,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("181")
                }
                EmergencyServiceCard(
                    title = "CHILD",
                    icon = Icons.Default.ChildCare,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("1098")
                }
                EmergencyServiceCard(
                    title = "ELDERLY",
                    icon = Icons.Default.Elderly,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("14567")
                }
                EmergencyServiceCard(
                    title = "RAILWAY",
                    icon = Icons.Default.Train,
                    modifier = Modifier.weight(1f)
                ) {
                    onCallNumber("139")
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 4. SITUATION DESCRIPTION VOICE/TEXT INPUT BOX
            // ─────────────────────────────────────────────────────────────
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, maroonRed),
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
                                color = textGrey,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
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
                            tint = maroonRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 5. CURRENT LOCATION PILL (e.g. Kozhipathy)
            // ─────────────────────────────────────────────────────────────
            Surface(
                color = locationPillBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = maroonRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = locationName,
                        color = textDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INFORMATION - INTEGRATED HELPLINE MODAL (Matching Image 2)
    // ─────────────────────────────────────────────────────────────
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Column {
                    Text(
                        "Information",
                        color = maroonRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Integrated Helpline",
                        color = textDark,
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
                        colors = ButtonDefaults.buttonColors(containerColor = maroonRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // ABOUT US DIALOG
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color.White,
            title = { Text("About 112 India", color = maroonRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Emergency Response Support System (ERSS) is a single, pan-India emergency number (112) initiative by the Ministry of Home Affairs, Government of India, powered by C-DAC Thiruvananthapuram.",
                    color = textDark,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = maroonRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ServiceBulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "| $text",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun ActionRoundButton(
    label: String,
    icon: ImageVector,
    isRedSos: Boolean = false,
    onClick: () -> Unit
) {
    val maroonRed = Color(0xFFB71C1C)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            color = if (isRedSos) maroonRed else Color.White,
            shape = CircleShape,
            border = if (!isRedSos) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
            modifier = Modifier.size(54.dp),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRedSos) {
                    Text(
                        "SOS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = maroonRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFF212121),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmergencyServiceCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val maroonRed = Color(0xFFB71C1C)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                tint = maroonRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                color = maroonRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
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
    val maroonRed = Color(0xFFB71C1C)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = maroonRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text("▶", color = maroonRed, fontSize = 8.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = maroonRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color(0xFF424242),
                fontSize = 11.sp
            )
        }
    }
}
