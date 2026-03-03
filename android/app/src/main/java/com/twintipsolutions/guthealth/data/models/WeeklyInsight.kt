package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class WeeklyInsight(
    val weekStart: String = "",
    val weekEnd: String = "",
    val gutScoreTrend: List<Int> = emptyList(),
    val avgGutScore: Double = 0.0,
    val topTriggers: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val aiReport: String = "",
    val generatedAt: Timestamp? = null
)
