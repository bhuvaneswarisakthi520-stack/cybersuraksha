package com.example.myapplication.ui.screens.fakecall

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Realistic Fake Call Overlay Screen.
 * Allows user to exit unsafe situations by receiving a fake call from a trusted contact or authority.
 */
@Composable
fun FakeCallScreen(
    callerName: String = "Mom",
    callerNumber: String = "+91 98765 43210",
    onDismissCall: () -> Unit
) {
    var isCallAccepted by remember { mutableStateOf(false) }
    var callDurationSeconds by remember { mutableIntStateOf(0) }

    // Call duration timer after accept
    LaunchedEffect(isCallAccepted) {
        if (isCallAccepted) {
            while (true) {
                delay(1000)
                callDurationSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Caller Info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCallAccepted) "Active Call" else "Incoming Call...",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = callerName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isCallAccepted) formatDuration(callDurationSeconds) else callerNumber,
                color = if (isCallAccepted) Color(0xFF00E676) else Color.LightGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Center Caller Avatar Icon
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(70.dp)
            )
        }

        // Bottom Action Buttons
        if (!isCallAccepted) {
            // Incoming Call: Accept / Decline buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Call (Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onDismissCall,
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color(0xFFFF1744), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", color = Color.White, fontSize = 12.sp)
                }

                // Accept Call (Green)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { isCallAccepted = true },
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accept", color = Color.White, fontSize = 12.sp)
                }
            }
        } else {
            // In-Call Controls: Mute, Speaker, End Call
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 30.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = "Mute", tint = Color.White)
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // End Call Button (Red)
                IconButton(
                    onClick = onDismissCall,
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFFFF1744), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
