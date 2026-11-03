import Foundation
import FirebaseAnalytics

/// Decides when to ask for an App Store rating.
///
/// Two triggers:
/// - Engagement: after a positive log (meal, or a normal Bristol 3–4 stool) once the user
///   has logged at least `minLogs` entries across `minDistinctDays` different days.
///   Fires at most once per app version.
/// - Correlation report: after the 1st and every 3rd successful report.
///
/// Apple caps the actual prompt at 3 displays per year and never says whether it showed,
/// so `review_prompt_requested` counts requests, not displays.
@MainActor
public enum ReviewPrompter {
    static let minLogs = 5
    static let minDistinctDays = 3
    static let cooldownDays = 14

    private static let defaults = UserDefaults.standard
    private enum Key {
        static let logCount = "reviewLogCount"
        static let logDays = "reviewLogDays"
        static let lastPromptDate = "reviewLastPromptDate"
        static let engagementPromptVersion = "reviewEngagementPromptVersion"
        static let pendingPrompt = "reviewPendingPrompt"
        static let correlationReportCount = "correlationReportCount"
    }

    private static var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "unknown"
    }

    private static let dayFormatter: DateFormatter = {
        let f = DateFormatter()
        f.calendar = Calendar(identifier: .gregorian)
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "yyyy-MM-dd"
        return f
    }()

    /// Call after any log is saved. Symptom logs and abnormal stools count toward
    /// engagement but never trigger the prompt themselves.
    public static func recordLog(isPositiveMoment: Bool) {
        let count = defaults.integer(forKey: Key.logCount) + 1
        defaults.set(count, forKey: Key.logCount)

        var days = Set(defaults.stringArray(forKey: Key.logDays) ?? [])
        if days.count < minDistinctDays {
            days.insert(dayFormatter.string(from: Date()))
            defaults.set(Array(days), forKey: Key.logDays)
        }

        guard isPositiveMoment,
              count >= minLogs,
              days.count >= minDistinctDays,
              defaults.string(forKey: Key.engagementPromptVersion) != appVersion,
              !inCooldown
        else { return }

        defaults.set(appVersion, forKey: Key.engagementPromptVersion)
        defaults.set(true, forKey: Key.pendingPrompt)
    }

    /// Returns true (once) if a log just earned a prompt. Call after the log sheet dismisses.
    public static func consumePendingPrompt() -> Bool {
        guard defaults.bool(forKey: Key.pendingPrompt) else { return false }
        defaults.set(false, forKey: Key.pendingPrompt)
        markRequested(trigger: "engagement")
        return true
    }

    /// Call after a successful correlation report. Returns true if the prompt should be requested.
    public static func recordCorrelationReport() -> Bool {
        let count = defaults.integer(forKey: Key.correlationReportCount) + 1
        defaults.set(count, forKey: Key.correlationReportCount)
        guard count == 1 || count % 3 == 0 else { return false }
        markRequested(trigger: "correlation_report")
        return true
    }

    private static var inCooldown: Bool {
        guard let last = defaults.object(forKey: Key.lastPromptDate) as? Date else { return false }
        return Date().timeIntervalSince(last) < TimeInterval(cooldownDays * 24 * 60 * 60)
    }

    private static func markRequested(trigger: String) {
        defaults.set(Date(), forKey: Key.lastPromptDate)
        Analytics.logEvent("review_prompt_requested", parameters: [
            "trigger": trigger,
            "log_count": defaults.integer(forKey: Key.logCount),
        ])
    }
}
