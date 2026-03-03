package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class Correlation(
    val id: String = "",
    val createdAt: Timestamp? = null,
    val triggerFood: String = "",
    val symptomType: String = "",
    val confidence: Double = 0.0,
    val occurrences: Int = 0,
    val avgTimeLag: Double = 0.0,
    val recommendation: String = "",
    val status: String = "confirmed",  // "confirmed" or "suspected" (legacy fallback)

    // New fields from improved correlation engine
    val confidenceLabel: String = "",          // "suspected", "probable", "likely", "strong"
    val totalExposures: Int = 0,               // total times this food was consumed
    val symptomaticExposures: Int = 0,         // how many times it caused the symptom
    val hitRate: Double = 0.0,                 // symptomaticExposures / totalExposures
    val possibleConfound: Boolean = false,     // true if food is low-FODMAP, correlation may be spurious
    val confoundNote: String = "",             // explains the confound
    val confoundRisk: String = "",             // "high", "moderate", or "low"
    val bristolImpact: String = "",            // description of Bristol type changes
    val insufficientData: Boolean = false,     // true when not enough data to confirm pattern
    val insufficientDataNote: String = ""      // explanation shown to user
)
