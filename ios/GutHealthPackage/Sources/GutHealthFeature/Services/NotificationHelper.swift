import UserNotifications
import UIKit
import FirebaseMessaging

public enum NotificationHelper {
    @MainActor
    public static func requestPermissionAndRegister() async -> Bool {
        do {
            let granted = try await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .badge, .sound])
            if granted {
                UIApplication.shared.registerForRemoteNotifications()
            }
            return granted
        } catch {
            return false
        }
    }

    /// The FCM token, or nil if it isn't available yet. Right after registering, the APNs
    /// token can take a moment to arrive, so this retries briefly. If it still fails, the
    /// MessagingDelegate saves the token once it arrives.
    @MainActor
    public static func fcmToken() async -> String? {
        for attempt in 0..<3 {
            if let token = try? await Messaging.messaging().token() { return token }
            if attempt < 2 { try? await Task.sleep(for: .seconds(1)) }
        }
        return nil
    }

    /// Default reminder times used when reminders are enabled from onboarding.
    public static let defaultReminderTimes = ["08:00", "12:00", "18:00"]
}
