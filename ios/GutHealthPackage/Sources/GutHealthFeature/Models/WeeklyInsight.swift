import Foundation

public struct WeeklyInsight: Codable, Identifiable, Sendable {
    public var id: String?
    public var weekStart: String
    public var weekEnd: String
    public var gutScoreTrend: [Int]
    public var avgGutScore: Double
    public var topTriggers: [String]
    public var improvements: [String]
    public var aiReport: String
    public var generatedAt: Date

    public init(id: String? = nil, weekStart: String, weekEnd: String, gutScoreTrend: [Int] = [], avgGutScore: Double = 0, topTriggers: [String] = [], improvements: [String] = [], aiReport: String = "", generatedAt: Date = Date()) {
        self.id = id
        self.weekStart = weekStart
        self.weekEnd = weekEnd
        self.gutScoreTrend = gutScoreTrend
        self.avgGutScore = avgGutScore
        self.topTriggers = topTriggers
        self.improvements = improvements
        self.aiReport = aiReport
        self.generatedAt = generatedAt
    }
}
