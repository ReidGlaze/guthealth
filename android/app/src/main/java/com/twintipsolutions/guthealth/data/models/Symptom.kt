package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class Symptom(
    val id: String = "",
    val createdAt: Timestamp? = null,
    val type: String = "bloating",
    val severity: Int = 1,
    val location: String? = null,
    val notes: String = "",
    val endedAt: Timestamp? = null   // optional end time; null means still ongoing
)
