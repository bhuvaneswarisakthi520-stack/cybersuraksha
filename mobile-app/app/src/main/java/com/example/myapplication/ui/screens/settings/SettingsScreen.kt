package com.example.myapplication.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    volumeTriggerEnabled: Boolean = false,
    onVolumeTriggerChanged: (Boolean) -> Unit = {},
    shakeTriggerEnabled: Boolean = false,
    onShakeTriggerChanged: (Boolean) -> Unit = {},
    silentModeEnabled: Boolean = true,
    onSilentModeChanged: (Boolean) -> Unit = {},
    autoRecordEnabled: Boolean = true,
    onAutoRecordChanged: (Boolean) -> Unit = {},
    encryptEvidenceEnabled: Boolean = true,
    onEncryptEvidenceChanged: (Boolean) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { SettingsSectionHeader("Trigger Settings") }
            item {
                SettingSwitchItem(
                    headline = "Volume Button Pattern",
                    supportingText = "Press volume up 3 times to activate",
                    checked = volumeTriggerEnabled,
                    onCheckedChange = onVolumeTriggerChanged
                )
            }
            item {
                SettingSwitchItem(
                    headline = "Shake Detection",
                    supportingText = "Shake phone vigorously to activate",
                    checked = shakeTriggerEnabled,
                    onCheckedChange = onShakeTriggerChanged
                )
            }

            item { SettingsSectionHeader("Emergency Settings") }
            item {
                ListItem(
                    headlineContent = { Text("Auto-escalation Timer") },
                    supportingContent = { Text("30 seconds") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                SettingSwitchItem(
                    headline = "Silent Mode",
                    supportingText = "No sound/vibration during emergency",
                    checked = silentModeEnabled,
                    onCheckedChange = onSilentModeChanged
                )
            }

            item { SettingsSectionHeader("Evidence") }
            item {
                SettingSwitchItem(
                    headline = "Auto-record Audio",
                    supportingText = "Start recording when SOS is triggered",
                    checked = autoRecordEnabled,
                    onCheckedChange = onAutoRecordChanged
                )
            }
            item {
                SettingSwitchItem(
                    headline = "Encrypt Evidence",
                    supportingText = "Secure locally stored evidence",
                    checked = encryptEvidenceEnabled,
                    onCheckedChange = onEncryptEvidenceChanged
                )
            }

            item { SettingsSectionHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("App Version") },
                    supportingContent = { Text("1.0.0") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Privacy Policy") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingSwitchItem(
    headline: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(headline) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    )
}
