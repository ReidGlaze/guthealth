import Foundation

public struct FodmapFood: Codable, Identifiable, Sendable {
    public var id: String?
    public var name: String
    public var category: String
    public var fodmapLevel: FodmapLevel
    public var fodmapCategories: [String]
    public var servingSize: String
    public var lowFodmapServing: String
    public var notes: String

    public init(id: String? = nil, name: String, category: String, fodmapLevel: FodmapLevel, fodmapCategories: [String] = [], servingSize: String = "", lowFodmapServing: String = "", notes: String = "") {
        self.id = id
        self.name = name
        self.category = category
        self.fodmapLevel = fodmapLevel
        self.fodmapCategories = fodmapCategories
        self.servingSize = servingSize
        self.lowFodmapServing = lowFodmapServing
        self.notes = notes
    }
}
