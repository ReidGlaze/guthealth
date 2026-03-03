import Foundation

public enum StoolColor: String, Codable, Sendable, CaseIterable {
    case brown, dark, light, green, yellow, red, black
}

public enum Urgency: String, Codable, Sendable, CaseIterable {
    case normal, urgent, emergency
}

public struct AIClassification: Codable, Sendable {
    public var bristolType: Int
    public var color: String
    public var observations: String
    public var analyzedAt: Date

    public init(bristolType: Int, color: String, observations: String = "", analyzedAt: Date) {
        self.bristolType = bristolType
        self.color = color
        self.observations = observations
        self.analyzedAt = analyzedAt
    }
}

public struct PoopLog: Codable, Identifiable, Sendable {
    public var id: String?
    public var createdAt: Date
    public var bristolType: Int
    public var color: StoolColor
    public var urgency: Urgency
    public var photoUrl: String?
    public var aiClassification: AIClassification?
    public var notes: String

    public init(id: String? = nil, createdAt: Date = Date(), bristolType: Int, color: StoolColor = .brown, urgency: Urgency = .normal, photoUrl: String? = nil, aiClassification: AIClassification? = nil, notes: String = "") {
        self.id = id
        self.createdAt = createdAt
        self.bristolType = bristolType
        self.color = color
        self.urgency = urgency
        self.photoUrl = photoUrl
        self.aiClassification = aiClassification
        self.notes = notes
    }

    public static let bristolDescriptions: [Int: String] = [
        1: "Separate hard lumps",
        2: "Lumpy, sausage-shaped",
        3: "Sausage with cracks",
        4: "Smooth, soft sausage",
        5: "Soft blobs with clear edges",
        6: "Fluffy, mushy pieces",
        7: "Watery, no solid pieces"
    ]
}
