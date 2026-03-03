import SwiftUI

public enum MainTab: String, CaseIterable, Sendable {
    case dashboard = "Dashboard"
    case log = "Log"
    case fodmap = "FODMAP"
    case insights = "Insights"

    public var icon: String {
        switch self {
        case .dashboard: return "heart.text.clipboard"
        case .log: return "plus.circle.fill"
        case .fodmap: return "leaf.fill"
        case .insights: return "chart.line.uptrend.xyaxis"
        }
    }
}

public struct ContentView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var authService = AuthService.shared
    @State private var selectedTab: MainTab = .dashboard
    @State private var errorMessage: String?

    public init() {}

    public var body: some View {
        Group {
            if authService.isLoading {
                loadingView
            } else if !hasCompletedOnboarding {
                OnboardingView()
            } else {
                mainTabView
            }
        }
        .task {
            await ensureAuthenticated()
        }
        .alert("Error", isPresented: .init(get: { errorMessage != nil }, set: { if !$0 { errorMessage = nil } })) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
    }

    // MARK: - Loading

    private var loadingView: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()
            VStack(spacing: AppSpacing.md) {
                ProgressView()
                    .tint(AppColors.primary)
                Text("AI Gut Health")
                    .font(AppTypography.title2)
                    .foregroundColor(AppColors.text)
            }
        }
    }

    // MARK: - Tab View

    private var mainTabView: some View {
        TabView(selection: $selectedTab) {
            DashboardView()
                .tabItem {
                    Label(MainTab.dashboard.rawValue, systemImage: MainTab.dashboard.icon)
                }
                .tag(MainTab.dashboard)

            LogView()
                .tabItem {
                    Label(MainTab.log.rawValue, systemImage: MainTab.log.icon)
                }
                .tag(MainTab.log)

            FODMAPGuideView()
                .tabItem {
                    Label(MainTab.fodmap.rawValue, systemImage: MainTab.fodmap.icon)
                }
                .tag(MainTab.fodmap)

            InsightsView()
                .tabItem {
                    Label(MainTab.insights.rawValue, systemImage: MainTab.insights.icon)
                }
                .tag(MainTab.insights)
        }
        .tint(AppColors.primary)
    }

    // MARK: - Auth

    private func ensureAuthenticated() async {
        guard !authService.isAuthenticated else { return }
        do {
            _ = try await authService.signInAnonymously()
        } catch {
            errorMessage = "Unable to connect. Please check your internet connection and try again."
        }
    }
}
