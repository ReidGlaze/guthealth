package com.twintipsolutions.guthealth.data.models

data class FodmapFood(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val fodmapLevel: String = "low",
    val fodmapCategories: List<String> = emptyList(),
    val servingSize: String = "",
    val lowFodmapServing: String = "",
    val notes: String = ""
)
