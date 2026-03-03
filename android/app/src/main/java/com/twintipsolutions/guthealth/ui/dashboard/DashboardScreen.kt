package com.twintipsolutions.guthealth.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.Meal
import com.twintipsolutions.guthealth.data.models.PoopLog
import com.twintipsolutions.guthealth.data.models.Symptom
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// Screen root
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onSettingsClick: () -> Unit = {}, onSeeAllHistory: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService() }

    // Date navigation state — 0 means today, -1 yesterday, etc.
    var dayOffset by remember { mutableIntStateOf(0) }
    val isToday = dayOffset == 0

    // Data for the selected day
    var meals by remember { mutableStateOf<List<Pair<String, Meal>>>(emptyList()) }
    var symptoms by remember { mutableStateOf<List<Pair<String, Symptom>>>(emptyList()) }
    var poopLogs by remember { mutableStateOf<List<Pair<String, PoopLog>>>(emptyList()) }

    // Today-only data
    var streakDays by remember { mutableIntStateOf(0) }

    var isRefreshing by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var deleteConfirmType by remember { mutableStateOf("") }

    // Delete dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun dayBounds(offset: Int): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, offset)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time
        cal.add(Calendar.DATE, 1)
        val end = cal.time
        return start to end
    }

    fun loadData() {
        scope.launch {
            isRefreshing = true
            try {
                firestoreService.signInAnonymously()
                val (start, end) = dayBounds(dayOffset)
                meals = firestoreService.getMealsForDateRange(start, end)
                symptoms = firestoreService.getSymptomsForDateRange(start, end)
                poopLogs = firestoreService.getPoopLogsForDateRange(start, end)

                if (isToday) {
                    streakDays = computeStreak(firestoreService)
                }
            } catch (_: Exception) {
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(dayOffset) { loadData() }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; deleteConfirmId = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this entry? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deleteConfirmId
                        val type = deleteConfirmType
                        showDeleteDialog = false
                        deleteConfirmId = null
                        if (id != null) {
                            scope.launch {
                                try {
                                    when (type) {
                                        "meal" -> firestoreService.deleteMeal(id)
                                        "symptom" -> firestoreService.deleteSymptom(id)
                                        "poop" -> firestoreService.deletePoopLog(id)
                                    }
                                    loadData()
                                } catch (_: Exception) {}
                            }
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deleteConfirmId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date navigation bar
                DateNavigationBar(
                    dayOffset = dayOffset,
                    isLoading = isRefreshing,
                    onPrev = { dayOffset-- },
                    onNext = { if (dayOffset < 0) dayOffset++ }
                )

                // Today-only: streak + checklist
                if (isToday) {
                    StreakCard(streakDays = streakDays)
                    TodayChecklistCard(
                        mealCount = meals.size,
                        symptomCount = symptoms.size,
                        poopCount = poopLogs.size
                    )
                }

                // Meals section
                SectionHeader(
                    title = "Meals",
                    count = meals.size,
                    icon = Icons.Default.CameraAlt,
                    color = TealPrimary
                )
                if (meals.isEmpty()) {
                    EmptySectionNote("No meals logged for this day.")
                } else {
                    meals.forEach { (id, meal) ->
                        MealEntryCard(
                            id = id,
                            meal = meal,
                            onDelete = {
                                deleteConfirmId = id
                                deleteConfirmType = "meal"
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Symptoms section
                SectionHeader(
                    title = "Symptoms",
                    count = symptoms.size,
                    icon = Icons.Default.EditNote,
                    color = Color(0xFFFFB74D)
                )
                if (symptoms.isEmpty()) {
                    EmptySectionNote("No symptoms logged for this day.")
                } else {
                    symptoms.forEach { (id, symptom) ->
                        SymptomEntryCard(
                            id = id,
                            symptom = symptom,
                            onDelete = {
                                deleteConfirmId = id
                                deleteConfirmType = "symptom"
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Poop Logs section
                SectionHeader(
                    title = "Poop Logs",
                    count = poopLogs.size,
                    icon = Icons.Default.WaterDrop,
                    color = GreenSecondary
                )
                if (poopLogs.isEmpty()) {
                    EmptySectionNote("No poop logs for this day.")
                } else {
                    poopLogs.forEach { (id, log) ->
                        PoopEntryCard(
                            id = id,
                            log = log,
                            onDelete = {
                                deleteConfirmId = id
                                deleteConfirmType = "poop"
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Date Navigation Bar
// ---------------------------------------------------------------------------

@Composable
private fun DateNavigationBar(
    dayOffset: Int,
    isLoading: Boolean = false,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val isToday = dayOffset == 0
    val dateLabel = when {
        dayOffset == 0 -> "Today"
        dayOffset == -1 -> "Yesterday"
        else -> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, dayOffset)
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            sdf.format(cal.time)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous day",
                    tint = TealPrimary
                )
            }

            // Center: show date label with a small inline spinner beside it when loading
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (isLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TealPrimary,
                        strokeWidth = 2.dp
                    )
                }
            }

            IconButton(
                onClick = onNext,
                enabled = !isToday
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next day",
                    tint = if (isToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else TealPrimary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section Header
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(title: String, count: Int, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (count > 0) {
            Box(
                modifier = Modifier
                    .background(color, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptySectionNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

// ---------------------------------------------------------------------------
// Swipe-to-delete wrapper
// ---------------------------------------------------------------------------

@Composable
private fun SwipeToDeleteCard(onDelete: () -> Unit, content: @Composable () -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = 120f

    Box(modifier = Modifier.fillMaxWidth()) {
        // Delete background shown when swiped
        if (offsetX < -20f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp.coerceIn((-threshold * 1.2f).dp, 0.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -threshold) {
                                onDelete()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f }
                    ) { _, dragAmount ->
                        if (dragAmount < 0) {
                            offsetX = (offsetX + dragAmount).coerceIn(-threshold * 1.5f, 0f)
                        } else {
                            offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                        }
                    }
                }
        ) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Meal Entry Card
// ---------------------------------------------------------------------------

@Composable
private fun MealEntryCard(id: String, meal: Meal, onDelete: () -> Unit) {
    SwipeToDeleteCard(onDelete = onDelete) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Meal photo thumbnail (small, inline)
                if (!meal.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = meal.photoUrl,
                        contentDescription = "Meal photo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.mealType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary
                    )
                    if (meal.foods.isNotEmpty()) {
                        Text(
                            text = meal.foods.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // FODMAP chips
                        val highFoods = meal.foods.filter { it.fodmapLevel == "high" }
                        val modFoods = meal.foods.filter { it.fodmapLevel == "moderate" }
                        if (highFoods.isNotEmpty() || modFoods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (highFoods.isNotEmpty()) {
                                    FodmapChip(label = "High FODMAP", color = Color(0xFFE57373))
                                }
                                if (modFoods.isNotEmpty()) {
                                    FodmapChip(label = "Moderate", color = Color(0xFFFFB74D))
                                }
                            }
                        }
                    }
                    if (meal.notes.isNotEmpty()) {
                        Text(
                            text = meal.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                val timeText = meal.createdAt?.let {
                    SimpleDateFormat("h:mm a", Locale.US).format(Date(it.seconds * 1000))
                } ?: ""
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Symptom Entry Card
// ---------------------------------------------------------------------------

@Composable
private fun SymptomEntryCard(id: String, symptom: Symptom, onDelete: () -> Unit) {
    SwipeToDeleteCard(onDelete = onDelete) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = symptom.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB74D)
                    )
                    Text(
                        text = "Severity ${symptom.severity}/10${if (!symptom.location.isNullOrEmpty()) " \u2022 ${symptom.location}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (symptom.notes.isNotEmpty()) {
                        Text(
                            text = symptom.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                val timeText = symptom.createdAt?.let {
                    SimpleDateFormat("h:mm a", Locale.US).format(Date(it.seconds * 1000))
                } ?: ""
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Poop Entry Card (photo hidden by default)
// ---------------------------------------------------------------------------

@Composable
private fun PoopEntryCard(id: String, log: PoopLog, onDelete: () -> Unit) {
    var photoVisible by remember { mutableStateOf(false) }

    SwipeToDeleteCard(onDelete = onDelete) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = GreenSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bristol Type ${log.bristolType}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenSecondary
                        )
                        Text(
                            text = "${log.color.replaceFirstChar { it.uppercase() }} \u2022 ${log.urgency.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (log.notes.isNotEmpty()) {
                            Text(
                                text = log.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    val timeText = log.createdAt?.let {
                        SimpleDateFormat("h:mm a", Locale.US).format(Date(it.seconds * 1000))
                    } ?: ""
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Poop photo: hidden by default, tap to reveal
                if (!log.photoUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!photoVisible) {
                        OutlinedButton(
                            onClick = { photoVisible = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tap to view photo", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        AnimatedVisibility(visible = photoVisible) {
                            AsyncImage(
                                model = log.photoUrl,
                                contentDescription = "Poop photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { photoVisible = false },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FODMAP chip
// ---------------------------------------------------------------------------

@Composable
private fun FodmapChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private suspend fun computeStreak(firestoreService: FirestoreService): Int {
    var streak = 0
    val cal = Calendar.getInstance()

    for (daysBack in 0 until 30) {
        cal.time = Date()
        cal.add(Calendar.DATE, -daysBack)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.time
        cal.add(Calendar.DATE, 1)
        val dayEnd = cal.time

        try {
            val m = firestoreService.getMealsForDateRange(dayStart, dayEnd)
            val s = firestoreService.getSymptomsForDateRange(dayStart, dayEnd)
            val p = firestoreService.getPoopLogsForDateRange(dayStart, dayEnd)

            if (m.isNotEmpty() || s.isNotEmpty() || p.isNotEmpty()) {
                streak++
            } else {
                break
            }
        } catch (_: Exception) {
            break
        }
    }

    return streak
}

// ---------------------------------------------------------------------------
// Streak Card
// ---------------------------------------------------------------------------

@Composable
private fun StreakCard(streakDays: Int) {
    val streakMessage = when {
        streakDays == 0 -> "Start your streak! Log your first entry today."
        streakDays >= 21 -> "3-week streak! Great habits forming."
        streakDays >= 14 -> "2-week streak! Patterns are getting clearer."
        streakDays >= 7 -> "$streakDays-day streak! Your correlation engine is ready."
        else -> "Day $streakDays — ${7 - streakDays} more days until trigger analysis!"
    }
    val streakColor = if (streakDays >= 7) GreenSecondary else Color(0xFFFF9800)
    val nextMilestone = when {
        streakDays < 7 -> 7
        streakDays < 14 -> 14
        else -> 21
    }
    val progress = (streakDays.toFloat() / nextMilestone).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "\uD83D\uDD25", fontSize = 28.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logging Streak",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = streakMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = streakColor
                    )
                }
                Text(
                    text = "${streakDays}d",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = streakColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = streakColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Next milestone: Day $nextMilestone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "$streakDays/$nextMilestone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Today's Checklist
// ---------------------------------------------------------------------------

@Composable
private fun TodayChecklistCard(mealCount: Int, symptomCount: Int, poopCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Today's Checklist",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            ChecklistRow("Meal logged", mealCount > 0, Icons.Default.CameraAlt)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistRow("Symptoms checked", symptomCount > 0, Icons.Default.EditNote)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistRow("Poop tracked", poopCount > 0, Icons.Default.WaterDrop)
        }
    }
}

@Composable
private fun ChecklistRow(title: String, isComplete: Boolean, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isComplete) GreenSecondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isComplete) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isComplete) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isComplete) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GreenSecondary
            )
        }
    }
}
