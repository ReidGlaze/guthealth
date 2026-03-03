package com.twintipsolutions.guthealth.ui.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.Symptom
import com.twintipsolutions.guthealth.ui.theme.ErrorRed
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.GreenSecondaryDark
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import com.twintipsolutions.guthealth.ui.theme.WarningOrange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SymptomLogSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onError: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService() }

    var selectedType by remember { mutableStateOf("bloating") }
    var selectedDateTime by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var severity by remember { mutableFloatStateOf(5f) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val symptomTypes = listOf(
        "bloating", "gas", "pain", "heartburn",
        "nausea", "diarrhea", "constipation", "cramping"
    )
    val symptomIcons = mapOf(
        "bloating" to "\uD83C\uDF88",
        "gas" to "\uD83D\uDCA8",
        "pain" to "\u26A1",
        "heartburn" to "\uD83D\uDD25",
        "nausea" to "\uD83E\uDD22",
        "diarrhea" to "\uD83D\uDCA7",
        "constipation" to "\uD83D\uDEAB",
        "cramping" to "\u3030\uFE0F"
    )
    val locations = listOf("upper", "lower", "left", "right")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Log Symptom",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Symptom type
        Text(
            text = "Symptom Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            symptomTypes.forEach { type ->
                val icon = symptomIcons[type] ?: ""
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = {
                        Text(
                            text = "$icon ${type.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Date/time picker
        Text(
            text = "When did this start?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        run {
            val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
            val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
            val cal = remember(selectedDateTime) { Calendar.getInstance().apply { time = selectedDateTime } }
            val isToday = remember(selectedDateTime) {
                val today = Calendar.getInstance()
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            val displayDate = if (isToday) "Today" else dateFormat.format(selectedDateTime)
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$displayDate at ${timeFormat.format(selectedDateTime)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = TealPrimary
                    )
                }
            }
        }

        // Severity slider
        Text(
            text = "Severity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        val severityColor = when {
            severity <= 3 -> GreenSecondaryDark
            severity <= 6 -> WarningOrange
            else -> ErrorRed
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = severityLabel(severity.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = severityColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${severity.toInt()}/10",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
            }
            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = severityColor,
                    activeTrackColor = severityColor
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mild", style = MaterialTheme.typography.labelSmall, color = GreenSecondaryDark)
                Text("Severe", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
            }
        }

        // Location
        Text(
            text = "Location (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            locations.forEach { location ->
                FilterChip(
                    selected = selectedLocation == location,
                    onClick = {
                        selectedLocation = if (selectedLocation == location) null else location
                    },
                    label = { Text(location.replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Notes (optional)") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        // Save button
        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    try {
                        val symptom = Symptom(
                            createdAt = Timestamp(selectedDateTime),
                            type = selectedType,
                            severity = severity.toInt(),
                            location = selectedLocation,
                            notes = notes,
                            endedAt = null
                        )
                        firestoreService.saveSymptom(symptom)
                        onSaved()
                    } catch (_: Exception) {
                        onError()
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Symptom", modifier = Modifier.padding(vertical = 4.dp))
        }

        // Disclaimer
        Text(
            text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val cal = Calendar.getInstance().apply { time = selectedDateTime }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = cal.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Calendar.getInstance().apply { timeInMillis = millis }
                        val current = Calendar.getInstance().apply { time = selectedDateTime }
                        current.set(Calendar.YEAR, picked.get(Calendar.YEAR))
                        current.set(Calendar.MONTH, picked.get(Calendar.MONTH))
                        current.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
                        selectedDateTime = current.time
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = selectedDateTime }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val updated = Calendar.getInstance().apply { time = selectedDateTime }
                    updated.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    updated.set(Calendar.MINUTE, timePickerState.minute)
                    updated.set(Calendar.SECOND, 0)
                    selectedDateTime = updated.time
                    showTimePicker = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) }
        )
    }

}

private fun severityLabel(severity: Int): String = when (severity) {
    1, 2 -> "Mild"
    3, 4 -> "Mild-Moderate"
    5, 6 -> "Moderate"
    7, 8 -> "Moderate-Severe"
    9, 10 -> "Severe"
    else -> "Moderate"
}
