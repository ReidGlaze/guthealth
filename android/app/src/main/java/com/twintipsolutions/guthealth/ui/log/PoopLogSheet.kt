package com.twintipsolutions.guthealth.ui.log

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.PoopLog
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class BristolType(
    val type: Int,
    val name: String,
    val description: String,
    val emoji: String
)

private val bristolTypes = listOf(
    BristolType(1, "Type 1", "Separate hard lumps", "\uD83E\uDED8"),
    BristolType(2, "Type 2", "Lumpy, sausage-shaped", "\uD83C\uDF30"),
    BristolType(3, "Type 3", "Sausage with cracks", "\uD83E\uDD56"),
    BristolType(4, "Type 4", "Smooth, soft sausage", "\uD83C\uDF4C"),
    BristolType(5, "Type 5", "Soft blobs with clear edges", "\uD83E\uDEE7"),
    BristolType(6, "Type 6", "Fluffy, mushy pieces", "\u2601\uFE0F"),
    BristolType(7, "Type 7", "Watery, no solid pieces", "\uD83D\uDCA7")
)

private fun bristolTypeColor(type: Int): androidx.compose.ui.graphics.Color = when (type) {
    1, 2 -> androidx.compose.ui.graphics.Color(0xFF8D6E63)  // amber/brown — constipation range
    3, 4 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)  // green — normal
    5 -> androidx.compose.ui.graphics.Color(0xFFFFB74D)     // yellow — borderline
    6, 7 -> androidx.compose.ui.graphics.Color(0xFFE57373)  // orange/red — diarrhea range
    else -> androidx.compose.ui.graphics.Color(0xFF2AA6A6)
}

