import Foundation

public enum MealType: String, Codable, Sendable, CaseIterable {
    case breakfast, lunch, dinner, snack
}

public enum FodmapLevel: String, Codable, Sendable {
    case low, moderate, high, unknown
}

public struct MealFood: Sendable {
    public var name: String
    public var fodmapLevel: FodmapLevel
    public var fodmapCategories: [String]
    public var servingSize: String
    public var lowFodmapServing: String
    public var triggers: [String]

    public init(name: String, fodmapLevel: FodmapLevel, fodmapCategories: [String] = [], servingSize: String = "", lowFodmapServing: String = "", triggers: [String] = []) {
        self.name = name
        self.fodmapLevel = fodmapLevel
        self.fodmapCategories = fodmapCategories
        self.servingSize = servingSize
        self.lowFodmapServing = lowFodmapServing
        self.triggers = triggers
    }
}

extension MealFood: Codable {
    enum CodingKeys: String, CodingKey {
        case name, fodmapLevel, fodmapCategories, servingSize, lowFodmapServing, triggers
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        name = try container.decode(String.self, forKey: .name)
        fodmapLevel = try container.decode(FodmapLevel.self, forKey: .fodmapLevel)
        fodmapCategories = (try? container.decode([String].self, forKey: .fodmapCategories)) ?? []
        servingSize = (try? container.decode(String.self, forKey: .servingSize)) ?? ""
        lowFodmapServing = (try? container.decode(String.self, forKey: .lowFodmapServing)) ?? ""
        triggers = (try? container.decode([String].self, forKey: .triggers)) ?? []
    }
}

public struct AIAnalysis: Codable, Sendable {
    public var rawResponse: String
    public var analyzedAt: Date

    public init(rawResponse: String, analyzedAt: Date) {
        self.rawResponse = rawResponse
        self.analyzedAt = analyzedAt
    }
}

public struct Meal: Codable, Identifiable, Sendable {
    public var id: String?
    public var createdAt: Date
    public var mealType: MealType
    public var photoUrl: String?
    public var foods: [MealFood]
    public var notes: String
    public var aiAnalysis: AIAnalysis?

    public init(id: String? = nil, createdAt: Date = Date(), mealType: MealType, photoUrl: String? = nil, foods: [MealFood] = [], notes: String = "", aiAnalysis: AIAnalysis? = nil) {
        self.id = id
        self.createdAt = createdAt
        self.mealType = mealType
        self.photoUrl = photoUrl
        self.foods = foods
        self.notes = notes
        self.aiAnalysis = aiAnalysis
    }
}
