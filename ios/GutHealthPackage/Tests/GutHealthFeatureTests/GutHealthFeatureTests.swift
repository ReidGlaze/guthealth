import Testing
@testable import GutHealthFeature

@Suite("GutHealth Models")
struct GutHealthFeatureTests {

    @Test("Meal creation with defaults")
    func mealCreation() {
        let meal = Meal(mealType: .breakfast)
        #expect(meal.mealType == .breakfast)
        #expect(meal.foods.isEmpty)
        #expect(meal.photoUrl == nil)
        #expect(meal.id == nil)
    }

    @Test("Symptom severity range")
    func symptomSeverity() {
        let symptom = Symptom(type: .bloating, severity: 7)
        #expect(symptom.severity == 7)
        #expect(symptom.type == .bloating)
        #expect(symptom.location == nil)
    }

    @Test("PoopLog Bristol type descriptions")
    func bristolDescriptions() {
        #expect(PoopLog.bristolDescriptions[1] == "Separate hard lumps")
        #expect(PoopLog.bristolDescriptions[4] == "Smooth, soft sausage")
        #expect(PoopLog.bristolDescriptions[7] == "Watery, no solid pieces")
    }

    @Test("UserProfile default preferences")
    func userProfileDefaults() {
        let profile = UserProfile(displayName: "Test User")
        #expect(profile.fodmapPhase == .elimination)
        #expect(profile.preferences.reminderTimes.count == 3)
        #expect(profile.preferences.notificationsEnabled == true)
    }

    @Test("FodmapFood creation")
    func fodmapFoodCreation() {
        let food = FodmapFood(name: "Apple", category: "fruit", fodmapLevel: .high, fodmapCategories: ["fructose", "sorbitol"])
        #expect(food.name == "Apple")
        #expect(food.fodmapLevel == .high)
        #expect(food.fodmapCategories.count == 2)
    }

    @Test("MealFood with FODMAP info")
    func mealFoodFodmap() {
        let food = MealFood(name: "Garlic", fodmapLevel: .high, fodmapCategories: ["fructans"])
        #expect(food.fodmapLevel == .high)
        #expect(food.fodmapCategories.contains("fructans"))
    }

    @Test("DailySummary gut score")
    func dailySummaryScore() {
        let summary = DailySummary(date: "2026-02-28", gutScore: 75)
        #expect(summary.gutScore == 75)
        #expect(summary.totalMeals == 0)
    }

    @Test("Correlation creation")
    func correlationCreation() {
        let correlation = Correlation(triggerFood: "Onion", symptomType: "bloating", confidence: 0.85, occurrences: 5, avgTimeLag: 3.5)
        #expect(correlation.confidence == 0.85)
        #expect(correlation.occurrences == 5)
    }
}
