package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class PoopLog(
    val id: String = "",
    val createdAt: Timestamp? = null,
    val bristolType: Int = 4,
    val color: String = "brown",
    val urgency: String = "normal",
    val photoUrl: String? = null,
    val aiClassification: AiClassification? = null,
    val notes: String = ""
)

data class AiClassification(
    val bristolType: Int = 4,
    val color: String = "brown",
    val analyzedAt: Timestamp? = null
)
