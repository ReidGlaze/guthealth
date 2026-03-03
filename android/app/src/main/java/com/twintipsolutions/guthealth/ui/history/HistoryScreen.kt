package com.twintipsolutions.guthealth.ui.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.Meal
import com.twintipsolutions.guthealth.data.models.PoopLog
import com.twintipsolutions.guthealth.data.models.Symptom
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class HistoryEntry(
    val id: String,
    val type: String,
    val timestamp: Date?
) {
    class MealEntry(id: String, val meal: Meal, timestamp: Date?) : HistoryEntry(id, "meal", timestamp)
    class SymptomEntry(id: String, val symptom: Symptom, timestamp: Date?) : HistoryEntry(id, "symptom", timestamp)
    class PoopEntry(id: String, val poopLog: PoopLog, timestamp: Date?) : HistoryEntry(id, "poop", timestamp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService() }

    val today = remember { Calendar.getInstance() }
    val last7Days = remember {
        (0..6).map { daysBack ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysBack) }
        }.reversed()
    }

    var selectedDayIndex by remember { mutableIntStateOf(6) } // default to today (last item)
    var entries by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var daysWithEntries by remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun getDateRange(cal: Calendar): Pair<Date, Date> {
        val start = Calendar.getInstance().apply {
            time = cal.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            time = start.time
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return start.time to end.time
    }

    fun loadEntriesForDay(dayIndex: Int) {
        scope.launch {
            isLoading = true
            try {
                firestoreService.signInAnonymously()
                val (startDate, endDate) = getDateRange(last7Days[dayIndex])
                val allEntries = mutableListOf<HistoryEntry>()

                val meals = firestoreService.getMealsForDateRange(startDate, endDate)
                meals.forEach { (docId, meal) ->
                    allEntries.add(
                        HistoryEntry.MealEntry(
                            id = docId,
                            meal = meal,
                            timestamp = meal.createdAt?.toDate()
                        )
                    )
                }

                val symptoms = firestoreService.getSymptomsForDateRange(startDate, endDate)
                symptoms.forEach { (docId, symptom) ->
                    allEntries.add(
                        HistoryEntry.SymptomEntry(
                            id = docId,
                            symptom = symptom,
                            timestamp = symptom.createdAt?.toDate()
                        )
                    )
                }

                val poopLogs = firestoreService.getPoopLogsForDateRange(startDate, endDate)
                poopLogs.forEach { (docId, log) ->
                    allEntries.add(
                        HistoryEntry.PoopEntry(
                            id = docId,
                            poopLog = log,
                            timestamp = log.createdAt?.toDate()
                        )
                    )
                }

                entries = allEntries.sortedByDescending { it.timestamp?.time ?: 0 }
            } catch (_: Exception) {
                entries = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Load dot indicators for all 7 days
    fun loadDayIndicators() {
        scope.launch {
            try {
                firestoreService.signInAnonymously()
                val hasEntries = mutableSetOf<Int>()
                last7Days.forEachIndexed { index, cal ->
                    val (startDate, endDate) = getDateRange(cal)
                    val meals = firestoreService.getMealsForDateRange(startDate, endDate)
                    val symptoms = firestoreService.getSymptomsForDateRange(startDate, endDate)
                    val poopLogs = firestoreService.getPoopLogsForDateRange(startDate, endDate)
                    if (meals.isNotEmpty() || symptoms.isNotEmpty() || poopLogs.isNotEmpty()) {
                        hasEntries.add(index)
                    }
                }
                daysWithEntries = hasEntries
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadDayIndicators()
        loadEntriesForDay(selectedDayIndex)
    }

    fun deleteEntry(entry: HistoryEntry) {
        scope.launch {
            try {
                when (entry) {
                    is HistoryEntry.MealEntry -> firestoreService.deleteMeal(entry.id)
                    is HistoryEntry.SymptomEntry -> firestoreService.deleteSymptom(entry.id)
                    is HistoryEntry.PoopEntry -> firestoreService.deletePoopLog(entry.id)
                }
                entries = entries.filter { it.id != entry.id }
                // Update dot indicators
                if (entries.isEmpty()) {
                    daysWithEntries = daysWithEntries - selectedDayIndex
                }
            } catch (_: Exception) {}
        }
    }

    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.US) }
    val dayOfMonthFormat = remember { SimpleDateFormat("d", Locale.US) }
    val headerDateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
        ) {
            // Week day selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(last7Days.size) { index ->
                        val cal = last7Days[index]
                        val isSelected = index == selectedDayIndex
                        val isToday = index == 6
                        val hasEntries = daysWithEntries.contains(index)

                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) TealPrimary else Color.Transparent,
                            label = "dayBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White
                            else if (isToday) TealPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            label = "dayText"
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedDayIndex = index
                                    loadEntriesForDay(index)
                                }
                                .background(bgColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayOfWeekFormat.format(cal.time),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayOfMonthFormat.format(cal.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Dot indicator
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasEntries) {
                                            if (isSelected) Color.White else TealPrimary
                                        } else Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }

            // Date header
            Text(
                text = headerDateFormat.format(last7Days[selectedDayIndex].time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Entries list
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else if (entries.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No entries",
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No entries for this day",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Use the Log tab to add meals, symptoms, or poop logs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = entries,
                        key = { it.id }
                    ) { entry ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    deleteEntry(entry)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0xFFE57373),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            HistoryEntryCard(entry = entry)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: HistoryEntry) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }
    val timeText = entry.timestamp?.let { timeFormat.format(it) } ?: ""

    val icon = when (entry) {
        is HistoryEntry.MealEntry -> Icons.Default.CameraAlt
        is HistoryEntry.SymptomEntry -> Icons.Default.EditNote
        is HistoryEntry.PoopEntry -> Icons.Default.WaterDrop
    }
    val iconColor = when (entry) {
        is HistoryEntry.MealEntry -> TealPrimary
        is HistoryEntry.SymptomEntry -> Color(0xFFFFB74D)
        is HistoryEntry.PoopEntry -> GreenSecondary
    }
    val typeLabel = when (entry) {
        is HistoryEntry.MealEntry -> entry.meal.mealType.replaceFirstChar { it.uppercase() }
        is HistoryEntry.SymptomEntry -> entry.symptom.type.replaceFirstChar { it.uppercase() }
        is HistoryEntry.PoopEntry -> "Bristol Type ${entry.poopLog.bristolType}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                when (entry) {
                    is HistoryEntry.MealEntry -> {
                        val foodNames = entry.meal.foods.joinToString(", ") { it.name }
                        if (foodNames.isNotEmpty()) {
                            Text(
                                text = foodNames,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // FODMAP badges
                        val fodmapLevels = entry.meal.foods
                            .map { it.fodmapLevel }
                            .filter { it != "unknown" }
                            .distinct()
                        if (fodmapLevels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                fodmapLevels.forEach { level ->
                                    val badgeColor = when (level) {
                                        "high" -> Color(0xFFE57373)
                                        "moderate" -> Color(0xFFFFB74D)
                                        "low" -> GreenSecondary
                                        else -> Color.Gray
                                    }
                                    Text(
                                        text = level.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier
                                            .background(badgeColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    is HistoryEntry.SymptomEntry -> {
                        Text(
                            text = "Severity: ${entry.symptom.severity}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.symptom.notes.isNotEmpty()) {
                            Text(
                                text = entry.symptom.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    is HistoryEntry.PoopEntry -> {
                        Text(
                            text = "${entry.poopLog.color.replaceFirstChar { it.uppercase() }} - ${entry.poopLog.urgency}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.poopLog.notes.isNotEmpty()) {
                            Text(
                                text = entry.poopLog.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
