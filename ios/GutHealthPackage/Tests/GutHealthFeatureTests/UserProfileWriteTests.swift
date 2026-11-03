import Testing
@testable import GutHealthFeature

@Suite("User profile writes")
struct UserProfileWriteTests {

    @Test("Dotted keys become nested maps for merge writes")
    func nestedDottedKeys() {
        let nested = FirestoreService.nested([
            "preferences.notificationsEnabled": true,
            "preferences.timezone": "America/Denver",
            "preferences.reminderTimes": ["08:00", "12:00"],
            "fcmToken": "abc",
        ])
        let prefs = nested["preferences"] as? [String: Any]
        #expect(prefs?["notificationsEnabled"] as? Bool == true)
        #expect(prefs?["timezone"] as? String == "America/Denver")
        #expect(prefs?["reminderTimes"] as? [String] == ["08:00", "12:00"])
        #expect(nested["fcmToken"] as? String == "abc")
        #expect(nested["preferences.timezone"] == nil)
    }

    @Test("Keys without dots are unchanged")
    func flatKeys() {
        let nested = FirestoreService.nested(["fodmapPhase": "elimination"])
        #expect(nested["fodmapPhase"] as? String == "elimination")
        #expect(nested.count == 1)
    }
}
