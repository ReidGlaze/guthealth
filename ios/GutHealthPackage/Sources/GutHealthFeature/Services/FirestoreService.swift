import Foundation
import FirebaseFirestore

public actor FirestoreService {
    public static let shared = FirestoreService()
    private let db = Firestore.firestore()

    private init() {}

    // MARK: - User Profile

    public func saveUserProfile(uid: String, profile: UserProfile) throws {
        let ref = db.collection("users").document(uid)
        try ref.setData(from: profile)
    }

    public func getUserProfile(uid: String) async throws -> UserProfile? {
        let doc = try await db.collection("users").document(uid).getDocument()
        guard doc.exists else { return nil }
        var profile = try doc.data(as: UserProfile.self)
        profile.id = doc.documentID
        return profile
    }

    public func updateUserProfile(uid: String, fields: [String: Any]) async throws {
        try await db.collection("users").document(uid).updateData(fields)
    }

    // MARK: - Meals

    public func saveMeal(uid: String, meal: Meal) throws -> String {
        let ref = try db.collection("users").document(uid).collection("meals").addDocument(from: meal)
        return ref.documentID
    }

    public func getMeals(uid: String, limit: Int = 50) async throws -> [Meal] {
        let snapshot = try await db.collection("users").document(uid).collection("meals")
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var meal = try? doc.data(as: Meal.self)
            meal?.id = doc.documentID
            return meal
        }
    }

    // MARK: - Symptoms

    public func saveSymptom(uid: String, symptom: Symptom) throws -> String {
        let ref = try db.collection("users").document(uid).collection("symptoms").addDocument(from: symptom)
        return ref.documentID
    }

    public func getSymptoms(uid: String, limit: Int = 50) async throws -> [Symptom] {
        let snapshot = try await db.collection("users").document(uid).collection("symptoms")
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var symptom = try? doc.data(as: Symptom.self)
            symptom?.id = doc.documentID
            return symptom
        }
    }

    /// Marks a symptom as resolved by setting its endedAt to now.
    public func resolveSymptom(uid: String, symptomId: String, endedAt: Date = Date()) async throws {
        try await db
            .collection("users").document(uid)
            .collection("symptoms").document(symptomId)
            .updateData(["endedAt": Timestamp(date: endedAt)])
    }

    // MARK: - Poop Logs

    public func savePoopLog(uid: String, log: PoopLog) throws -> String {
        let ref = try db.collection("users").document(uid).collection("poopLogs").addDocument(from: log)
        return ref.documentID
    }

    public func getPoopLogs(uid: String, limit: Int = 50) async throws -> [PoopLog] {
        let snapshot = try await db.collection("users").document(uid).collection("poopLogs")
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var log = try? doc.data(as: PoopLog.self)
            log?.id = doc.documentID
            return log
        }
    }

    // MARK: - Photo URL Updates

    public func updateMealPhotoUrl(uid: String, mealId: String, photoUrl: String) async throws {
        try await db.collection("users").document(uid).collection("meals").document(mealId)
            .updateData(["photoUrl": photoUrl])
    }

    public func updatePoopLogPhotoUrl(uid: String, logId: String, photoUrl: String) async throws {
        try await db.collection("users").document(uid).collection("poopLogs").document(logId)
            .updateData(["photoUrl": photoUrl])
    }

    // MARK: - Daily Summaries

    public func getDailySummary(uid: String, date: String) async throws -> DailySummary? {
        let doc = try await db.collection("users").document(uid).collection("dailySummaries").document(date).getDocument()
        guard doc.exists else { return nil }
        var summary = try doc.data(as: DailySummary.self)
        summary.id = doc.documentID
        return summary
    }

    // MARK: - Correlation Reports

    public func getCorrelationReports(uid: String, limit: Int = 20) async throws -> [CorrelationReport] {
        let snapshot = try await db.collection("users").document(uid).collection("correlationReports")
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var report = try? doc.data(as: CorrelationReport.self)
            report?.id = doc.documentID
            return report
        }
    }

    // MARK: - Delete Entries

    public func deleteMeal(uid: String, mealId: String) async throws {
        try await db.collection("users").document(uid).collection("meals").document(mealId).delete()
    }

    public func deleteSymptom(uid: String, symptomId: String) async throws {
        try await db.collection("users").document(uid).collection("symptoms").document(symptomId).delete()
    }

    public func deletePoopLog(uid: String, logId: String) async throws {
        try await db.collection("users").document(uid).collection("poopLogs").document(logId).delete()
    }

    public func deleteCorrelationReport(uid: String, reportId: String) async throws {
        try await db.collection("users").document(uid).collection("correlationReports").document(reportId).delete()
    }

    // MARK: - Meals by Date

    public func getMealsForDate(uid: String, date: Date) async throws -> [Meal] {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        guard let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay) else { return [] }
        let snapshot = try await db.collection("users").document(uid).collection("meals")
            .whereField("createdAt", isGreaterThanOrEqualTo: startOfDay)
            .whereField("createdAt", isLessThan: endOfDay)
            .order(by: "createdAt", descending: true)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var meal = try? doc.data(as: Meal.self)
            meal?.id = doc.documentID
            return meal
        }
    }

    public func getSymptomsForDate(uid: String, date: Date) async throws -> [Symptom] {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        guard let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay) else { return [] }
        let snapshot = try await db.collection("users").document(uid).collection("symptoms")
            .whereField("createdAt", isGreaterThanOrEqualTo: startOfDay)
            .whereField("createdAt", isLessThan: endOfDay)
            .order(by: "createdAt", descending: true)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var symptom = try? doc.data(as: Symptom.self)
            symptom?.id = doc.documentID
            return symptom
        }
    }

    public func getPoopLogsForDate(uid: String, date: Date) async throws -> [PoopLog] {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        guard let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay) else { return [] }
        let snapshot = try await db.collection("users").document(uid).collection("poopLogs")
            .whereField("createdAt", isGreaterThanOrEqualTo: startOfDay)
            .whereField("createdAt", isLessThan: endOfDay)
            .order(by: "createdAt", descending: true)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var log = try? doc.data(as: PoopLog.self)
            log?.id = doc.documentID
            return log
        }
    }

    // MARK: - Weekly Insights

    public func getWeeklyInsights(uid: String, limit: Int = 10) async throws -> [WeeklyInsight] {
        let snapshot = try await db.collection("users").document(uid).collection("weeklyInsights")
            .order(by: "generatedAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        return snapshot.documents.compactMap { doc in
            var insight = try? doc.data(as: WeeklyInsight.self)
            insight?.id = doc.documentID
            return insight
        }
    }

    // MARK: - FODMAP Database

    public func getFodmapFoods() async throws -> [FodmapFood] {
        let snapshot = try await db.collection("fodmapDatabase").getDocuments()
        return snapshot.documents.compactMap { doc in
            var food = try? doc.data(as: FodmapFood.self)
            food?.id = doc.documentID
            return food
        }
    }

    public func searchFodmapFoods(query: String) async throws -> [FodmapFood] {
        let all = try await getFodmapFoods()
        let lowered = query.lowercased()
        return all.filter { $0.name.lowercased().contains(lowered) }
    }

    // MARK: - Delete All User Data

    public func deleteUserData(uid: String) async throws {
        let userRef = db.collection("users").document(uid)
        let subcollections = ["meals", "symptoms", "poopLogs", "dailySummaries", "correlations", "correlationReports", "weeklyInsights"]
        for name in subcollections {
            let snapshot = try await userRef.collection(name).getDocuments()
            for doc in snapshot.documents {
                try await doc.reference.delete()
            }
        }
        try await userRef.delete()
    }
}
