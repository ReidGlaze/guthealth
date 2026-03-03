import Foundation

/// A single AI-generated narrative correlation analysis report.
/// Stored in `/users/{userId}/correlationReports/{reportId}`.
public struct CorrelationReport: Codable, Identifiable, Sendable {
    public var id: String?
    public var createdAt: Date
    /// ISO date string — first day of the analysis window (7 days before the run).
    public var periodStart: String
    /// ISO date string — last day of the analysis window (day of the run).
    public var periodEnd: String
    public var mealsAnalyzed: Int
    public var symptomsAnalyzed: Int
    public var poopLogsAnalyzed: Int
    /// Full plain-English narrative from Gemini.
    public var aiReport: String
    /// Always "This is not medical advice".
    public var disclaimer: String

    public init(
        id: String? = nil,
        createdAt: Date = Date(),
        periodStart: String,
        periodEnd: String,
        mealsAnalyzed: Int = 0,
        symptomsAnalyzed: Int = 0,
        poopLogsAnalyzed: Int = 0,
        aiReport: String,
        disclaimer: String = "This is not medical advice"
    ) {
        self.id = id
        self.createdAt = createdAt
        self.periodStart = periodStart
        self.periodEnd = periodEnd
        self.mealsAnalyzed = mealsAnalyzed
        self.symptomsAnalyzed = symptomsAnalyzed
        self.poopLogsAnalyzed = poopLogsAnalyzed
        self.aiReport = aiReport
        self.disclaimer = disclaimer
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(String.self, forKey: .id)
        createdAt = (try? container.decode(Date.self, forKey: .createdAt)) ?? Date()
        periodStart = try container.decode(String.self, forKey: .periodStart)
        periodEnd = try container.decode(String.self, forKey: .periodEnd)
        mealsAnalyzed = (try? container.decode(Int.self, forKey: .mealsAnalyzed)) ?? 0
        symptomsAnalyzed = (try? container.decode(Int.self, forKey: .symptomsAnalyzed)) ?? 0
        poopLogsAnalyzed = (try? container.decode(Int.self, forKey: .poopLogsAnalyzed)) ?? 0
        aiReport = (try? container.decode(String.self, forKey: .aiReport)) ?? ""
        disclaimer = (try? container.decode(String.self, forKey: .disclaimer)) ?? "This is not medical advice"
    }
}
