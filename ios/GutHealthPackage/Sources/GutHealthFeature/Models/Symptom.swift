import Foundation

public enum SymptomType: String, Codable, Sendable, CaseIterable {
    case bloating, gas, pain, heartburn, nausea, diarrhea, constipation, cramping

    public var icon: String {
        switch self {
        case .bloating: return "wind"
        case .gas: return "cloud"
        case .pain: return "bolt.fill"
        case .heartburn: return "flame"
        case .nausea: return "tornado"
        case .diarrhea: return "drop.fill"
        case .constipation: return "stop.fill"
        case .cramping: return "waveform.path"
        }
    }
}

public enum SymptomLocation: String, Codable, Sendable, CaseIterable {
    case upper, lower, left, right
}

public struct Symptom: Codable, Identifiable, Sendable {
    public var id: String?
    public var createdAt: Date
    public var type: SymptomType
    public var severity: Int
    public var location: SymptomLocation?
    public var notes: String
    /// Optional end time. Nil when the symptom is still ongoing.
    public var endedAt: Date?

    /// Human-readable duration string, e.g. "lasted 3.5 hours". Nil when endedAt is absent.
    public var durationDescription: String? {
        guard let end = endedAt else { return nil }
        let hours = end.timeIntervalSince(createdAt) / 3600
        if hours < 1 {
            let minutes = Int(end.timeIntervalSince(createdAt) / 60)
            return "lasted \(minutes) min"
        }
        let rounded = (hours * 10).rounded() / 10
        let formatted = rounded == rounded.rounded() ? "\(Int(rounded))" : String(format: "%.1f", rounded)
        return "lasted \(formatted) hour\(rounded == 1 ? "" : "s")"
    }

    public init(
        id: String? = nil,
        createdAt: Date = Date(),
        type: SymptomType,
        severity: Int,
        location: SymptomLocation? = nil,
        notes: String = "",
        endedAt: Date? = nil
    ) {
        self.id = id
        self.createdAt = createdAt
        self.type = type
        self.severity = severity
        self.location = location
        self.notes = notes
        self.endedAt = endedAt
    }
}
