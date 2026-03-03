package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class CorrelationReport(
    val id: String = "",
    val createdAt: Timestamp? = null,
    val periodStart: String = "",
    val periodEnd: String = "",
    val mealsAnalyzed: Int = 0,
    val symptomsAnalyzed: Int = 0,
    val poopLogsAnalyzed: Int = 0,
    val aiReport: String = "",
    val disclaimer: String = "This is not medical advice"
)
