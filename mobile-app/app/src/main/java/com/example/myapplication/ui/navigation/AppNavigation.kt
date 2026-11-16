package com.example.myapplication.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.screens.contacts.ContactUiModel
import com.example.myapplication.ui.screens.contacts.ContactsScreen
import com.example.myapplication.ui.screens.emergency.CyberSurakshaEmergencyScreen
import com.example.myapplication.ui.screens.history.AuditLogDetailScreen
import com.example.myapplication.ui.screens.history.AuditLogUiItem
import com.example.myapplication.ui.screens.home.CyberSurakshaHomeScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class AppScreen(val route: String) {
    data object Home : AppScreen("home")
    data object Contacts : AppScreen("contacts")
    data object Settings : AppScreen("settings")
    data object EmergencyActive : AppScreen("emergency")
    data object History : AppScreen("history")
    data object JourneyMode : AppScreen("journey")
    data object SafePoints : AppScreen("safepoints")
    data object SafetyMap : AppScreen("safetymap")
    data object Erss112 : AppScreen("erss112")
    data object ErssProfile : AppScreen("erssprofile")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isEmergencyActive: Boolean = false,
    onSOSActivated: () -> Unit = {},
    onEmergencyCall: (String) -> Unit = {},
    contacts: List<ContactUiModel> = emptyList(),
    onAddContact: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onDeleteContact: (String) -> Unit = {},
    onToggleEmergency: (String) -> Unit = {},
    auditLogs: List<AuditLogUiItem> = emptyList(),
    onSafe: () -> Unit = {},
    onExportEvidence: () -> Unit = {}
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppScreen.Home.route

    // Persistent User Profile state (loaded from UserProfileManager)
    var userName by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getUserName(context)) }
    var parentName by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getParentName(context)) }
    var parentPhone by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getParentPhone(context)) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Stealth & Disguise Overlay States
    var isDisguiseActive by remember { mutableStateOf(false) }
    var showFakeCall by remember { mutableStateOf(false) }

    // Start ShakeDetector for hard shake gesture
    DisposableEffect(Unit) {
        val shakeDetector = com.example.myapplication.trigger.ShakeDetector {
            Toast.makeText(context, "🚨 HARD SHAKE DETECTED! Triggering SOS...", Toast.LENGTH_LONG).show()
            onSOSActivated()
            navController.navigate(AppScreen.EmergencyActive.route)
        }
        shakeDetector.startListening(context)
        onDispose {
            shakeDetector.stopListening()
        }
    }

    // Camouflage Calculator Disguise Overlay (Safe Design: Discreet vibration, background SOS, harmless screen)
    if (isDisguiseActive) {
        com.example.myapplication.ui.screens.disguise.CalculatorDisguiseScreen(
            secretUnlockPattern = "7777",
            secretSosPattern = "9999",
            onUnlockApp = {
                isDisguiseActive = false
            },
            onTriggerDisguisedSos = {
                // Background SOS transmission while remaining on harmless calculator screen
                onSOSActivated()
            }
        )
        return
    }

    // Fake Call Overlay
    if (showFakeCall) {
        com.example.myapplication.ui.screens.fakecall.FakeCallScreen(
            callerName = parentName,
            callerNumber = parentPhone,
            onDismissCall = { showFakeCall = false }
        )
        return
    }

    Scaffold(
        topBar = {
            if (currentRoute != AppScreen.Erss112.route && currentRoute != AppScreen.ErssProfile.route) {
                CyberSurakshaTopHeaderBar(
                    roleName = "User ($userName)",
                    isOnline = true,
                    onEditProfileClick = { showProfileDialog = true }
                )
            }
        },
        bottomBar = {
            CyberSurakshaBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(AppScreen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onSOSClick = {
                    if (isEmergencyActive) {
                        navController.navigate(AppScreen.EmergencyActive.route)
                    } else {
                        onSOSActivated()
                        navController.navigate(AppScreen.EmergencyActive.route)
                    }
                }
            )
        },
        containerColor = DarkNavy
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(AppScreen.Home.route) {
                CyberSurakshaHomeScreen(
                    isEmergencyActive = isEmergencyActive,
                    statusText = if (isEmergencyActive) "Emergency Active" else "Resolved",
                    userName = userName,
                    parentName = parentName,
                    parentPhone = parentPhone,
                    onTriggerManually = {
                        if (isEmergencyActive) {
                            navController.navigate(AppScreen.EmergencyActive.route)
                        } else {
                            onSOSActivated()
                            navController.navigate(AppScreen.EmergencyActive.route)
                        }
                    },
                    onNavigateToContacts = { navController.navigate(AppScreen.Contacts.route) },
                    onNavigateToHistory = { navController.navigate(AppScreen.History.route) },
                    onNavigateToJourney = { navController.navigate(AppScreen.JourneyMode.route) },
                    onNavigateToSafePoints = { navController.navigate(AppScreen.SafePoints.route) },
                    onNavigateToSafetyMap = { navController.navigate(AppScreen.SafetyMap.route) },
                    onToggleDisguiseMode = { isDisguiseActive = true },
                    onTriggerFakeCall = { showFakeCall = true },
                    onCallHotline = onEmergencyCall
                )
            }

            composable(AppScreen.Contacts.route) {
                ContactsScreen(
                    contacts = contacts,
                    onAddContact = onAddContact,
                    onDeleteContact = onDeleteContact,
                    onToggleEmergency = onToggleEmergency,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.History.route) {
                val formattedLogs = if (auditLogs.isNotEmpty()) {
                    auditLogs
                } else {
                    listOf(
                        AuditLogUiItem(
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                            "No new incidents logged yet. Trigger SOS to create real-time audit logs."
                        )
                    )
                }

                AuditLogDetailScreen(
                    incidentId = "INC-2026-0001",
                    closedTimestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date()),
                    logs = formattedLogs,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.EmergencyActive.route) {
                CyberSurakshaEmergencyScreen(
                    auditLogs = auditLogs,
                    trustedContactCount = contacts.count { it.isEmergencyContact },
                    onCallNumber = onEmergencyCall,
                    onExportEvidence = onExportEvidence,
                    onResolveIncident = {
                        onSafe()
                        navController.popBackStack(AppScreen.Home.route, inclusive = false)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.JourneyMode.route) {
                com.example.myapplication.ui.screens.journey.JourneyModeScreen(
                    contacts = contacts,
                    onTriggerEmergency = {
                        onSOSActivated()
                        navController.navigate(AppScreen.EmergencyActive.route)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.SafePoints.route) {
                com.example.myapplication.ui.screens.safepoints.SafePointsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.SafetyMap.route) {
                com.example.myapplication.ui.screens.safetymap.SafetyRiskMapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppScreen.Erss112.route) {
                com.example.myapplication.ui.screens.erss112.Erss112Screen(
                    locationName = "Kozhipathy",
                    onNavigateToProfile = { navController.navigate(AppScreen.ErssProfile.route) },
                    onNavigateToHistory = { navController.navigate(AppScreen.History.route) },
                    onNavigateToTrackMe = { navController.navigate(AppScreen.JourneyMode.route) },
                    onTriggerEmergencySos = {
                        onSOSActivated()
                        navController.navigate(AppScreen.EmergencyActive.route)
                    },
                    onCallNumber = onEmergencyCall
                )
            }

            composable(AppScreen.ErssProfile.route) {
                com.example.myapplication.ui.screens.erss112.Erss112ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Editable Profile & Emergency Contact Dialog
    if (showProfileDialog) {
        var inputUser by remember { mutableStateOf(userName) }
        var inputParent by remember { mutableStateOf(parentName) }
        var inputPhone by remember { mutableStateOf(parentPhone) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Edit Profile & Primary Contact", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User Name", color = TextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = { inputUser = it },
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

                    Text("Emergency Contact Name", color = TextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = inputParent,
                        onValueChange = { inputParent = it },
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

                    Text("Contact Phone Number", color = TextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userName = inputUser
                        parentName = inputParent
                        parentPhone = inputPhone

                        // Permanently persist to SharedPreferences
                        com.example.myapplication.util.UserProfileManager.saveFullProfile(
                            context = context,
                            userName = inputUser,
                            userPhone = null,
                            parentName = inputParent,
                            parentPhone = inputPhone
                        )

                        // Also add/update as emergency contact in list
                        onAddContact(inputParent, inputPhone, true)

                        showProfileDialog = false
                        Toast.makeText(context, "✅ Profile & Emergency Contact Saved Permanently", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Save Profile", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
