import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore
import FirebaseMessaging
import FirebaseAnalytics
import GutHealthFeature

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        return .portrait
    }

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        Messaging.messaging().delegate = FCMTokenHandler.shared
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}

/// Handles FCM token updates separately to avoid @MainActor isolation issues.
final class FCMTokenHandler: NSObject, MessagingDelegate, @unchecked Sendable {
    static let shared = FCMTokenHandler()

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        guard let uid = Auth.auth().currentUser?.uid else { return }
        let timezone = TimeZone.current.identifier
        Firestore.firestore().collection("users").document(uid).updateData([
            "fcmToken": token,
            "preferences.timezone": timezone
        ]) { error in
            if let error = error {
                print("Failed to save FCM token: \(error.localizedDescription)")
            }
        }
    }
}

@main
struct GutHealthApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
