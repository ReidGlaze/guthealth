package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class DailySummary(
    val date: String = "",
    val totalMeals: Int = 0,
    val totalSymptoms: Int = 0,
    val totalPoopLogs: Int = 0,
    val highFodmapCount: Int = 0,
    val avgSymptomSeverity: Double = 0.0,
    val dominantSymptom: String? = null,
    val generatedAt: Timestamp? = null,
    val aiSummary: String = ""
)
