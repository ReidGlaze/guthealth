import Foundation
import FirebaseAuth

@MainActor @Observable
public final class AuthService {
    public static let shared = AuthService()

    public var currentUser: User?
    public var isAuthenticated = false
    public var isLoading = true

    @ObservationIgnored
    private nonisolated(unsafe) var authStateListener: AuthStateDidChangeListenerHandle?

    private init() {
        setupAuthStateListener()
    }

    deinit {
        if let listener = authStateListener {
            Auth.auth().removeStateDidChangeListener(listener)
        }
    }

    private func setupAuthStateListener() {
        authStateListener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.currentUser = user
                self?.isAuthenticated = user != nil
                self?.isLoading = false
            }
        }
    }

    public func signInAnonymously() async throws -> String {
        let result = try await Auth.auth().signInAnonymously()
        return result.user.uid
    }

    public var currentUserId: String? {
        Auth.auth().currentUser?.uid
    }

    public var isSignedIn: Bool {
        Auth.auth().currentUser != nil
    }

    public func signOut() throws {
        try Auth.auth().signOut()
    }

    public func deleteUser() async throws {
        try await Auth.auth().currentUser?.delete()
    }
}
