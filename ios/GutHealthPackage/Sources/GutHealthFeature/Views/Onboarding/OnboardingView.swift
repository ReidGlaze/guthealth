import SwiftUI
import FirebaseFirestore

public struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var currentPage = 0
    @State private var selectedPhase = "elimination"
    @State private var remindersEnabled = false
    @State private var isSaving = false

    private let authService = AuthService.shared
    private let phases = [
        ("elimination", "Elimination", "New to low-FODMAP? Remove high-FODMAP foods for 2-6 weeks"),
        ("reintroduction", "Reintroduction", "Ready to test one FODMAP group at a time"),
        ("maintenance", "Maintenance", "Manage your long-term personalized diet"),
        ("unsure", "I'm not sure", "We'll start you with elimination — change anytime in Settings")
    ]

    public init() {}

    public var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                TabView(selection: $currentPage) {
                    welcomePage.tag(0)
                    howItWorksPage.tag(1)
                    whatAreFodmapsPage.tag(2)
                    eliminationDietPage.tag(3)
                    fodmapJourneyPage.tag(4)
                    stayConsistentPage.tag(5)
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
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Image(systemName: "leaf.circle.fill")
                .font(.system(size: 100))
                .foregroundStyle(AppColors.primary)
                .symbolRenderingMode(.hierarchical)

            Text("Welcome to\nAI Gut Health")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)
                .multilineTextAlignment(.center)

            Text("Discover which foods trigger your\nsymptoms using AI")
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
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Text("How It Works")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            VStack(spacing: AppSpacing.xl) {
                onboardingStep(
                    icon: "camera.fill",
                    number: "1",
                    title: "Log Your Meals",
                    description: "Snap a photo and AI identifies foods with FODMAP levels"
                )
                onboardingStep(
                    icon: "chart.bar.fill",
                    number: "2",
                    title: "Track Symptoms",
                    description: "Record bloating, gas, pain, and other digestive symptoms"
                )
                onboardingStep(
                    icon: "sparkles",
                    number: "3",
                    title: "AI Finds Triggers",
                    description: "Our AI analyzes patterns to find your trigger foods"
                )
            }

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func onboardingStep(icon: String, number: String, title: String, description: String) -> some View {
        HStack(spacing: AppSpacing.md) {
            ZStack {
                Circle()
                    .fill(AppColors.primaryContainer)
                    .frame(width: 56, height: 56)
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(AppColors.primary)
            }

            VStack(alignment: .leading, spacing: AppSpacing.xs) {
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

    // MARK: - Page 3: What Are FODMAPs?

    private var whatAreFodmapsPage: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Image(systemName: "doc.text.magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(AppColors.primary)

            Text("What Are FODMAPs?")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            Text("Fermentable Oligosaccharides, Disaccharides,\nMonosaccharides, And Polyols")
                .font(AppTypography.subhead)
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)

            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                fodmapCategoryRow(name: "Fructose", examples: "Apples, pears, honey")
                fodmapCategoryRow(name: "Lactose", examples: "Milk, soft cheese, yogurt")
                fodmapCategoryRow(name: "Fructans", examples: "Wheat, garlic, onion")
                fodmapCategoryRow(name: "GOS", examples: "Legumes, cashews")
                fodmapCategoryRow(name: "Mannitol", examples: "Mushrooms, cauliflower")
                fodmapCategoryRow(name: "Sorbitol", examples: "Stone fruits, sweeteners")
            }
            .padding(.horizontal, AppSpacing.sm)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func fodmapCategoryRow(name: String, examples: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Circle()
                .fill(AppColors.primary)
                .frame(width: 8, height: 8)
            Text(name)
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)
                .frame(width: 80, alignment: .leading)
            Text(examples)
                .font(AppTypography.subhead)
                .foregroundColor(AppColors.textSecondary)
        }
    }

    // MARK: - Page 4: The Elimination Diet

    private var eliminationDietPage: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Image(systemName: "list.bullet.clipboard")
                .font(.system(size: 48))
                .foregroundColor(AppColors.primary)

            Text("The Elimination Diet")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            VStack(spacing: AppSpacing.md) {
                eliminationPhaseCard(
                    number: "1",
                    title: "Elimination",
                    duration: "2-6 weeks",
                    description: "Remove all high-FODMAP foods"
                )
                eliminationPhaseCard(
                    number: "2",
                    title: "Reintroduction",
                    duration: "6-8 weeks",
                    description: "Test one FODMAP group at a time"
                )
                eliminationPhaseCard(
                    number: "3",
                    title: "Maintenance",
                    duration: "Ongoing",
                    description: "Eat freely except personal triggers"
                )
            }

            Text("Most people find relief within the first 2 weeks")
                .font(AppTypography.footnote)
                .foregroundColor(AppColors.success)
                .italic()
                .multilineTextAlignment(.center)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func eliminationPhaseCard(number: String, title: String, duration: String, description: String) -> some View {
        HStack(spacing: AppSpacing.md) {
            ZStack {
                Circle()
                    .fill(AppColors.primaryContainer)
                    .frame(width: 40, height: 40)
                Text(number)
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.primary)
            }

            VStack(alignment: .leading, spacing: 2) {
                HStack {
                    Text(title)
                        .font(AppTypography.headline)
                        .foregroundColor(AppColors.text)
                    Text("(\(duration))")
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textTertiary)
                }
                Text(description)
                    .font(AppTypography.subhead)
                    .foregroundColor(AppColors.textSecondary)
            }
            Spacer()
        }
        .padding(AppSpacing.md)
        .background(AppColors.surface)
        .cornerRadius(AppRadius.md)
    }

    // MARK: - Page 5: FODMAP Journey

    private var fodmapJourneyPage: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Image(systemName: "leaf.fill")
                .font(.system(size: 48))
                .foregroundColor(AppColors.primary)

            Text("Your FODMAP Journey")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)

            Text("FODMAPs are certain carbohydrates that\ncan trigger IBS symptoms")
                .font(AppTypography.body)
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)

            VStack(spacing: AppSpacing.sm) {
                Text("Select your phase:")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                    .frame(maxWidth: .infinity, alignment: .leading)

                ForEach(phases, id: \.0) { phase in
                    phaseRow(id: phase.0, title: phase.1, subtitle: phase.2)
                }
            }
            .padding(.top, AppSpacing.sm)

            Spacer()
            Spacer()
        }
        .padding(.horizontal, AppSpacing.xl)
    }

    private func phaseRow(id: String, title: String, subtitle: String) -> some View {
        Button {
            selectedPhase = id
        } label: {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(AppTypography.callout)
                        .foregroundColor(AppColors.text)
                    Text(subtitle)
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
                Spacer()
                Image(systemName: selectedPhase == id ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(selectedPhase == id ? AppColors.primary : AppColors.textTertiary)
                    .font(.system(size: 22))
            }
            .padding(AppSpacing.md)
            .background(selectedPhase == id ? AppColors.primaryContainer : AppColors.surface)
            .cornerRadius(AppRadius.md)
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.md)
                    .stroke(selectedPhase == id ? AppColors.primary : AppColors.border, lineWidth: 1)
            )
        }
    }

    // MARK: - Page 6: Stay Consistent

    private var stayConsistentPage: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()

            Image(systemName: "calendar.badge.clock")
                .font(.system(size: 48))
                .foregroundColor(AppColors.primary)

            Text("Stay Consistent")
                .font(AppTypography.largeTitle)
                .foregroundColor(AppColors.text)

            Text("The AI needs at least 7 days of data to\nfind your trigger foods. Log every meal\nand symptom!")
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
                Toggle("", isOn: $remindersEnabled)
                    .labelsHidden()
                    .tint(AppColors.primary)
            }
            .padding(AppSpacing.md)
            .background(AppColors.surface)
            .cornerRadius(AppRadius.md)

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
                ForEach(0..<6, id: \.self) { index in
                    Circle()
                        .fill(index == currentPage ? AppColors.primary : AppColors.textTertiary.opacity(0.5))
                        .frame(width: 8, height: 8)
                }
            }

            // Navigation buttons
            if currentPage < 5 {
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

            if (1...4).contains(currentPage) {
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

        // Resolve phase — "unsure" defaults to elimination
        let resolvedPhase = selectedPhase == "unsure" ? "elimination" : selectedPhase

        // Save FODMAP phase to Firestore
        if let uid = authService.currentUserId {
            try? await FirestoreService.shared.updateUserProfile(uid: uid, fields: [
                "fodmapPhase": resolvedPhase,
                "fodmapPhaseStartDate": Timestamp(date: Date()),
                "preferences.notificationsEnabled": remindersEnabled
            ])
        }

        // Request notification permission if enabled
        if remindersEnabled {
            _ = await NotificationHelper.requestPermissionAndRegister()
        }

        hasCompletedOnboarding = true
    }
}
