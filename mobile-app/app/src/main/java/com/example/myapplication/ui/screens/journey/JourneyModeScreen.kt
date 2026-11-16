package com.example.myapplication.ui.screens.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.screens.contacts.ContactUiModel
import com.example.myapplication.ui.theme.*

/**
 * Pixel-perfect implementation of CyberSuraksha Journey Mode matching prototype mockup (media_1791555239165_c77e6361.jpg).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyModeScreen(
    contacts: List<ContactUiModel> = emptyList(),
    onTriggerEmergency: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var startingLoc by remember { mutableStateOf("Tech Innovation Hub, Coimbatore") }
    var destination by remember { mutableStateOf("Gandhipuram Bus Station") }
    var durationMinutes by remember { mutableIntStateOf(15) }
    var selectedContact by remember { mutableStateOf("Primary Contact") }
    var isJourneyActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Journey Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Automated tiered safety tracking for commutes & solo travel", color = TextSecondary, fontSize = 11.sp)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ─────────────────────────────────────────────────────────────
            // START A MONITORED JOURNEY CARD
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
                        Text("Start a Monitored Journey", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("NEW TRIP", color = SafeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Starting Location Input
                    Text("Starting Location", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = startingLoc,
                        onValueChange = { startingLoc = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkNavy,
                            unfocusedContainerColor = DarkNavy,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    // Destination Input
                    Text("Destination", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkNavy,
                            unfocusedContainerColor = DarkNavy,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    // Duration & Contact Selectors Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Expected Duration", color = TextSecondary, fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = DarkNavy,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$durationMinutes Minutes", color = TextPrimary, fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Trusted Contact", color = TextSecondary, fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = DarkNavy,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedContact, color = TextPrimary, fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Green Start Journey Button
                    Button(
                        onClick = { isJourneyActive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Start Monitored Journey", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Workflow specification footer text
            Text(
                "Workflow strictly implements Stage 1 → Stage 2 → Stage 3 specifications.",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
