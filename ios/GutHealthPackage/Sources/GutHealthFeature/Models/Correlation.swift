import Foundation

public struct Correlation: Codable, Identifiable, Sendable {
    public var id: String?
    public var createdAt: Date
    public var triggerFood: String
    public var symptomType: String
    public var confidence: Double
    public var occurrences: Int
    public var avgTimeLag: Double
    public var recommendation: String
    public var status: CorrelationStatus

    // New fields from improved correlation engine (all optional for backward compatibility)
    public var confidenceLabel: ConfidenceLabel?
    public var totalExposures: Int?
    public var symptomaticExposures: Int?
    public var hitRate: Double?
    public var possibleConfound: Bool?
    public var confoundNote: String?
    public var confoundRisk: ConfoundRisk?
    public var bristolImpact: String?

    // Insufficient data fields — true when totalExposures < 3
    public var insufficientData: Bool?
    public var insufficientDataNote: String?

    public init(
        id: String? = nil,
        createdAt: Date = Date(),
        triggerFood: String,
        symptomType: String,
        confidence: Double,
        occurrences: Int,
        avgTimeLag: Double,
        recommendation: String = "",
        status: CorrelationStatus = .confirmed,
        confidenceLabel: ConfidenceLabel? = nil,
        totalExposures: Int? = nil,
        symptomaticExposures: Int? = nil,
        hitRate: Double? = nil,
        possibleConfound: Bool? = nil,
        confoundNote: String? = nil,
        confoundRisk: ConfoundRisk? = nil,
        bristolImpact: String? = nil,
        insufficientData: Bool? = nil,
        insufficientDataNote: String? = nil
    ) {
        self.id = id
        self.createdAt = createdAt
        self.triggerFood = triggerFood
        self.symptomType = symptomType
        self.confidence = confidence
        self.occurrences = occurrences
        self.avgTimeLag = avgTimeLag
        self.recommendation = recommendation
        self.status = status
        self.confidenceLabel = confidenceLabel
        self.totalExposures = totalExposures
        self.symptomaticExposures = symptomaticExposures
        self.hitRate = hitRate
        self.possibleConfound = possibleConfound
        self.confoundNote = confoundNote
        self.confoundRisk = confoundRisk
        self.bristolImpact = bristolImpact
        self.insufficientData = insufficientData
        self.insufficientDataNote = insufficientDataNote
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(String.self, forKey: .id)
        createdAt = try container.decode(Date.self, forKey: .createdAt)
        triggerFood = try container.decode(String.self, forKey: .triggerFood)
        symptomType = try container.decode(String.self, forKey: .symptomType)
        confidence = try container.decode(Double.self, forKey: .confidence)
        occurrences = try container.decode(Int.self, forKey: .occurrences)
        avgTimeLag = try container.decode(Double.self, forKey: .avgTimeLag)
        recommendation = (try container.decodeIfPresent(String.self, forKey: .recommendation)) ?? ""
        status = (try container.decodeIfPresent(CorrelationStatus.self, forKey: .status)) ?? .confirmed
        confidenceLabel = try container.decodeIfPresent(ConfidenceLabel.self, forKey: .confidenceLabel)
        totalExposures = try container.decodeIfPresent(Int.self, forKey: .totalExposures)
        symptomaticExposures = try container.decodeIfPresent(Int.self, forKey: .symptomaticExposures)
        hitRate = try container.decodeIfPresent(Double.self, forKey: .hitRate)
        possibleConfound = try container.decodeIfPresent(Bool.self, forKey: .possibleConfound)
        confoundNote = try container.decodeIfPresent(String.self, forKey: .confoundNote)
        confoundRisk = try container.decodeIfPresent(ConfoundRisk.self, forKey: .confoundRisk)
        bristolImpact = try container.decodeIfPresent(String.self, forKey: .bristolImpact)
        insufficientData = try container.decodeIfPresent(Bool.self, forKey: .insufficientData)
        insufficientDataNote = try container.decodeIfPresent(String.self, forKey: .insufficientDataNote)
    }
}

public enum CorrelationStatus: String, Codable, Sendable {
    case confirmed
    case suspected
}

/// Richer confidence label from the improved correlation engine.
public enum ConfidenceLabel: String, Codable, Sendable {
    case suspected
    case probable
    case likely
    case strong
}

/// Confound risk level indicating stacking confound probability.
public enum ConfoundRisk: String, Codable, Sendable {
    case high
    case moderate
    case low
}
