package com.twintipsolutions.guthealth.data.models

import com.google.firebase.Timestamp

data class UserProfile(
    val displayName: String = "",
    val createdAt: Timestamp? = null,
    val preferences: UserPreferences = UserPreferences(),
    val fodmapPhase: String = "elimination",
    val fodmapPhaseStartDate: Timestamp? = null,
    val fcmToken: String = ""
)

data class UserPreferences(
    val reminderTimes: List<String> = listOf("08:00", "12:00", "18:00"),
    val timezone: String = "America/Denver",
    val notificationsEnabled: Boolean = false
)
