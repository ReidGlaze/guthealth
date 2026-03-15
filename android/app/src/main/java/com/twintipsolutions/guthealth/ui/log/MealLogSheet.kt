package com.twintipsolutions.guthealth.ui.log

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.viewinterop.AndroidView
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.AiAnalysis
import com.twintipsolutions.guthealth.data.FodmapRepository
import com.twintipsolutions.guthealth.data.models.Food
import com.twintipsolutions.guthealth.data.models.Meal
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun mealTypeForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> "breakfast"
        in 11..14 -> "lunch"
        in 15..20 -> "dinner"
        else -> "snack"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onError: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val firestoreService = remember { FirestoreService() }
    val fodmapRepository = remember { FodmapRepository.getInstance(context) }

    var selectedMealType by remember { mutableStateOf(mealTypeForCurrentTime()) }
    var selectedDateTime by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var manualFoodEntry by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    var capturedImageBase64 by remember { mutableStateOf<String?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

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
                        // Copy to cache file so Coil can read it reliably after picker URI expires
                        val cacheFile = java.io.File(context.cacheDir, "meal_photo_${System.currentTimeMillis()}.jpg")
                        cacheFile.writeBytes(bytes)
                        capturedImageUri = Uri.fromFile(cacheFile)
                        // Encode base64 for AI upload
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, baos)
                            capturedImageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                            bitmap.recycle()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MealLogSheet", "Error processing selected photo", e)
            }
        }
    }
    var analyzedFoods by remember { mutableStateOf<List<Food>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var analysisError by remember { mutableStateOf<String?>(null) }
    var manualFoods by remember { mutableStateOf<List<Food>>(emptyList()) }

    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")

    // Auto-analyze when photo is captured or selected
    LaunchedEffect(capturedImageBase64) {
        val base64 = capturedImageBase64
        if (base64 != null && analyzedFoods.isEmpty() && !isAnalyzing) {
            isAnalyzing = true
            analysisError = null
            try {
                val result = firestoreService.analyzeFoodPhoto(base64, selectedMealType)
                @Suppress("UNCHECKED_CAST")
                val foodsList = result["foods"] as? List<Map<String, Any>> ?: emptyList()
                analyzedFoods = foodsList.map { foodMap ->
                    Food(
                        name = foodMap["name"] as? String ?: "",
                        fodmapLevel = foodMap["fodmapLevel"] as? String ?: "unknown",
                        fodmapCategories = (foodMap["fodmapCategories"] as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        servingSize = foodMap["servingSize"] as? String ?: "",
                        triggers = (foodMap["triggers"] as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList()
                    )
                }
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } catch (e: Exception) {
                android.util.Log.e("MealLogSheet", "AI analysis failed", e)
                analysisError = "AI analysis unavailable. You can still add foods manually."
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } finally {
                isAnalyzing = false
            }
        }
    }

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { base64, fileUri ->
                capturedImageBase64 = base64
                capturedImageUri = fileUri
                showCamera = false
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
                text = "Log Meal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Meal type chips
        Column {
            Text(
                text = "Meal Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealTypes.forEach { type ->
                    FilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Date/time picker
        Column {
            Text(
                text = "When did you eat this?",
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

        // Camera / Photo section
        Column {
            Text(
                text = "Food Photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (capturedImageUri != null || capturedImageBase64 != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = capturedImageUri,
                        contentDescription = "Food photo",
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
                                analyzedFoods = emptyList()
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
                                analyzedFoods = emptyList()
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
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Library")
                        }
                    }
                    Text(
                        text = "AI will identify foods and FODMAP levels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Analyzing indicator
        if (isAnalyzing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TealPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing...", color = TealPrimary)
            }
        }
        if (analysisError != null) {
            Text(
                text = analysisError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // AI-analyzed foods list
        if (analyzedFoods.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Foods (${analyzedFoods.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "This is not medical advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                analyzedFoods.forEachIndexed { index, food ->
                    FoodResultCard(
                        food = food,
                        safeFodmapServing = fodmapRepository.lookup(food.name)?.lowFodmapServing,
                        onRemove = { analyzedFoods = analyzedFoods.toMutableList().also { it.removeAt(index) } }
                    )
                }
            }
        }

        // Manual food entry
        Column {
            Text(
                text = "Add Food Manually",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualFoodEntry,
                    onValueChange = { manualFoodEntry = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g., Grilled chicken") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val entry = manualFoodEntry.trim()
                        if (entry.isNotBlank()) {
                            val newFood = try {
                                val match = fodmapRepository.lookup(entry)
                                if (match != null) {
                                    Food(
                                        name = entry,
                                        fodmapLevel = match.fodmapLevel,
                                        fodmapCategories = match.fodmapCategories,
                                        servingSize = match.servingSize,
                                        triggers = match.fodmapCategories
                                    )
                                } else {
                                    Food(name = entry, fodmapLevel = "unknown")
                                }
                            } catch (e: Exception) {
                                Log.w("MealLogSheet", "FODMAP lookup failed", e)
                                Food(name = entry, fodmapLevel = "unknown")
                            }
                            manualFoods = manualFoods + newFood
                            manualFoodEntry = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Add")
                }
            }
        }

        // Manual foods list
        if (manualFoods.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                manualFoods.forEachIndexed { index, food ->
                    FoodResultCard(
                        food = food,
                        safeFodmapServing = fodmapRepository.lookup(food.name)?.lowFodmapServing,
                        onRemove = { manualFoods = manualFoods.toMutableList().also { it.removeAt(index) } }
                    )
                }
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
                        val allFoods = analyzedFoods + manualFoods
                        val meal = Meal(
                            createdAt = Timestamp(selectedDateTime),
                            mealType = selectedMealType,
                            foods = allFoods,
                            notes = notes,
                            aiAnalysis = if (analyzedFoods.isNotEmpty()) AiAnalysis(
                                analyzedAt = Timestamp.now()
                            ) else null
                        )
                        val mealId = firestoreService.saveMeal(meal)
                        val base64ToUpload = capturedImageBase64
                        if (base64ToUpload != null) {
                            val uid = firestoreService.currentUserId ?: ""
                            scope.launch {
                                try {
                                    val photoUrl = firestoreService.uploadMealPhoto(uid, mealId, base64ToUpload)
                                    firestoreService.updateMealPhotoUrl(mealId, photoUrl)
                                } catch (_: Exception) { }
                            }
                        }
                        onSaved()
                    } catch (e: Exception) {
                        onError()
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            enabled = !isSaving && (analyzedFoods.isNotEmpty() || manualFoods.isNotEmpty() || capturedImageBase64 != null || capturedImageUri != null)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Meal", modifier = Modifier.padding(vertical = 4.dp))
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

@Composable
fun FoodResultCard(food: Food, safeFodmapServing: String? = null, onRemove: (() -> Unit)? = null) {
    val levelColor = when (food.fodmapLevel) {
        "low" -> GreenSecondary
        "moderate" -> Color(0xFFFFB74D)
        "high" -> Color(0xFFE57373)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(levelColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (food.fodmapCategories.isNotEmpty()) {
                        Text(
                            text = food.fodmapCategories.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (food.servingSize.isNotEmpty()) {
                        Text(
                            text = food.servingSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // FODMAP level pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = food.fodmapLevel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (onRemove != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            // Serving size warning: only show when the estimated serving actually differs from safe serving
            val estimatedServing = food.servingSize.trim()
            val safeServing = safeFodmapServing?.trim()
            val servingsAreDifferent = !safeServing.isNullOrBlank() &&
                estimatedServing.isNotBlank() &&
                !estimatedServing.equals(safeServing, ignoreCase = true)
            if (servingsAreDifferent && safeServing != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFB74D).copy(alpha = 0.1f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estimated: $estimatedServing | Safe FODMAP serving: $safeServing",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB74D)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraScreen(
    onImageCaptured: (String, Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) onClose()
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Failed to bind camera", e)
                        }
                    },
                    ContextCompat.getMainExecutor(ctx)
                )
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        // Capture button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, TealPrimary, CircleShape)
                .clickable {
                    val outputFile = File(
                        context.cacheDir,
                        "food_photo_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions =
                        ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val bytes = outputFile.readBytes()
                                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                val fileUri = Uri.fromFile(outputFile)
                                onImageCaptured(base64, fileUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onClose()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Capture",
                tint = TealPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
