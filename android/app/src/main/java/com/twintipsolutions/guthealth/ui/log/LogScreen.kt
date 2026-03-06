package com.twintipsolutions.guthealth.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants
import com.twintipsolutions.guthealth.ui.theme.TealPrimary

private enum class ActiveSheet {
    NONE, MEAL, SYMPTOM, POOP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen() {
    val view = LocalView.current
    var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            if (activeSheet == ActiveSheet.NONE) {
                TopAppBar(
                    title = { Text("Log") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (activeSheet) {
            ActiveSheet.MEAL -> {
                Box(modifier = Modifier.padding(padding)) {
                    MealLogSheet(
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onSaved = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            activeSheet = ActiveSheet.NONE
                            snackbarMessage = "Meal saved!"
                        },
                        onError = {
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                            snackbarMessage = "Failed to save. Please check your connection."
                        }
                    )
                }
            }
            ActiveSheet.SYMPTOM -> {
                Box(modifier = Modifier.padding(padding)) {
                    SymptomLogSheet(
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onSaved = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            activeSheet = ActiveSheet.NONE
                            snackbarMessage = "Symptom logged!"
                        },
                        onError = {
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                            snackbarMessage = "Failed to save. Please check your connection."
                        }
                    )
                }
            }
            ActiveSheet.POOP -> {
                Box(modifier = Modifier.padding(padding)) {
                    PoopLogSheet(
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onSaved = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            activeSheet = ActiveSheet.NONE
                            snackbarMessage = "Poop log saved!"
                        },
                        onError = {
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                            snackbarMessage = "Failed to save. Please check your connection."
                        }
                    )
                }
            }
            ActiveSheet.NONE -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "What would you like to log?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Single card with three rows separated by dividers — matches iOS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            LogOptionRow(
                                icon = Icons.Filled.CameraAlt,
                                iconBackgroundColor = TealPrimary.copy(alpha = 0.12f),
                                iconTint = TealPrimary,
                                title = "Meal",
                                subtitle = "Snap a photo or manually enter foods",
                                onClick = { activeSheet = ActiveSheet.MEAL }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            LogOptionRow(
                                icon = Icons.Filled.EditNote,
                                iconBackgroundColor = Color(0xFFFFF3E0),
                                iconTint = Color(0xFFFF9800),
                                title = "Symptom",
                                subtitle = "Track bloating, pain, gas, and more",
                                onClick = { activeSheet = ActiveSheet.SYMPTOM }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            LogOptionRow(
                                icon = Icons.Filled.WaterDrop,
                                iconBackgroundColor = Color(0xFFFFF8E1),
                                iconTint = Color(0xFFFFB300),
                                title = "Poop",
                                subtitle = "Bristol Stool Chart classification",
                                onClick = { activeSheet = ActiveSheet.POOP }
                            )
                        }
                    }

                    Text(
                        text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogOptionRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
