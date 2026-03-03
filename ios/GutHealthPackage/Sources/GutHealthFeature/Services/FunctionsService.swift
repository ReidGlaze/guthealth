import Foundation
import FirebaseFunctions

public actor FunctionsService {
    public static let shared = FunctionsService()
    private let functions = Functions.functions()

    private init() {}

    public func analyzeFoodPhoto(imageBase64: String, mealType: String?) async throws -> [MealFood] {
        var data: [String: Any] = ["imageBase64": imageBase64]
        if let mealType { data["mealType"] = mealType }

        let result = try await functions.httpsCallable("analyzeFoodPhoto").call(data)

        guard let response = result.data as? [String: Any],
              let foodsArray = response["foods"] as? [[String: Any]] else {
            return []
        }

        return foodsArray.compactMap { dict in
            guard let name = dict["name"] as? String else { return nil }
            let levelStr = dict["fodmapLevel"] as? String ?? "unknown"
            let level = FodmapLevel(rawValue: levelStr) ?? .unknown
            let categories = dict["fodmapCategories"] as? [String] ?? []
            let serving = dict["servingSize"] as? String ?? ""
            let lowFodmapServing = dict["lowFodmapServing"] as? String ?? ""
            let triggers = dict["triggers"] as? [String] ?? []
            return MealFood(name: name, fodmapLevel: level, fodmapCategories: categories, servingSize: serving, lowFodmapServing: lowFodmapServing, triggers: triggers)
        }
    }

    public func classifyPoopPhoto(imageBase64: String) async throws -> AIClassification {
        let result = try await functions.httpsCallable("classifyPoopPhoto").call(["imageBase64": imageBase64])

        guard let response = result.data as? [String: Any],
              let bristolType = response["bristolType"] as? Int,
              let color = response["color"] as? String else {
            throw FunctionsError.invalidResponse
        }

        let observations = response["observations"] as? String ?? ""
        return AIClassification(bristolType: bristolType, color: color, observations: observations, analyzedAt: Date())
    }

    public struct CorrelationEngineResult: Sendable {
        public let report: CorrelationReport?
        public let message: String?
    }

    public func runCorrelationEngine(daysBack: Int = 7) async throws -> CorrelationEngineResult {
        let result = try await functions.httpsCallable("runCorrelationEngine").call(["daysBack": daysBack])
        let response = result.data as? [String: Any] ?? [:]

        if let message = response["message"] as? String {
            return CorrelationEngineResult(report: nil, message: message)
        }

        guard let reportId = response["reportId"] as? String,
              let aiReport = response["aiReport"] as? String else {
            return CorrelationEngineResult(report: nil, message: "Unexpected response from server")
        }

        let report = CorrelationReport(
            id: reportId,
            createdAt: Date(),
            periodStart: response["periodStart"] as? String ?? "",
            periodEnd: response["periodEnd"] as? String ?? "",
            mealsAnalyzed: response["mealsAnalyzed"] as? Int ?? 0,
            symptomsAnalyzed: response["symptomsAnalyzed"] as? Int ?? 0,
            poopLogsAnalyzed: response["poopLogsAnalyzed"] as? Int ?? 0,
            aiReport: aiReport,
            disclaimer: response["disclaimer"] as? String ?? "This is not medical advice"
        )
        return CorrelationEngineResult(report: report, message: nil)
    }
}

public enum FunctionsError: LocalizedError {
    case invalidResponse

    public var errorDescription: String? {
        switch self {
        case .invalidResponse: return "Invalid response from server"
        }
    }
}