private val colorOptions = listOf(
    "brown" to Color(0xFF8D6E63),
    "dark" to Color(0xFF4E342E),
    "light" to Color(0xFFD7CCC8),
    "green" to Color(0xFF66BB6A),
    "yellow" to Color(0xFFFFCA28),
    "red" to Color(0xFFEF5350),
    "black" to Color(0xFF212121)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoopLogSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onError: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val firestoreService = remember { FirestoreService() }

    var selectedBristol by remember { mutableStateOf<Int?>(null) }
    var selectedDateTime by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var capturedImageBase64 by remember { mutableStateOf<String?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isClassifying by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf<String?>(null) }

    val urgencyOptions = listOf("normal", "urgent", "emergency")

    fun classifyWithAI(base64: String) {
        isClassifying = true
        classificationResult = null
        scope.launch {
            try {
                val result = firestoreService.classifyPoopPhoto(base64)
                val bristolResult = (result["bristolType"] as? Number)?.toInt()
                if (bristolResult != null && bristolResult in 1..7) {
                    selectedBristol = bristolResult
                }
                val validColors = colorOptions.map { it.first }
                val colorResult = (result["color"] as? String)?.lowercase()?.trim()
                if (colorResult != null && colorResult in validColors) {
                    selectedColor = colorResult
                }
                val observations = (result["observations"] as? String)?.trim()
                if (!observations.isNullOrEmpty()) {
                    notes = observations
                }
                val bristolLabel = if (bristolResult != null && bristolResult in 1..7) "Type $bristolResult" else "unknown"
                val colorLabel = if (colorResult != null && colorResult in validColors) colorResult else "unknown"
                classificationResult = "AI classified as $bristolLabel, color: $colorLabel"
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } catch (_: Exception) {
                classificationResult = "AI classification unavailable. Please select manually."
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } finally {
                isClassifying = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    if (bytes.isNotEmpty()) {
                        val cacheFile = java.io.File(context.cacheDir, "poop_photo_${System.currentTimeMillis()}.jpg")
                        cacheFile.writeBytes(bytes)
                        capturedImageUri = Uri.fromFile(cacheFile)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            try {
                                java.io.ByteArrayOutputStream().use { baos ->
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, baos)
                                    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                                    capturedImageBase64 = base64
                                    classifyWithAI(base64)
                                }
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    }
                } else {
                    Log.e("PoopLogSheet", "Could not open input stream for URI: $uri")
                }
            } catch (e: Exception) {
                Log.e("PoopLogSheet", "Error processing selected photo", e)
            }
        }
    }

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { base64, fileUri ->
                capturedImageBase64 = base64
                capturedImageUri = fileUri
                showCamera = false
                classifyWithAI(base64)
            },
            onClose = { showCamera = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Log Poop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Photo section — camera/library buttons always visible at top
        Column {
            Text(
                text = "Poop Photo (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (capturedImageUri != null || capturedImageBase64 != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = capturedImageUri,
                        contentDescription = "Poop photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedImageBase64 = null
                                capturedImageUri = null
                                classificationResult = null
                                showCamera = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retake")
                        }
                        OutlinedButton(
                            onClick = {
                                capturedImageBase64 = null
                                capturedImageUri = null
                                classificationResult = null
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Library")
                        }
                    }
                    if (isClassifying) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = TealPrimary
                            )
                            Text(
                                "Classifying with AI...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (classificationResult != null) {
                        Text(
                            text = classificationResult!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary
                        )
                        Text(
                            text = "This is not medical advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCamera = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Library")
                        }
                    }
                    Text(
                        text = "AI will classify using the Bristol Stool Chart",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Date/time picker — always visible
        Column {
            Text(
                text = "When did this happen?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
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

        // Bristol Stool Chart — always visible
        Column {
            Text(
                text = "Bristol Stool Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                bristolTypes.forEach { bristol ->
                    val isSelected = selectedBristol != null && selectedBristol == bristol.type
                    val typeColor = bristolTypeColor(bristol.type)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBristol = bristol.type },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) typeColor.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, typeColor) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bristol.emoji,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bristol.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = bristol.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Color selector — 4-column grid layout
        Column {
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 4-column grid: rows of 4
            val rows = colorOptions.chunked(4)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { (colorName, colorValue) ->
                            val isSelected = selectedColor != null && selectedColor == colorName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedColor = colorName }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(colorValue)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, TealPrimary, CircleShape)
                                            else Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = colorName.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        // Fill remaining columns in last row with spacers
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Red/black stool safety warning — only shown when red or black is actively selected
        if (selectedColor == "red" || selectedColor == "black") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE65100))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Red or black stool can indicate bleeding. If you haven't eaten foods that could cause this color (beets, iron supplements, etc.), please seek medical attention.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBF360C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Urgency picker — always visible
        Column {
            Text(
                text = "Urgency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                urgencyOptions.forEach { urgency ->
                    val urgencyColor = when (urgency) {
                        "normal" -> TealPrimary
                        "urgent" -> Color(0xFFFFB74D)
                        "emergency" -> Color(0xFFE57373)
                        else -> TealPrimary
                    }
                    val urgencyIcon = when (urgency) {
                        "normal" -> Icons.Default.CheckCircle
                        "urgent" -> Icons.Default.Warning
                        "emergency" -> Icons.Default.Error
                        else -> Icons.Default.CheckCircle
                    }
                    FilterChip(
                        selected = selectedUrgency != null && selectedUrgency == urgency,
                        onClick = { selectedUrgency = if (selectedUrgency == urgency) null else urgency },
                        label = { Text(urgency.replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            Icon(
                                imageVector = urgencyIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedUrgency == urgency) Color.White else urgencyColor
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = urgencyColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Notes field — always visible
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Any additional details...") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        // Save button — requires Bristol type and color to both be selected
        val canSave = selectedBristol != null && selectedColor != null
        Button(
            onClick = {
                val bristol = selectedBristol ?: return@Button
                val color = selectedColor ?: return@Button
                isSaving = true
                scope.launch {
                    try {
                        val poopLog = PoopLog(
                            createdAt = Timestamp(selectedDateTime),
                            bristolType = bristol,
                            color = color,
                            urgency = selectedUrgency ?: "normal",
                            notes = notes
                        )
                        val logId = firestoreService.savePoopLog(poopLog)
                        val base64ToUpload = capturedImageBase64
                        if (base64ToUpload != null) {
                            val uid = firestoreService.currentUserId ?: ""
                            scope.launch {
                                try {
                                    val photoUrl = firestoreService.uploadPoopPhoto(uid, logId, base64ToUpload)
                                    firestoreService.updatePoopLogPhotoUrl(logId, photoUrl)
                                } catch (_: Exception) { }
                            }
                        }
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
            enabled = !isSaving && canSave
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Poop Log", modifier = Modifier.padding(vertical = 4.dp))
        }

        // Disclaimer
        Text(
            text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
                        val picked = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
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
