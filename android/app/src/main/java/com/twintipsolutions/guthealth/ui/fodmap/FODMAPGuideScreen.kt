package com.twintipsolutions.guthealth.ui.fodmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.FodmapRepository
import com.twintipsolutions.guthealth.data.models.FodmapFood
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FODMAPGuideScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firestoreService = remember { FirestoreService() }
    val fodmapRepository = remember { FodmapRepository.getInstance(context) }

    var searchQuery by remember { mutableStateOf("") }
    var currentPhase by remember { mutableStateOf("elimination") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val allFoods by fodmapRepository.foods.collectAsState()

    LaunchedEffect(Unit) {
        try {
            firestoreService.signInAnonymously()
            val profile = firestoreService.getUserProfile()
            if (profile != null) {
                currentPhase = profile.fodmapPhase
            }
        } catch (_: Exception) {}
        fodmapRepository.refreshIfNeeded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FODMAP Guide") },
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
            // Search bar — above phase card, matches iOS
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search foods...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Phase card — white card with segmented pills
            PhaseCard(
                currentPhase = currentPhase,
                onPhaseChange = { newPhase ->
                    scope.launch {
                        try {
                            firestoreService.updateFodmapPhase(newPhase)
                            currentPhase = newPhase
                        } catch (_: Exception) {}
                    }
                }
            )

            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(null to "All", "low" to "Low", "moderate" to "Moderate", "high" to "High")
                filters.forEach { (level, label) ->
                    val isSelected = selectedFilter == level
                    val chipColor = when (level) {
                        "low" -> GreenSecondary
                        "moderate" -> Color(0xFFFFB74D)
                        "high" -> Color(0xFFE57373)
                        else -> TealPrimary
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = level },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Phase guidance tips — teal-tinted box matching iOS
            PhaseGuidanceCard(phase = currentPhase)

            // Filtered food list
            val displayedFoods = allFoods.filter { food ->
                val matchesFilter = selectedFilter == null || food.fodmapLevel == selectedFilter
                val matchesSearch = searchQuery.length < 2 || food.name.contains(searchQuery, ignoreCase = true) ||
                    food.category.contains(searchQuery, ignoreCase = true)
                matchesFilter && matchesSearch
            }

            if (displayedFoods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (searchQuery.length >= 2) "No foods found matching \"$searchQuery\". Try a different search term."
                               else "No foods found for this filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                displayedFoods.forEach { food ->
                    FodmapFoodRow(food = food)
                }
            }

            // Disclaimer
            Text(
                text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice. Consult a dietitian for personalized FODMAP guidance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhaseCard(currentPhase: String, onPhaseChange: (String) -> Unit) {
    val phases = listOf("elimination", "reintroduction", "maintenance")

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
                text = "Your FODMAP Phase",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Segmented pill selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                phases.forEach { phase ->
                    val isSelected = phase == currentPhase
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TealPrimary else Color.Transparent)
                            .clickable { onPhaseChange(phase) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phase.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phase name + "Started today" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = phaseTitle(currentPhase),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = phaseDescription(currentPhase),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // "Started today" badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenSecondary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Started today",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun phaseTitle(phase: String): String = when (phase) {
    "elimination" -> "Elimination Phase"
    "reintroduction" -> "Reintroduction Phase"
    "maintenance" -> "Maintenance Phase"
    else -> ""
}

private fun phaseDescription(phase: String): String = when (phase) {
    "elimination" -> "Avoid high FODMAP foods for 2-6 weeks"
    "reintroduction" -> "Slowly reintroduce one FODMAP group at a time"
    "maintenance" -> "Enjoy a varied diet while avoiding personal triggers"
    else -> ""
}

@Composable
private fun PhaseGuidanceCard(phase: String) {
    val tips = when (phase) {
        "elimination" -> listOf(
            "Focus on low FODMAP foods (marked green below)",
            "Keep a detailed food diary",
            "Symptoms should improve within 2-6 weeks"
        )
        "reintroduction" -> listOf(
            "Test one FODMAP group at a time",
            "Wait 3 days between testing new groups",
            "Return to elimination diet between tests"
        )
        "maintenance" -> listOf(
            "Avoid your identified trigger foods",
            "You may tolerate some FODMAPs in small amounts",
            "Enjoy a varied, balanced diet"
        )
        else -> emptyList()
    }

    if (tips.isEmpty()) return

    // Teal-tinted tips box matching iOS style
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TealPrimary.copy(alpha = 0.08f))
            .border(1.dp, TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Tips",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = GreenSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FodmapFoodRow(food: FodmapFood) {
    val levelColor = when (food.fodmapLevel) {
        "high" -> Color(0xFFE57373)
        "moderate" -> Color(0xFFFFB74D)
        else -> GreenSecondary
    }
    val levelText = food.fodmapLevel.replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(levelColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        // Name + category + fodmap categories
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            val subtitleParts = buildList {
                if (food.category.isNotEmpty()) add(food.category.replaceFirstChar { it.uppercase() })
                if (food.fodmapCategories.isNotEmpty()) add(food.fodmapCategories.joinToString(", "))
            }
            if (subtitleParts.isNotEmpty()) {
                Text(
                    text = subtitleParts.joinToString(" \u00b7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // FODMAP level + safe serving on right — matches iOS layout
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = levelText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = levelColor
            )
            if (food.lowFodmapServing.isNotEmpty()) {
                Text(
                    text = "Safe: ${food.lowFodmapServing}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
