package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class Meal(
    val id: String = "",
    val createdAt: Timestamp? = null,
    val mealType: String = "snack",
    val photoUrl: String? = null,
    val foods: List<Food> = emptyList(),
    val notes: String = "",
    val aiAnalysis: AiAnalysis? = null
)

data class Food(
    val name: String = "",
    val fodmapLevel: String = "unknown",
    val fodmapCategories: List<String> = emptyList(),
    val servingSize: String = "",
    val triggers: List<String> = emptyList()
)

data class AiAnalysis(
    val rawResponse: String = "",
    val analyzedAt: Timestamp? = null
)
