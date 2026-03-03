import Foundation

/// Manages the FODMAP food database with Firestore as source of truth
/// and UserDefaults for offline caching. Falls back to bundled data on first launch.
@Observable
public final class FodmapRepository: @unchecked Sendable {
    public static let shared = FodmapRepository()

    public private(set) var foods: [FodmapFood] = []

    private let cacheKey = "cachedFodmapFoods"
    private let cacheTimestampKey = "fodmapCacheTimestamp"
    private let cacheTTL: TimeInterval = 24 * 60 * 60 // 24 hours

    private init() {
        foods = loadFromCache() ?? FodmapDatabase.bundledFoods
    }

    // MARK: - Public API

    /// Refreshes from Firestore if cache is stale (>24h) or if forced.
    public func refreshIfNeeded(force: Bool = false) async {
        guard force || isCacheStale else { return }
        do {
            let fetched = try await FirestoreService.shared.getFodmapFoods()
            guard !fetched.isEmpty else { return }
            foods = fetched
            saveToCache(fetched)
        } catch {
            // Keep current data on failure
        }
    }

    /// Search foods by name with case-insensitive partial matching.
    public func search(_ query: String) -> [FodmapFood] {
        let trimmed = query.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return foods }
        return foods.filter { $0.name.localizedCaseInsensitiveContains(trimmed) }
    }

    /// Find the best match for a food name.
    public func lookup(_ name: String) -> FodmapFood? {
        let trimmed = name.trimmingCharacters(in: .whitespaces).lowercased()
        guard !trimmed.isEmpty else { return nil }
        if let exact = foods.first(where: { $0.name.lowercased() == trimmed }) {
            return exact
        }
        return foods.first(where: { $0.name.lowercased().contains(trimmed) || trimmed.contains($0.name.lowercased()) })
    }

    // MARK: - Cache

    private var isCacheStale: Bool {
        guard let timestamp = UserDefaults.standard.object(forKey: cacheTimestampKey) as? Date else {
            return true
        }
        return Date().timeIntervalSince(timestamp) > cacheTTL
    }

    private func loadFromCache() -> [FodmapFood]? {
        guard let data = UserDefaults.standard.data(forKey: cacheKey) else { return nil }
        return try? JSONDecoder().decode([FodmapFood].self, from: data)
    }

    private func saveToCache(_ foods: [FodmapFood]) {
        if let data = try? JSONEncoder().encode(foods) {
            UserDefaults.standard.set(data, forKey: cacheKey)
            UserDefaults.standard.set(Date(), forKey: cacheTimestampKey)
        }
    }
}
