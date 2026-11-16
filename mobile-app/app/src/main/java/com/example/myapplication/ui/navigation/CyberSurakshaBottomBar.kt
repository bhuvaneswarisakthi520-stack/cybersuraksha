package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

/**
 * Top Status Header matching Prototype mockup with clickable profile edit dialog:
 * "CyberSuraksha v1.0 Prototype | Role: User (Prakash B) | 🟢 ONLINE"
 */
@Composable
fun CyberSurakshaTopHeaderBar(
    roleName: String = "User (Prakash B)",
    isOnline: Boolean = true,
    onEditProfileClick: () -> Unit = {}
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("CyberSuraksha", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("v1.0 Prototype", color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { onEditProfileClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Role: ", color = TextSecondary, fontSize = 10.sp)
                    Text(roleName, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = AccentTeal, modifier = Modifier.size(12.dp))
                }
            }

            Surface(
                color = SafeGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SafeGreen)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("ONLINE", color = SafeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar matching Prototype mockup:
 * [Home] [Journey] ( [SOS] glowing center button ) [Incidents] [Evidence]
 */
@Composable
fun CyberSurakshaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSOSClick: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentRoute == AppScreen.Home.route,
                onClick = { onNavigate(AppScreen.Home.route) }
            )

            NavItem(
                label = "Journey",
                icon = Icons.Default.DirectionsWalk,
                isSelected = currentRoute == AppScreen.JourneyMode.route,
                onClick = { onNavigate(AppScreen.JourneyMode.route) }
            )

            // Large center glowing SOS button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed)
                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable { onSOSClick() }
            ) {
                Text(
                    "SOS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            NavItem(
                label = "Incidents",
                icon = Icons.AutoMirrored.Filled.List,
                isSelected = currentRoute == AppScreen.History.route,
                onClick = { onNavigate(AppScreen.History.route) }
            )

            NavItem(
                label = "Evidence",
                icon = Icons.Default.Lock,
                isSelected = currentRoute == AppScreen.History.route, // leads to Evidence Vault tab
                onClick = { onNavigate(AppScreen.History.route) }
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) AccentTeal else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
