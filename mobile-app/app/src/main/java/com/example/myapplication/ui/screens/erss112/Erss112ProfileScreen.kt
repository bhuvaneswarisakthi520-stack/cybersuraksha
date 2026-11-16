package com.example.myapplication.ui.screens.erss112

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 112 India Profile Screen matching prototype mockup.
 *
 * Displays user identity, demographics,
 * Emergency Contact, Volunteer status, Language, Notification settings, and Edit Profile functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Erss112ProfileScreen(
    initialName: String = "User",
    initialPhone: String = "+91 9XXXXXXXXX",
    initialState: String = "State",
    initialDob: String = "DD-MM-YYYY",
    initialGender: String = "Not Specified",
    initialAboutMe: String = "Student",
    initialEContact: String = "Nil",
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getUserName(context)) }
    var phone by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getUserPhone(context)) }
    var state by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getState(context)) }
    var dob by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getDob(context)) }
    var gender by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getGender(context)) }
    var aboutMe by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getAboutMe(context)) }
    var eContact by remember { mutableStateOf(com.example.myapplication.util.UserProfileManager.getEContact(context)) }

    var isVolunteer by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("English") }
    var notificationEnabled by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf(false) }

    val maroonTheme = Color(0xFFB71C1C)
    val cardBg = Color.White
    val textDark = Color(0xFF212121)
    val textGrey = Color(0xFF757575)
    val bgScreen = Color(0xFFFBF1F1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = maroonTheme),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Edit Profile",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonTheme)
            )
        },
        containerColor = bgScreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ─────────────────────────────────────────────────────────────
            // 1. TOP USER CARD (Avatar, Name, Phone)
            // ─────────────────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFCDD2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = maroonTheme,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = name,
                            color = textDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = phone,
                            color = textGrey,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 2. DEMOGRAPHICS & PROFILE DETAILS CARD
            // ─────────────────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // State
                    ProfileDetailItem(
                        icon = Icons.Default.LocationOn,
                        label = "State",
                        value = state
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 10.dp))

                    // Date of Birth
                    ProfileDetailItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Date of Birth",
                        value = dob
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 10.dp))

                    // Gender
                    ProfileDetailItem(
                        icon = Icons.Default.Wc,
                        label = "Gender",
                        value = gender
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 10.dp))

                    // About Me
                    ProfileDetailItem(
                        icon = Icons.Default.Accessibility,
                        label = "About Me",
                        value = aboutMe
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 10.dp))

                    // E-Contact with red edit pencil button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = textGrey, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("E-Contact", color = textGrey, fontSize = 12.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(eContact, color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(maroonTheme)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Contact", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 3. SETTINGS & PREFERENCES CARD
            // ─────────────────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Are you a Volunteer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Are you a Volunteer", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Checkbox(
                            checked = isVolunteer,
                            onCheckedChange = { isVolunteer = it },
                            colors = CheckboxDefaults.colors(checkedColor = maroonTheme)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                    // Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Language", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(language, color = maroonTheme, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                    // Notification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notification", color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Checkbox(
                            checked = notificationEnabled,
                            onCheckedChange = { notificationEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = maroonTheme)
                        )
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 4. LOGOUT BUTTON
            // ─────────────────────────────────────────────────────────────
            Button(
                onClick = {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = maroonTheme),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // ─────────────────────────────────────────────────────────────
            // 5. FOOTER
            // ─────────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("v6.2.0", color = textGrey, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Powered by C-DAC", color = textDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Thiruvananthapuram", color = textDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT PROFILE DIALOG
    // ─────────────────────────────────────────────────────────────
    if (showEditDialog) {
        var editName by remember { mutableStateOf(name) }
        var editPhone by remember { mutableStateOf(phone) }
        var editState by remember { mutableStateOf(state) }
        var editDob by remember { mutableStateOf(dob) }
        var editGender by remember { mutableStateOf(gender) }
        var editAboutMe by remember { mutableStateOf(aboutMe) }
        var editEContact by remember { mutableStateOf(eContact) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color.White,
            title = {
                Text("Edit Profile Details", color = maroonTheme, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editState,
                        onValueChange = { editState = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDob,
                        onValueChange = { editDob = it },
                        label = { Text("Date of Birth (DD-MM-YYYY)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGender,
                        onValueChange = { editGender = it },
                        label = { Text("Gender") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAboutMe,
                        onValueChange = { editAboutMe = it },
                        label = { Text("About Me") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEContact,
                        onValueChange = { editEContact = it },
                        label = { Text("Emergency Contact (Phone)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        name = editName
                        phone = editPhone
                        state = editState
                        dob = editDob
                        gender = editGender
                        aboutMe = editAboutMe
                        eContact = editEContact

                        // Permanently persist to SharedPreferences
                        com.example.myapplication.util.UserProfileManager.saveFullProfile(
                            context = context,
                            userName = editName,
                            userPhone = editPhone,
                            parentName = "Emergency Contact",
                            parentPhone = editEContact,
                            state = editState,
                            dob = editDob,
                            gender = editGender,
                            aboutMe = editAboutMe
                        )

                        showEditDialog = false
                        Toast.makeText(context, "✅ Profile Saved Permanently", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = maroonTheme)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = textGrey)
                }
            }
        )
    }
}

@Composable
private fun ProfileDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = Color(0xFF757575), fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color(0xFF212121), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
