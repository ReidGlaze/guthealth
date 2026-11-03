import Foundation

public enum FodmapPhase: String, Codable, Sendable, CaseIterable {
    case elimination, reintroduction, maintenance
}

public struct UserPreferences: Codable, Sendable {
    public var reminderTimes: [String]
    public var timezone: String
    public var notificationsEnabled: Bool

    public init(reminderTimes: [String] = ["08:00", "12:00", "18:00"], timezone: String = TimeZone.current.identifier, notificationsEnabled: Bool = false) {
        self.reminderTimes = reminderTimes
        self.timezone = timezone
        self.notificationsEnabled = notificationsEnabled
    }

    // Profiles can be partial (created by a merge write of only the fields being saved),
    // so every field falls back to its default instead of failing to decode.
    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        let defaults = UserPreferences()
        reminderTimes = try c.decodeIfPresent([String].self, forKey: .reminderTimes) ?? defaults.reminderTimes
        timezone = try c.decodeIfPresent(String.self, forKey: .timezone) ?? defaults.timezone
        notificationsEnabled = try c.decodeIfPresent(Bool.self, forKey: .notificationsEnabled) ?? false
    }
}

public struct UserProfile: Codable, Identifiable, Sendable {
    public var id: String?
    public var displayName: String
    public var createdAt: Date
    public var preferences: UserPreferences
    public var fodmapPhase: FodmapPhase
    public var fodmapPhaseStartDate: Date
    public var fcmToken: String?

    public init(id: String? = nil, displayName: String, createdAt: Date = Date(), preferences: UserPreferences = UserPreferences(), fodmapPhase: FodmapPhase = .elimination, fodmapPhaseStartDate: Date = Date(), fcmToken: String? = nil) {
        self.id = id
        self.displayName = displayName
        self.createdAt = createdAt
        self.preferences = preferences
        self.fodmapPhase = fodmapPhase
        self.fodmapPhaseStartDate = fodmapPhaseStartDate
        self.fcmToken = fcmToken
    }

    private enum CodingKeys: String, CodingKey {
        case id, displayName, createdAt, preferences, fodmapPhase, fodmapPhaseStartDate, fcmToken
    }

    // Tolerates partial profile documents (see UserPreferences.init(from:)).
    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decodeIfPresent(String.self, forKey: .id)
        displayName = try c.decodeIfPresent(String.self, forKey: .displayName) ?? ""
        createdAt = try c.decodeIfPresent(Date.self, forKey: .createdAt) ?? Date()
        preferences = try c.decodeIfPresent(UserPreferences.self, forKey: .preferences) ?? UserPreferences()
        fodmapPhase = (try? c.decodeIfPresent(FodmapPhase.self, forKey: .fodmapPhase)) ?? .elimination
        fodmapPhaseStartDate = try c.decodeIfPresent(Date.self, forKey: .fodmapPhaseStartDate) ?? Date()
        fcmToken = try c.decodeIfPresent(String.self, forKey: .fcmToken)
    }
}
