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
}
