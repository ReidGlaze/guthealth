import SwiftUI

public struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var currentPage = 0
    @State private var remindersEnabled = false
    @State private var isSaving = false

    private let authService = AuthService.shared

    public init() {}

    public var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                TabView(selection: $currentPage) {
                    welcomePage.tag(0)
                    howItWorksPage.tag(1)
                    whatToExpectPage.tag(2)
                    getStartedPage.tag(3)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.easeInOut(duration: 0.3), value: currentPage)

                pageIndicatorAndButtons
                    .padding(.bottom, AppSpacing.xl)
            }
        }
    }

    // MARK: - Page 1: Welcome

    private var welcomePage: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()

            Image("onboarding_welcome", bundle: .main)
                .resizable()
                .scaledToFit()
                .frame(height: 200)

            Text("AI Gut Health")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)
                .multilineTextAlignment(.center)

            Text("Tired of guessing what's\nupsetting your stomach?")
                .font(AppTypography.title3)
                .foregroundColor(AppColors.text)
                .multilineTextAlignment(.center)

            Text("Track your food, symptoms, and poop.\nAI finds the patterns you can't.")
                .font(AppTypography.body)
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    // MARK: - Page 2: How It Works

    private var howItWorksPage: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()

            Image("onboarding_how_it_works", bundle: .main)
                .resizable()
                .scaledToFit()
                .frame(height: 160)

            Text("How It Works")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            VStack(spacing: AppSpacing.lg) {
                onboardingStep(
                    icon: "camera.fill",
                    title: "Photograph Your Food",
                    description: "AI identifies what you ate and flags potential triggers"
                )
                onboardingStep(
                    icon: "waveform.path.ecg",
                    title: "Log How You Feel",
                    description: "Record bloating, gas, pain, or any digestive symptoms"
                )
                onboardingStep(
                    icon: "drop.fill",
                    title: "Track Your Poop",
                    description: "Photo or manual entry using the Bristol Stool Chart"
                )
                onboardingStep(
                    icon: "brain.head.profile",
                    title: "AI Connects the Dots",
                    description: "Analyzes your data to find which foods cause your symptoms"
                )
            }

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func onboardingStep(icon: String, title: String, description: String) -> some View {
        HStack(spacing: AppSpacing.md) {
            ZStack {
                Circle()
                    .fill(AppColors.primaryContainer)
                    .frame(width: 48, height: 48)
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(AppColors.primary)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                Text(description)
                    .font(AppTypography.subhead)
                    .foregroundColor(AppColors.textSecondary)
            }

            Spacer()
        }
    }

    // MARK: - Page 3: What to Expect

    private var whatToExpectPage: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()

            Image("onboarding_what_to_expect", bundle: .main)
                .resizable()
                .scaledToFit()
                .frame(height: 160)

            Text("What to Expect")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            VStack(spacing: AppSpacing.md) {
                expectationRow(
                    icon: "3.circle.fill",
                    text: "Log for at least 3 days to run your first analysis"
                )
                expectationRow(
                    icon: "magnifyingglass",
                    text: "AI looks for patterns between what you eat and how you feel"
                )
                expectationRow(
                    icon: "lightbulb.fill",
                    text: "Get a personalized report with your likely trigger foods"
                )
            }

            Text("7 days of logging gives the best results.\nMost users find their first trigger within a week!")
                .font(AppTypography.callout)
                .foregroundColor(AppColors.success)
                .multilineTextAlignment(.center)
                .padding(.top, AppSpacing.sm)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func expectationRow(icon: String, text: String) -> some View {
        HStack(spacing: AppSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(AppColors.primary)
                .frame(width: 32)
            Text(text)
                .font(AppTypography.body)
                .foregroundColor(AppColors.text)
            Spacer()
        }
    }

    // MARK: - Page 4: Get Started

    private var getStartedPage: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()

            Image("onboarding_stay_on_track", bundle: .main)
                .resizable()
                .scaledToFit()
                .frame(height: 160)

            Text("Stay on Track")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            Text("Consistent logging is key.\nReminders help you build the habit.")
                .font(AppTypography.body)
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)

            // Reminders toggle
            HStack {
                Image(systemName: "bell.fill")
                    .foregroundColor(AppColors.primary)
                    .frame(width: 24)
                Text("Enable Reminders")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                Spacer()
                Toggle("", isOn: Binding(
                    get: { remindersEnabled },
                    set: { newValue in
                        if newValue {
                            Task {
                                let granted = await NotificationHelper.requestPermissionAndRegister()
                                remindersEnabled = granted
                            }
                        } else {
                            remindersEnabled = false
                        }
                    }
                ))
                    .labelsHidden()
                    .tint(AppColors.primary)
            }
            .padding(AppSpacing.md)
            .background(AppColors.surface)
            .cornerRadius(AppRadius.md)

            Text("You can always change this in Settings")
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.textTertiary)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    // MARK: - Bottom Controls

    private var pageIndicatorAndButtons: some View {
        VStack(spacing: AppSpacing.md) {
            // Page indicators
            HStack(spacing: AppSpacing.sm) {
                ForEach(0..<4, id: \.self) { index in
                    Circle()
                        .fill(index == currentPage ? AppColors.primary : AppColors.textTertiary.opacity(0.5))
                        .frame(width: 8, height: 8)
                }
            }

            // Navigation buttons
            if currentPage < 3 {
                Button {
                    withAnimation {
                        currentPage += 1
                    }
                } label: {
                    Text("Continue")
                }
                .buttonStyle(PrimaryButtonStyle())
                .padding(.horizontal, AppSpacing.xl)
            } else {
                Button {
                    Task { await completeOnboarding() }
                } label: {
                    if isSaving {
                        ProgressView()
                            .tint(.white)
                    } else {
                        Text("Get Started")
                    }
                }
                .buttonStyle(PrimaryButtonStyle(isEnabled: !isSaving))
                .disabled(isSaving)
                .padding(.horizontal, AppSpacing.xl)
            }

            if (1...2).contains(currentPage) {
                Button {
                    withAnimation {
                        currentPage -= 1
                    }
                } label: {
                    Text("Back")
                        .font(AppTypography.callout)
                        .foregroundColor(AppColors.textSecondary)
                }
            }
        }
    }

    // MARK: - Complete Onboarding

    private func completeOnboarding() async {
        isSaving = true
        defer { isSaving = false }

        if let uid = authService.currentUserId {
            try? await FirestoreService.shared.updateUserProfile(uid: uid, fields: [
                "preferences.notificationsEnabled": remindersEnabled
            ])
        }

        hasCompletedOnboarding = true
    }
}
