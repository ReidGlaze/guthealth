import Foundation
import HealthKit

/// Optional, write-only sync of symptom logs to Apple Health.
///
/// Off by default — nothing happens (and no permission prompt appears) until the user
/// turns it on in Settings. Every call is best-effort: failures are logged and ignored
/// so Apple Health can never block or break saving a log in the app.
///
/// Gas and general pain have no Apple Health equivalent and are not synced.
@MainActor
public final class HealthKitService {
    public static let shared = HealthKitService()

    private let store = HKHealthStore()
    private let enabledKey = "appleHealthEnabled"

    private init() {}

    /// False on iPads without the Health app.
    public var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    public var isEnabled: Bool {
        get { UserDefaults.standard.bool(forKey: enabledKey) }
        set { UserDefaults.standard.set(newValue, forKey: enabledKey) }
    }

    private static let typeMap: [SymptomType: HKCategoryTypeIdentifier] = [
        .bloating: .bloating,
        .cramping: .abdominalCramps,
        .constipation: .constipation,
        .diarrhea: .diarrhea,
        .heartburn: .heartburn,
        .nausea: .nausea,
    ]

    private var shareTypes: Set<HKSampleType> {
        Set(Self.typeMap.values.map { HKCategoryType($0) })
    }

    /// Shows Apple's permission sheet (only the first time). Returns true if the user
    /// allowed writing at least one symptom type.
    public func requestAuthorization() async -> Bool {
        guard isAvailable else { return false }
        do {
            try await store.requestAuthorization(toShare: shareTypes, read: [])
        } catch {
            print("HealthKit authorization failed: \(error)")
            return false
        }
        return shareTypes.contains { store.authorizationStatus(for: $0) == .sharingAuthorized }
    }

    /// Writes a symptom to Apple Health. Safe to call repeatedly for the same symptom:
    /// the Firestore id is used as the sync identifier, so Health keeps a single copy.
    public func saveSymptom(_ symptom: Symptom, id: String) async {
        guard isEnabled, isAvailable, let sample = Self.sample(for: symptom, id: id),
              store.authorizationStatus(for: sample.sampleType) == .sharingAuthorized
        else { return }
        do {
            try await store.save(sample)
        } catch {
            print("HealthKit save failed: \(error)")
        }
    }

    /// Backfills existing symptom logs after the user turns the feature on.
    public func saveSymptoms(_ symptoms: [Symptom]) async {
        // One at a time: an already-synced symptom (same sync id) can fail on its own
        // without blocking the rest.
        for symptom in symptoms {
            guard let id = symptom.id else { continue }
            await saveSymptom(symptom, id: id)
        }
    }

    /// Removes the Apple Health copy of a symptom deleted in the app.
    public func deleteSymptom(id: String) async {
        guard isAvailable else { return }
        let predicate = HKQuery.predicateForObjects(
            withMetadataKey: HKMetadataKeyExternalUUID, allowedValues: [id]
        )
        for type in shareTypes where store.authorizationStatus(for: type) == .sharingAuthorized {
            _ = try? await store.deleteObjects(of: type, predicate: predicate)
        }
    }

    /// Removes every symptom this app wrote to Apple Health (used by "Delete All Data").
    public func deleteAllSymptoms() async {
        guard isAvailable else { return }
        let predicate = HKQuery.predicateForObjects(from: HKSource.default())
        for type in shareTypes where store.authorizationStatus(for: type) == .sharingAuthorized {
            _ = try? await store.deleteObjects(of: type, predicate: predicate)
        }
    }

    private static func sample(for symptom: Symptom, id: String) -> HKCategorySample? {
        guard let identifier = typeMap[symptom.type] else { return nil }
        let end = max(symptom.endedAt ?? symptom.createdAt, symptom.createdAt)
        return HKCategorySample(
            type: HKCategoryType(identifier),
            value: severity(symptom.severity).rawValue,
            start: symptom.createdAt,
            end: end,
            metadata: [
                HKMetadataKeyExternalUUID: id,
                HKMetadataKeySyncIdentifier: id,
                HKMetadataKeySyncVersion: 1,
            ]
        )
    }

    /// App severity is 1–10; Apple Health uses mild / moderate / severe.
    private static func severity(_ value: Int) -> HKCategoryValueSeverity {
        switch value {
        case ...3: return .mild
        case 4...6: return .moderate
        default: return .severe
        }
    }
}
