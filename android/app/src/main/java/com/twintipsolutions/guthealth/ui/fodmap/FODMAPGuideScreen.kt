package com.twintipsolutions.guthealth.ui.fodmap

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
    val context = LocalContext.current
    val firestoreService = remember { FirestoreService() }
    val fodmapRepository = remember { FodmapRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var isInfoExpanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val allFoods by fodmapRepository.foods.collectAsState()

    fun loadData() {
        scope.launch {
            isRefreshing = true
            try {
                firestoreService.signInAnonymously()
                fodmapRepository.refreshIfNeeded()
            } catch (_: Exception) {
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            firestoreService.signInAnonymously()
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
        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { loadData() },
            state = pullRefreshState,
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

                // What is FODMAP? expandable info card
                WhatIsFodmapCard(
                    isExpanded = isInfoExpanded,
                    onToggle = { isInfoExpanded = !isInfoExpanded }
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

                // Research attribution footer with clickable Monash link
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val monashText = buildAnnotatedString {
                        append("FODMAP data based on research by ")
                        pushStringAnnotation(tag = "URL", annotation = "https://www.monashfodmap.com")
                        withStyle(
                            style = SpanStyle(
                                color = TealPrimary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Monash University")
                        }
                        pop()
                    }
                    ClickableText(
                        text = monashText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        onClick = { offset ->
                            monashText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                        }
                    )
                    Text(
                        text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice. Consult a dietitian for personalized FODMAP guidance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ClickableText(
    text: androidx.compose.ui.text.AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    onClick: (Int) -> Unit
) {
    androidx.compose.foundation.text.ClickableText(
        text = text,
        style = style,
        onClick = onClick
    )
}

@Composable
private fun WhatIsFodmapCard(isExpanded: Boolean, onToggle: () -> Unit) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "What is FODMAP?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "FODMAPs (Fermentable Oligosaccharides, Disaccharides, Monosaccharides, and Polyols) are short-chain carbohydrates that can cause bloating, gas, and digestive discomfort, especially for those with IBS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This guide helps you identify high and low FODMAP foods. Use it with the app's meal logging and correlation analysis to discover your personal triggers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Color legend
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Color Legend",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        LegendRow(color = GreenSecondary, label = "Low", detail = "Generally well-tolerated")
                        LegendRow(color = Color(0xFFFFB74D), label = "Moderate", detail = "May trigger in larger portions")
                        LegendRow(color = Color(0xFFE57373), label = "High", detail = "Common trigger for sensitive individuals")
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "— $detail",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
