package com.twintipsolutions.guthealth.data

import android.util.Base64
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.twintipsolutions.guthealth.data.models.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FirestoreService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    private fun userDoc() = db.collection("users").document(
        currentUserId ?: throw IllegalStateException("Not signed in")
    )

    // --- Meals ---
    suspend fun saveMeal(meal: Meal): String {
        val ref = userDoc().collection("meals").document()
        ref.set(meal).await()
        return ref.id
    }

    suspend fun getTodayMeals(): List<Meal> {
        val startOfDay = getStartOfDay()
        val snapshot = userDoc().collection("meals")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startOfDay))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(Meal::class.java)
    }

    suspend fun getRecentEntries(limit: Int = 5): List<RecentEntry> {
        val entries = mutableListOf<RecentEntry>()
        val startOfDay = getStartOfDay()
        val ts = Timestamp(startOfDay)

        try {
            val meals = userDoc().collection("meals")
                .whereGreaterThanOrEqualTo("createdAt", ts)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
            meals.documents.forEach { doc ->
                val meal = doc.toObject(Meal::class.java) ?: return@forEach
                val foodNames = meal.foods.joinToString(", ") { it.name }.ifEmpty { meal.mealType }
                entries.add(
                    RecentEntry(
                        type = "meal",
                        title = meal.mealType.replaceFirstChar { it.uppercase() },
                        subtitle = foodNames,
                        timestamp = meal.createdAt
                    )
                )
            }
        } catch (_: Exception) {}

        try {
            val symptoms = userDoc().collection("symptoms")
                .whereGreaterThanOrEqualTo("createdAt", ts)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
            symptoms.documents.forEach { doc ->
                val symptom = doc.toObject(Symptom::class.java) ?: return@forEach
                entries.add(
                    RecentEntry(
                        type = "symptom",
                        title = symptom.type.replaceFirstChar { it.uppercase() },
                        subtitle = "Severity: ${symptom.severity}/10",
                        timestamp = symptom.createdAt
                    )
                )
            }
        } catch (_: Exception) {}

        try {
            val poopLogs = userDoc().collection("poopLogs")
                .whereGreaterThanOrEqualTo("createdAt", ts)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get().await()
            poopLogs.documents.forEach { doc ->
                val log = doc.toObject(PoopLog::class.java) ?: return@forEach
                entries.add(
                    RecentEntry(
                        type = "poop",
                        title = "Bristol Type ${log.bristolType}",
                        subtitle = "${log.color.replaceFirstChar { it.uppercase() }} - ${log.urgency}",
                        timestamp = log.createdAt
                    )
                )
            }
        } catch (_: Exception) {}

        return entries.sortedByDescending { it.timestamp?.seconds ?: 0 }.take(limit)
    }

    // --- Symptoms ---
    suspend fun saveSymptom(symptom: Symptom): String {
        val ref = userDoc().collection("symptoms").document()
        val data = mutableMapOf<String, Any?>(
            "createdAt" to symptom.createdAt,
            "type" to symptom.type,
            "severity" to symptom.severity,
            "location" to symptom.location,
            "notes" to symptom.notes
        )
        if (symptom.endedAt != null) {
            data["endedAt"] = symptom.endedAt
        }
        ref.set(data).await()
        return ref.id
    }

    suspend fun markSymptomResolved(symptomId: String) {
        userDoc().collection("symptoms").document(symptomId)
            .update("endedAt", Timestamp.now()).await()
    }

    suspend fun getTodaySymptoms(): List<Symptom> {
        val startOfDay = getStartOfDay()
        val snapshot = userDoc().collection("symptoms")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startOfDay))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(Symptom::class.java)
    }

    // --- Poop Logs ---
    suspend fun savePoopLog(poopLog: PoopLog): String {
        val ref = userDoc().collection("poopLogs").document()
        ref.set(poopLog).await()
        return ref.id
    }

    suspend fun uploadMealPhoto(uid: String, mealId: String, imageBase64: String): String {
        val bytes = Base64.decode(imageBase64, Base64.NO_WRAP)
        val ref = storage.reference.child("users/$uid/meals/$mealId.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadPoopPhoto(uid: String, logId: String, imageBase64: String): String {
        val bytes = Base64.decode(imageBase64, Base64.NO_WRAP)
        val ref = storage.reference.child("users/$uid/poopLogs/$logId.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun updateMealPhotoUrl(mealId: String, photoUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("meals").document(mealId)
            .update("photoUrl", photoUrl).await()
    }

    suspend fun updatePoopLogPhotoUrl(logId: String, photoUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("poopLogs").document(logId)
            .update("photoUrl", photoUrl).await()
    }

    suspend fun getTodayPoopLogs(): List<PoopLog> {
        val startOfDay = getStartOfDay()
        val snapshot = userDoc().collection("poopLogs")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startOfDay))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(PoopLog::class.java)
    }

    // --- Daily Summary ---
    suspend fun getDailySummary(dateKey: String): DailySummary? {
        val snapshot = userDoc().collection("dailySummaries").document(dateKey).get().await()
        return snapshot.toObject(DailySummary::class.java)
    }

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    // --- Correlation Reports (new narrative model) ---
    suspend fun getCorrelationReports(limit: Int = 10): List<CorrelationReport> {
        val snapshot = userDoc().collection("correlationReports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            CorrelationReport(
                id = doc.id,
                createdAt = data["createdAt"] as? Timestamp,
                periodStart = data["periodStart"] as? String ?: "",
                periodEnd = data["periodEnd"] as? String ?: "",
                mealsAnalyzed = (data["mealsAnalyzed"] as? Number)?.toInt() ?: 0,
                symptomsAnalyzed = (data["symptomsAnalyzed"] as? Number)?.toInt() ?: 0,
                poopLogsAnalyzed = (data["poopLogsAnalyzed"] as? Number)?.toInt() ?: 0,
                aiReport = data["aiReport"] as? String ?: "",
                disclaimer = data["disclaimer"] as? String ?: "This is not medical advice"
            )
        }
    }

    // --- Weekly Insights ---
    suspend fun getWeeklyInsights(limit: Int = 4): List<WeeklyInsight> {
        val snapshot = userDoc().collection("weeklyInsights")
            .orderBy("generatedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.toObjects(WeeklyInsight::class.java)
    }

    // --- FODMAP Database ---
    suspend fun getAllFodmapFoods(): List<FodmapFood> {
        val snapshot = db.collection("fodmapDatabase")
            .get()
            .await()
        return snapshot.toObjects(FodmapFood::class.java)
    }

    suspend fun searchFodmapFoods(query: String): List<FodmapFood> {
        if (query.isBlank()) return emptyList()
        val snapshot = db.collection("fodmapDatabase")
            .orderBy("name")
            .startAt(query.lowercase())
            .endAt(query.lowercase() + "\uf8ff")
            .limit(20)
            .get()
            .await()
        return snapshot.toObjects(FodmapFood::class.java)
    }

    suspend fun getFodmapFoodsByCategory(category: String): List<FodmapFood> {
        val snapshot = db.collection("fodmapDatabase")
            .whereEqualTo("category", category)
            .limit(50)
            .get()
            .await()
        return snapshot.toObjects(FodmapFood::class.java)
    }

    // --- User Profile ---
    suspend fun getUserProfile(): UserProfile? {
        val snapshot = userDoc().get().await()
        return snapshot.toObject(UserProfile::class.java)
    }

    suspend fun updateFodmapPhase(phase: String) {
        userDoc().update(
            mapOf(
                "fodmapPhase" to phase,
                "fodmapPhaseStartDate" to Timestamp.now()
            )
        ).await()
    }

    suspend fun updateUserProfile(fields: Map<String, Any>) {
        userDoc().update(fields).await()
    }

    suspend fun deleteUserData() {
        val uid = currentUserId ?: return
        val userRef = db.collection("users").document(uid)
        val subcollections = listOf("meals", "symptoms", "poopLogs", "dailySummaries", "correlations", "correlationReports", "weeklyInsights")
        for (name in subcollections) {
            val snapshot = userRef.collection(name).get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        }
        userRef.delete().await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    // --- Cloud Functions ---
    suspend fun analyzeFoodPhoto(imageBase64: String, mealType: String? = null): Map<String, Any> {
        val data = hashMapOf<String, Any>("imageBase64" to imageBase64)
        if (mealType != null) data["mealType"] = mealType
        val result = functions.getHttpsCallable("analyzeFoodPhoto").call(data).await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun classifyPoopPhoto(imageBase64: String): Map<String, Any> {
        val data = hashMapOf<String, Any>("imageBase64" to imageBase64)
        val result = functions.getHttpsCallable("classifyPoopPhoto").call(data).await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    suspend fun runCorrelationEngine(daysBack: Int = 7): Map<String, Any> {
        val data = hashMapOf<String, Any>(
            "userId" to (currentUserId ?: ""),
            "daysBack" to daysBack
        )
        val result = functions.getHttpsCallable("runCorrelationEngine").call(data).await()
        @Suppress("UNCHECKED_CAST")
        return result.getData() as Map<String, Any>
    }

    // --- History: entries for a specific date range ---
    suspend fun getMealsForDateRange(startDate: Date, endDate: Date): List<Pair<String, Meal>> {
        val snapshot = userDoc().collection("meals")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startDate))
            .whereLessThan("createdAt", Timestamp(endDate))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val meal = doc.toObject(Meal::class.java) ?: return@mapNotNull null
            doc.id to meal
        }
    }

    suspend fun getSymptomsForDateRange(startDate: Date, endDate: Date): List<Pair<String, Symptom>> {
        val snapshot = userDoc().collection("symptoms")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startDate))
            .whereLessThan("createdAt", Timestamp(endDate))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val symptom = doc.toObject(Symptom::class.java) ?: return@mapNotNull null
            doc.id to symptom
        }
    }

    suspend fun getPoopLogsForDateRange(startDate: Date, endDate: Date): List<Pair<String, PoopLog>> {
        val snapshot = userDoc().collection("poopLogs")
            .whereGreaterThanOrEqualTo("createdAt", Timestamp(startDate))
            .whereLessThan("createdAt", Timestamp(endDate))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val log = doc.toObject(PoopLog::class.java) ?: return@mapNotNull null
            doc.id to log
        }
    }

    suspend fun deleteMeal(mealId: String) {
        userDoc().collection("meals").document(mealId).delete().await()
    }

    suspend fun deleteSymptom(symptomId: String) {
        userDoc().collection("symptoms").document(symptomId).delete().await()
    }

    suspend fun deletePoopLog(logId: String) {
        userDoc().collection("poopLogs").document(logId).delete().await()
    }

    suspend fun deleteCorrelationReport(reportId: String) {
        userDoc().collection("correlationReports").document(reportId).delete().await()
    }

    // --- Helpers ---
    private fun getStartOfDay(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}

data class RecentEntry(
    val type: String,
    val title: String,
    val subtitle: String,
    val timestamp: Timestamp? = null
)
