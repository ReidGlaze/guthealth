package com.twintipsolutions.guthealth.ui.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.app.Activity
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Timestamp
import com.twintipsolutions.guthealth.data.FirestoreService
import com.twintipsolutions.guthealth.data.models.CorrelationReport
import com.twintipsolutions.guthealth.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val firestoreService = remember { FirestoreService() }

    var correlationReports by remember { mutableStateOf<List<CorrelationReport>>(emptyList()) }
    var isRunningAnalysis by remember { mutableStateOf(false) }
    var analysisError by remember { mutableStateOf<String?>(null) }
    var selectedDays by remember { mutableStateOf(7) }

    fun loadData() {
        scope.launch {
            try {
                firestoreService.signInAnonymously()
                correlationReports = firestoreService.getCorrelationReports()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
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
            // Correlation analysis section
            CorrelationReportsCard(
                reports = correlationReports,
                isRunningAnalysis = isRunningAnalysis,
                analysisError = analysisError,
                selectedDays = selectedDays,
                onSelectedDaysChange = { selectedDays = it },
                onRunAnalysis = {
                    isRunningAnalysis = true
                    analysisError = null
                    scope.launch {
                        try {
                            val result = firestoreService.runCorrelationEngine(daysBack = selectedDays)
                            val message = result["message"] as? String
                            val reportId = result["reportId"] as? String
                            val aiReport = result["aiReport"] as? String
                            if (message != null && reportId == null) {
                                analysisError = message
                                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                            } else if (reportId != null && aiReport != null) {
                                val report = CorrelationReport(
                                    id = reportId,
                                    createdAt = Timestamp.now(),
                                    periodStart = result["periodStart"] as? String ?: "",
                                    periodEnd = result["periodEnd"] as? String ?: "",
                                    mealsAnalyzed = (result["mealsAnalyzed"] as? Number)?.toInt() ?: 0,
                                    symptomsAnalyzed = (result["symptomsAnalyzed"] as? Number)?.toInt() ?: 0,
                                    poopLogsAnalyzed = (result["poopLogsAnalyzed"] as? Number)?.toInt() ?: 0,
                                    aiReport = aiReport,
                                    disclaimer = result["disclaimer"] as? String ?: "This is not medical advice"
                                )
                                correlationReports = listOf(report) + correlationReports
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                requestInAppReview(context)
                            }
                        } catch (e: Exception) {
                            analysisError = "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                        } finally {
                            isRunningAnalysis = false
                        }
                    }
                }
            )

            // Page-level disclaimer
            Text(
                text = "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun TimePeriodSelector(
    selectedDays: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(3 to "3 Days", 7 to "7 Days", 10 to "10 Days")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (days, label) ->
            val isSelected = selectedDays == days
            OutlinedButton(
                onClick = { onSelectionChange(days) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) TealPrimary else Color.Transparent,
                    contentColor = if (isSelected) Color.White else TealPrimary
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = TealPrimary
                ),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CorrelationReportsCard(
    reports: List<CorrelationReport>,
    isRunningAnalysis: Boolean,
    analysisError: String?,
    selectedDays: Int,
    onSelectedDaysChange: (Int) -> Unit,
    onRunAnalysis: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Correlation Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AI analysis of your meals, symptoms, and poop logs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time period selector
            Text(
                text = "Analysis period",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimePeriodSelector(
                selectedDays = selectedDays,
                onSelectionChange = onSelectedDaysChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Run analysis button
            Button(
                onClick = onRunAnalysis,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                enabled = !isRunningAnalysis
            ) {
                if (isRunningAnalysis) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing your data...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Correlation Analysis")
                }
            }

            if (analysisError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = analysisError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Analyzes your last $selectedDays days of meals, symptoms, and poop logs together for patterns.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    // Past reports list
    if (reports.isNotEmpty()) {
        reports.forEach { report ->
            CorrelationReportCard(report = report)
        }
    } else {
        EmptyReportsCard()
    }
}

private val reportDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

@Composable
private fun CorrelationReportCard(report: CorrelationReport) {
    val dateLabel = buildString {
        if (report.periodStart.isNotEmpty() && report.periodEnd.isNotEmpty()) {
            append(report.periodStart)
            append(" \u2014 ")
            append(report.periodEnd)
        } else {
            report.createdAt?.let { ts ->
                append(reportDateFormat.format(Date(ts.seconds * 1000)))
            }
        }
    }

    val statsLine = buildString {
        val parts = mutableListOf<String>()
        if (report.mealsAnalyzed > 0) parts.add("${report.mealsAnalyzed} meals")
        if (report.symptomsAnalyzed > 0) parts.add("${report.symptomsAnalyzed} symptoms")
        if (report.poopLogsAnalyzed > 0) parts.add("${report.poopLogsAnalyzed} poop logs")
        append(parts.joinToString(" \u2022 "))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (statsLine.isNotEmpty()) {
                        Text(
                            text = statsLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (report.aiReport.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = report.aiReport,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = report.disclaimer.ifEmpty { "This is not medical advice." },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun requestInAppReview(context: Context) {
    val prefs = context.getSharedPreferences("gut_health_prefs", Context.MODE_PRIVATE)
    val count = prefs.getInt("correlationReportCount", 0) + 1
    prefs.edit().putInt("correlationReportCount", count).apply()
    // Prompt after 1st and every 3rd report thereafter
    if (count == 1 || (count > 1 && count % 3 == 0)) {
        val activity = context as? Activity ?: return
        val reviewManager = ReviewManagerFactory.create(context)
        reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            reviewManager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

@Composable
private fun EmptyReportsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No reports yet",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Run a correlation analysis above to get your first AI-powered gut health report. For best results, log meals, symptoms, and poop logs for at least 7 days.\n\nThis is not medical advice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
