package com.twintipsolutions.guthealth.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.ui.theme.GreenSecondary
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import com.twintipsolutions.guthealth.ui.theme.TealLight
import kotlinx.coroutines.launch

private const val PREFS_NAME = "gut_health_prefs"
private const val KEY_ONBOARDING_COMPLETED = "has_completed_onboarding"

fun hasCompletedOnboarding(context: Context): Boolean {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_ONBOARDING_COMPLETED, false)
}

private fun setOnboardingCompleted(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ONBOARDING_COMPLETED, true)
        .apply()
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firestoreService = remember { FirestoreService() }

    var selectedPhase by remember { mutableStateOf("elimination") }
    var remindersEnabled by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        remindersEnabled = granted
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> HowItWorksPage()
                    2 -> WhatAreFodmapsPage()
                    3 -> EliminationDietPage()
                    4 -> FodmapJourneyPage(
                        selectedPhase = selectedPhase,
                        onPhaseSelected = { selectedPhase = it }
                    )
                    5 -> StayConsistentPage(
                        remindersEnabled = remindersEnabled,
                        onRemindersToggle = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@StayConsistentPage
                                }
                            }
                            remindersEnabled = enabled
                        }
                    )
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(6) { index ->
                    val color by animateColorAsState(
                        targetValue = if (index == pagerState.currentPage)
                            TealPrimary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Navigation buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pagerState.currentPage < 5) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(
                            "Continue",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                try {
                                    val resolvedPhase = if (selectedPhase == "unsure") "elimination" else selectedPhase
                                    firestoreService.signInAnonymously()
                                    firestoreService.updateUserProfile(
                                        mapOf(
                                            "fodmapPhase" to resolvedPhase,
                                            "fodmapPhaseStartDate" to Timestamp.now(),
                                            "preferences.notificationsEnabled" to remindersEnabled
                                        )
                                    )
                                } catch (_: Exception) {}
                                setOnboardingCompleted(context)
                                isSaving = false
                                onComplete()
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Get Started",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (pagerState.currentPage in 1..4) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text(
                            "Back",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Spa,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = TealPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Welcome to\nAI Gut Health",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Discover which foods trigger your\nsymptoms using AI",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun HowItWorksPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "How It Works",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        OnboardingStep(
            icon = Icons.Default.CameraAlt,
            title = "Log Your Meals",
            description = "Snap a photo and AI identifies foods with FODMAP levels"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingStep(
            icon = Icons.Default.BarChart,
            title = "Track Symptoms",
            description = "Record bloating, gas, pain, and other digestive symptoms"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingStep(
            icon = Icons.Default.AutoAwesome,
            title = "AI Finds Triggers",
            description = "Our AI analyzes patterns to find your trigger foods"
        )
    }
}

@Composable
private fun OnboardingStep(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(TealLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = TealPrimary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun WhatAreFodmapsPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TealPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "What Are FODMAPs?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Fermentable Oligosaccharides, Disaccharides,\nMonosaccharides, And Polyols",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FodmapCategoryRow("Fructose", "Apples, pears, honey")
            FodmapCategoryRow("Lactose", "Milk, soft cheese, yogurt")
            FodmapCategoryRow("Fructans", "Wheat, garlic, onion")
            FodmapCategoryRow("GOS", "Legumes, cashews")
            FodmapCategoryRow("Mannitol", "Mushrooms, cauliflower")
            FodmapCategoryRow("Sorbitol", "Stone fruits, sweeteners")
        }
    }
}

@Composable
private fun FodmapCategoryRow(name: String, examples: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(TealPrimary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = examples,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EliminationDietPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        @Suppress("DEPRECATION")
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TealPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "The Elimination Diet",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        EliminationPhaseCard("1", "Elimination", "2-6 weeks", "Remove all high-FODMAP foods")
        Spacer(modifier = Modifier.height(12.dp))
        EliminationPhaseCard("2", "Reintroduction", "6-8 weeks", "Test one FODMAP group at a time")
        Spacer(modifier = Modifier.height(12.dp))
        EliminationPhaseCard("3", "Maintenance", "Ongoing", "Eat freely except personal triggers")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Most people find relief within the first 2 weeks",
            style = MaterialTheme.typography.bodyMedium,
            color = GreenSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EliminationPhaseCard(number: String, title: String, duration: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TealLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "($duration)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FodmapJourneyPage(
    selectedPhase: String,
    onPhaseSelected: (String) -> Unit
) {
    val phases = listOf(
        Triple("elimination", "Elimination", "New to low-FODMAP? Remove high-FODMAP foods for 2-6 weeks"),
        Triple("reintroduction", "Reintroduction", "Ready to test one FODMAP group at a time"),
        Triple("maintenance", "Maintenance", "Manage your long-term personalized diet"),
        Triple("unsure", "I'm not sure", "We'll start you with elimination — change anytime in Settings")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TealPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Your FODMAP Journey",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "FODMAPs are certain carbohydrates that\ncan trigger IBS symptoms",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Select your phase:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        phases.forEach { (id, title, subtitle) ->
            val isSelected = selectedPhase == id
            val bgColor by animateColorAsState(
                targetValue = if (isSelected)
                    TealLight.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface,
                label = "phase_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected)
                    TealPrimary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                label = "phase_border"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onPhaseSelected(id) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun StayConsistentPage(
    remindersEnabled: Boolean,
    onRemindersToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TealPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Stay Consistent",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "The AI needs at least 7 days of data to\nfind your trigger foods. Log every meal\nand symptom!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Reminders toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Enable Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = remindersEnabled,
                onCheckedChange = onRemindersToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
            )
        }
    }
}
