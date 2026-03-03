import Foundation

public struct DailySummary: Codable, Identifiable, Sendable {
    public var id: String?
    public var date: String
    public var totalMeals: Int
    public var totalSymptoms: Int
    public var totalPoopLogs: Int
    public var highFodmapCount: Int
    public var avgSymptomSeverity: Double
    public var dominantSymptom: String?
    public var generatedAt: Date
    public var aiSummary: String

    public init(id: String? = nil, date: String, totalMeals: Int = 0, totalSymptoms: Int = 0, totalPoopLogs: Int = 0, highFodmapCount: Int = 0, avgSymptomSeverity: Double = 0, dominantSymptom: String? = nil, generatedAt: Date = Date(), aiSummary: String = "") {
        self.id = id
        self.date = date
        self.totalMeals = totalMeals
        self.totalSymptoms = totalSymptoms
        self.totalPoopLogs = totalPoopLogs
        self.highFodmapCount = highFodmapCount
        self.avgSymptomSeverity = avgSymptomSeverity
        self.dominantSymptom = dominantSymptom
        self.generatedAt = generatedAt
        self.aiSummary = aiSummary
    }
}
