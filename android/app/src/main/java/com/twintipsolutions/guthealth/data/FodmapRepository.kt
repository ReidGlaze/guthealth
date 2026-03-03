package com.twintipsolutions.guthealth.data

import android.content.Context
import android.content.SharedPreferences
import com.twintipsolutions.guthealth.data.models.FodmapDatabase
import com.twintipsolutions.guthealth.data.models.FodmapFood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the FODMAP food database with Firestore as source of truth
 * and Firestore's built-in offline persistence for caching.
 * Falls back to bundled data on first launch.
 */
class FodmapRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("fodmap_cache", Context.MODE_PRIVATE)
    private val firestoreService = FirestoreService()

    private val _foods = MutableStateFlow(FodmapDatabase.bundledFoods)
    val foods: StateFlow<List<FodmapFood>> = _foods.asStateFlow()

    private val cacheTtlMs = 24 * 60 * 60 * 1000L // 24 hours

    /** Refreshes from Firestore if cache is stale (>24h) or if forced. */
    suspend fun refreshIfNeeded(force: Boolean = false) {
        if (!force && !isCacheStale()) return
        try {
            val fetched = firestoreService.getAllFodmapFoods()
            if (fetched.isNotEmpty()) {
                _foods.value = fetched
                prefs.edit().putLong("lastFetchTimestamp", System.currentTimeMillis()).apply()
            }
        } catch (_: Exception) {
            // Keep current data on failure
        }
    }

    /** Search foods by name with case-insensitive partial matching. */
    fun search(query: String): List<FodmapFood> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return _foods.value
        return _foods.value.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    /** Find the best match for a food name. */
    fun lookup(name: String): FodmapFood? {
        val trimmed = name.trim().lowercase()
        if (trimmed.isEmpty()) return null
        _foods.value.firstOrNull { it.name.lowercase() == trimmed }?.let { return it }
        return _foods.value.firstOrNull {
            it.name.lowercase().contains(trimmed) || trimmed.contains(it.name.lowercase())
        }
    }

    private fun isCacheStale(): Boolean {
        val lastFetch = prefs.getLong("lastFetchTimestamp", 0L)
        return System.currentTimeMillis() - lastFetch > cacheTtlMs
    }

    companion object {
        @Volatile
        private var instance: FodmapRepository? = null

        fun getInstance(context: Context): FodmapRepository {
            return instance ?: synchronized(this) {
                instance ?: FodmapRepository(context).also { instance = it }
            }
        }
    }
}
