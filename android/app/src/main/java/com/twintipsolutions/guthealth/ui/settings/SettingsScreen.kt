package com.twintipsolutions.guthealth.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firestoreService = remember { FirestoreService() }

    var fodmapPhase by remember { mutableStateOf("elimination") }
    var morningEnabled by remember { mutableStateOf(true) }
    var lunchEnabled by remember { mutableStateOf(true) }
    var dinnerEnabled by remember { mutableStateOf(true) }
    var morningTime by remember { mutableStateOf("08:00") }
    var lunchTime by remember { mutableStateOf("12:00") }
    var dinnerTime by remember { mutableStateOf("18:00") }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        scope.launch {
            try {
                firestoreService.updateUserProfile(
                    mapOf("preferences.notificationsEnabled" to granted)
                )
            } catch (_: Exception) {}
        }
    }

    // Load profile
    LaunchedEffect(Unit) {
        try {
            firestoreService.signInAnonymously()
            val profile = firestoreService.getUserProfile()
            if (profile != null) {
                fodmapPhase = profile.fodmapPhase
                notificationsEnabled = profile.preferences.notificationsEnabled
                val times = profile.preferences.reminderTimes
                morningEnabled = times.any { it.startsWith("0") || it.startsWith("10") }
                lunchEnabled = times.any { it.startsWith("11") || it.startsWith("12") || it.startsWith("13") }
                dinnerEnabled = times.any { it.startsWith("1") && !it.startsWith("10") && !it.startsWith("11") && !it.startsWith("12") && !it.startsWith("13") || it.startsWith("2") }
                for (time in times) {
                    val hour = time.substringBefore(":").toIntOrNull() ?: continue
                    when {
                        hour < 11 -> { morningTime = time; morningEnabled = true }
                        hour < 15 -> { lunchTime = time; lunchEnabled = true }
                        else -> { dinnerTime = time; dinnerEnabled = true }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FODMAP Phase Section
            SettingsSectionHeader("FODMAP Phase")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val phases = listOf("elimination", "reintroduction", "maintenance")
                    phases.forEach { phase ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = fodmapPhase == phase,
                                onClick = {
                                    fodmapPhase = phase
                                    scope.launch {
                                        try {
                                            firestoreService.updateFodmapPhase(phase)
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = phase.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = phaseDescription(phase),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Reminder Times Section
            SettingsSectionHeader("Reminder Times")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    fun saveReminders() {
                        scope.launch {
                            try {
                                val times = mutableListOf<String>()
                                if (morningEnabled) times.add(morningTime)
                                if (lunchEnabled) times.add(lunchTime)
                                if (dinnerEnabled) times.add(dinnerTime)
                                firestoreService.updateUserProfile(
                                    mapOf("preferences.reminderTimes" to times)
                                )
                            } catch (_: Exception) {}
                        }
                    }

                    ReminderRow(
                        label = "Morning",
                        icon = Icons.Default.WbSunny,
                        time = morningTime,
                        enabled = morningEnabled,
                        onToggle = { morningEnabled = it; saveReminders() },
                        onTimeClick = { showTimePicker = "morning" }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ReminderRow(
                        label = "Lunch",
                        icon = Icons.Default.LunchDining,
                        time = lunchTime,
                        enabled = lunchEnabled,
                        onToggle = { lunchEnabled = it; saveReminders() },
                        onTimeClick = { showTimePicker = "lunch" }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ReminderRow(
                        label = "Dinner",
                        icon = Icons.Default.DinnerDining,
                        time = dinnerTime,
                        enabled = dinnerEnabled,
                        onToggle = { dinnerEnabled = it; saveReminders() },
                        onTimeClick = { showTimePicker = "dinner" }
                    )
                }
            }
            Text(
                text = "Get reminders to log your meals at these times.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Notifications Section
            SettingsSectionHeader("Notifications")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Switch
                                }
                            }
                            notificationsEnabled = enabled
                            scope.launch {
                                try {
                                    firestoreService.updateUserProfile(
                                        mapOf("preferences.notificationsEnabled" to enabled)
                                    )
                                } catch (_: Exception) {}
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                    )
                }
            }

            // App Info Section
            SettingsSectionHeader("App Info")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = getAppVersion(context),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(onClick = { showAboutDialog = true }) {
                            Text("About AI Gut Health", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Medical Disclaimer Section
            SettingsSectionHeader("Medical Disclaimer")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Medical Disclaimer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI Gut Health & IBS Tracker is an educational wellness tool. It is not intended to provide medical advice, diagnosis, or treatment recommendations. Always consult a qualified healthcare professional before making dietary changes or medical decisions. The AI-generated content in this app is for informational purposes only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Account Section
            SettingsSectionHeader("Account")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Sign Out",
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Delete All Data",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Text(
                text = "Deleting data will permanently remove all your logged meals, symptoms, poop logs, and AI insights.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Time Picker Dialog
    showTimePicker?.let { which ->
        val currentTime = when (which) {
            "morning" -> morningTime
            "lunch" -> lunchTime
            else -> dinnerTime
        }
        val hour = currentTime.substringBefore(":").toIntOrNull() ?: 8
        val minute = currentTime.substringAfter(":").toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)

        AlertDialog(
            onDismissRequest = { showTimePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    when (which) {
                        "morning" -> morningTime = newTime
                        "lunch" -> lunchTime = newTime
                        else -> dinnerTime = newTime
                    }
                    scope.launch {
                        try {
                            val times = mutableListOf<String>()
                            if (morningEnabled) times.add(if (which == "morning") newTime else morningTime)
                            if (lunchEnabled) times.add(if (which == "lunch") newTime else lunchTime)
                            if (dinnerEnabled) times.add(if (which == "dinner") newTime else dinnerTime)
                            firestoreService.updateUserProfile(
                                mapOf("preferences.reminderTimes" to times)
                            )
                        } catch (_: Exception) {}
                    }
                    showTimePicker = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = null }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete All Data?") },
            text = {
                Text("This will permanently delete all your meals, symptoms, poop logs, and insights. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                firestoreService.deleteUserData()
                                firestoreService.deleteAccount()
                            } catch (_: Exception) {}
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?") },
            text = { Text("You will need to sign in again to access your data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        firestoreService.signOut()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About AI Gut Health & IBS Tracker") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "AI Gut Health & IBS Tracker is an educational wellness tool developed by Twin Tip Solutions LLC. It helps you track your diet, symptoms, and digestive health patterns using AI-powered food analysis and correlation detection.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Medical Disclaimer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This application is designed as an educational wellness tool and is NOT intended to provide medical advice or diagnosis, replace consultation with healthcare professionals, serve as a substitute for professional medical treatment, or make claims about curing or treating any medical condition.\n\nThe AI-generated content, including food analysis, symptom correlations, and weekly insights, is for informational and educational purposes only.\n\nAlways consult with a qualified healthcare professional before starting an elimination diet or making significant dietary changes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Privacy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This app uses anonymous authentication. We do not collect personally identifiable information. Your health data is stored securely in Firebase and can be deleted at any time from Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun ReminderRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (enabled) {
                TextButton(
                    onClick = onTimeClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = formatDisplayTime(time),
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary
                    )
                }
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
        )
    }
}

private fun phaseDescription(phase: String): String = when (phase) {
    "elimination" -> "Remove high-FODMAP foods for 2-6 weeks"
    "reintroduction" -> "Reintroduce FODMAP groups one at a time"
    "maintenance" -> "Personalized diet based on your triggers"
    else -> ""
}

private fun formatDisplayTime(time: String): String {
    val hour = time.substringBefore(":").toIntOrNull() ?: return time
    val minute = time.substringAfter(":").toIntOrNull() ?: return time
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, amPm)
}

private fun getAppVersion(context: android.content.Context): String {
    return try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName} (${info.longVersionCode})"
    } catch (_: Exception) {
        "1.0.0"
    }
}
